package neo.module.impl.combat;

import neo.event.PreTickEvent;
import neo.event.PreUpdateEvent;
import neo.event.ReceivePacketEvent;
import neo.event.Render3DEvent;
import neo.mixins.impl.entity.S14PacketEntityAccessor;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.impl.player.Blink;
import neo.module.impl.render.TargetESP;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import neo.script.classes.Vec3;
import neo.util.Utils;
import neo.util.packet.PacketUtils;
import neo.util.packet.backtrack.TimedPacket;
import neo.util.render.RenderUtils;
import neo.util.render.Theme;
import neo.util.render.animation.Animation;
import neo.util.render.animation.Easing;
import neo.util.render.comp.TargetESPComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Backtrack extends Module {

    private final SliderSetting minLatency = new SliderSetting("Min latency", 50, 10, 1000, 10);
    private final SliderSetting maxLatency = new SliderSetting("Max latency", 100, 10, 1000, 10);
    private final SliderSetting minDistance = new SliderSetting("Min distance", 0.0, 0.0, 3.0, 0.1);
    private final SliderSetting maxDistance = new SliderSetting("Max distance", 6.0, 0.0, 10.0, 0.1);
    private final SliderSetting stopOnTargetHurtTime = new SliderSetting("Stop on target HurtTime", -1, -1, 10, 1);
    private final SliderSetting stopOnSelfHurtTime = new SliderSetting("Stop on self HurtTime", -1, -1, 10, 1);
    private final ButtonSetting drawRealPosition = new ButtonSetting("Draw real position", true);

    private final ButtonSetting esp = new ButtonSetting("Render TargetESP", true);

    private final java.util.Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private final List<Packet<?>> skipPackets = new ArrayList<>();
    private @Nullable Animation animationX;
    private @Nullable Animation animationY;
    private @Nullable Animation animationZ;
    private Vec3 vec3;
    private EntityPlayer target;

    private int currentLatency = 0;

    public Backtrack() {
        super("Backtrack", category.combat);
        this.registerSetting(new DescriptionSetting("Allows you to hit past opponents."));
        this.registerSetting(minLatency);
        this.registerSetting(maxLatency);
        this.registerSetting(minDistance);
        this.registerSetting(maxDistance);
        this.registerSetting(stopOnTargetHurtTime);
        this.registerSetting(stopOnSelfHurtTime);
        this.registerSetting(drawRealPosition);
        this.registerSetting(esp);
    }

    @Override
    public String getInfo() {
        return (currentLatency == 0 ? (int) maxLatency.getInput() : currentLatency) + "ms";
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(minLatency, maxLatency);
        Utils.correctValue(minDistance, maxDistance);
    }

    @Override
    public void onEnable() {
        packetQueue.clear();
        skipPackets.clear();
        vec3 = null;
        target = null;
    }

    @Override
    public void onDisable() {
        releaseAll();
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        try {
            final double distance = vec3.distanceTo(mc.thePlayer);
            if (distance > maxDistance.getInput()
                    || distance < minDistance.getInput()
            ) {
                currentLatency = 0;
            }

        } catch (NullPointerException ignored) {
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (target == null || vec3 == null || target.isDead) return;
        if (esp.isToggled()) {
            final net.minecraft.util.Vec3 pos = currentLatency > 0 ? vec3.toVec3() : target.getPositionVector();
            if (animationX == null || animationY == null || animationZ == null) {
                animationX = new Animation(Easing.EASE_OUT_CIRC, 300);
                animationY = new Animation(Easing.EASE_OUT_CIRC, 300);
                animationZ = new Animation(Easing.EASE_OUT_CIRC, 300);

                animationX.setValue(pos.xCoord);
                animationY.setValue(pos.yCoord);
                animationZ.setValue(pos.zCoord);
            }

            animationX.run(pos.xCoord);
            animationY.run(pos.yCoord);
            animationZ.run(pos.zCoord);

            Color color = RenderUtils.toColor(RenderUtils.toArgb(RenderUtils.toRgbColor(Theme.getGradient(ModuleManager.targetESP.theme.getInput(), 0.0)), ModuleManager.targetESP.alpha.getInput()));
            if (TargetESP.white.isToggled()) {
                color = Color.WHITE;
            }
            float speed = (float) ModuleManager.targetESP.speed.getInput();
            if ((int) ModuleManager.targetESP.mode.getInput() != 4) {
                speed = TargetESPComponent.getSpeed((int) ModuleManager.targetESP.speed.getInput());
            }
            TargetESPComponent.render((int) ModuleManager.targetESP.mode.getInput(), new net.minecraft.util.Vec3(animationX.getValue(), animationY.getValue(), animationZ.getValue()), color, speed);
        }
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent e) {
        while (!packetQueue.isEmpty()) {
            try {
                if (packetQueue.element().getCold().getIdk(currentLatency)) {
                    Packet<?> packet = packetQueue.remove().getPacket();
                    skipPackets.add(packet);
                    PacketUtils.receivePacket2(packet);
                } else {
                    break;
                }
            } catch (NullPointerException ignored) {
            }
        }

        if (packetQueue.isEmpty() && target != null) {
            vec3 = new Vec3(target.getPositionVector());
        }
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        if (target == null || vec3 == null || target.isDead)
            return;

        final net.minecraft.util.Vec3 pos = currentLatency > 0 ? vec3.toVec3() : target.getPositionVector();

        if (animationX == null || animationY == null || animationZ == null) {
            animationX = new Animation(Easing.EASE_OUT_CIRC, 300);
            animationY = new Animation(Easing.EASE_OUT_CIRC, 300);
            animationZ = new Animation(Easing.EASE_OUT_CIRC, 300);

            animationX.setValue(pos.xCoord);
            animationY.setValue(pos.yCoord);
            animationZ.setValue(pos.zCoord);
        }

        animationX.run(pos.xCoord);
        animationY.run(pos.yCoord);
        animationZ.run(pos.zCoord);
        if (drawRealPosition.isToggled()) {
            Blink.drawBoxi(new net.minecraft.util.Vec3(animationX.getValue(), animationY.getValue(), animationZ.getValue()));
        }
    }

    @SubscribeEvent
    public void onAttack(@NotNull AttackEntityEvent e) {
        final Vec3 targetPos = new Vec3(e.entity);
        if (e.entity instanceof EntityPlayer) {
            if (target == null || e.entity != target) {
                vec3 = targetPos;
                if (animationX != null && animationY != null && animationZ != null) {
                    long duration = target == null ? 0 : Math.min(500, Math.max(100, (long) new Vec3(e.entity).distanceTo(target) * 50));
                    animationX.setDuration(duration);
                    animationY.setDuration(duration);
                    animationZ.setDuration(duration);
                }
            } else if (animationX != null && animationY != null && animationZ != null) {
                animationX.setDuration(100);
                animationY.setDuration(100);
                animationZ.setDuration(100);
            }
            target = (EntityPlayer) e.entity;

            try {
                final double distance = targetPos.distanceTo(mc.thePlayer);
                if (distance > maxDistance.getInput() || distance < minDistance.getInput())
                    return;

            } catch (NullPointerException ignored) {
            }

            currentLatency = (int) (Math.random() * (maxLatency.getInput() - minLatency.getInput()) + minLatency.getInput());
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent e) {
        if (!Utils.isntnull()) return;
        Packet<?> p = e.getPacket();
        if (skipPackets.contains(p)) {
            skipPackets.remove(p);
            return;
        }

        if (target != null && stopOnTargetHurtTime.getInput() != -1 && target.hurtTime == stopOnTargetHurtTime.getInput()) {
            releaseAll();
            return;
        }
        if (stopOnSelfHurtTime.getInput() != -1 && mc.thePlayer.hurtTime == stopOnSelfHurtTime.getInput()) {
            releaseAll();
            return;
        }

        try {
            if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 20) {
                packetQueue.clear();
                return;
            }

            if (target == null) {
                releaseAll();
                return;
            }

            if (e.isCanceled())
                return;

            if (p instanceof S19PacketEntityStatus
                    || p instanceof S02PacketChat
                    || p instanceof S0BPacketAnimation
                    || p instanceof S06PacketUpdateHealth
            )
                return;

            if (p instanceof S08PacketPlayerPosLook || p instanceof S40PacketDisconnect) {
                releaseAll();
                target = null;
                vec3 = null;
                return;

            } else if (p instanceof S13PacketDestroyEntities) {
                S13PacketDestroyEntities wrapper = (S13PacketDestroyEntities) p;
                for (int id : wrapper.getEntityIDs()) {
                    if (id == target.getEntityId()) {
                        target = null;
                        vec3 = null;
                        releaseAll();
                        return;
                    }
                }
            } else if (p instanceof S14PacketEntity) {
                S14PacketEntity wrapper = (S14PacketEntity) p;
                if (((S14PacketEntityAccessor) wrapper).getEntityId() == target.getEntityId()) {
                    vec3 = vec3.add(wrapper.func_149062_c() / 32.0D, wrapper.func_149061_d() / 32.0D,
                            wrapper.func_149064_e() / 32.0D);
                }
            } else if (p instanceof S18PacketEntityTeleport) {
                S18PacketEntityTeleport wrapper = (S18PacketEntityTeleport) p;
                if (wrapper.getEntityId() == target.getEntityId()) {
                    vec3 = new Vec3(wrapper.getX() / 32.0D, wrapper.getY() / 32.0D, wrapper.getZ() / 32.0D);
                }
            }

            packetQueue.add(new TimedPacket(p));
            e.cancelEvent();
            e.setCanceled(true);
        } catch (NullPointerException ignored) {

        }
    }

    private void releaseAll() {
        if (!packetQueue.isEmpty()) {
            for (TimedPacket timedPacket : packetQueue) {
                Packet<?> packet = timedPacket.getPacket();
                skipPackets.add(packet);
                PacketUtils.receivePacket2(packet);
            }
            packetQueue.clear();
        }
    }

}
