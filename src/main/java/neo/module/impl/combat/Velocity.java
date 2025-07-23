package neo.module.impl.combat;

import neo.event.ReceivePacketEvent;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;
import neo.util.packet.PacketUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;


public class Velocity extends Module {
    public static SliderSetting mode;
    public static SliderSetting horizontal;
    public static SliderSetting vertical;
    private final SliderSetting chance;
    private final ButtonSetting onlyWhileTargeting;
    private final ButtonSetting disableS;
    private final ButtonSetting onlyInAir;
    private boolean transaction = false;

    private static final String[] modes = {"Packet", "Cancel", "Vulcan"};

    public Velocity() {
        super("Velocity", category.combat, 0);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(horizontal = (SliderSetting) new SliderSetting("Horizontal", 90.0D, 0.0D, 100.0D, 1.0D)
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
        this.registerSetting(vertical   = (SliderSetting) new SliderSetting("Vertical",   100.0D, 0.0D, 100.0D, 1.0D)
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
        this.registerSetting(chance     = (SliderSetting) new SliderSetting("Chance",     100.0D, 0.0D, 100.0D, 1.0D, "%")
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
        this.registerSetting(onlyWhileTargeting = (ButtonSetting) new ButtonSetting("Only while targeting", false)
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
        this.registerSetting(disableS   = (ButtonSetting) new ButtonSetting("Disable while holding S", false)
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
        this.registerSetting(onlyInAir  = (ButtonSetting) new ButtonSetting("OnlyInAir", true)
                .setVisibleWhen(() -> (int) mode.getInput() == 0));
    }

    @Override
    public String getInfo() {
        return modes[(int) mode.getInput()];
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        switch ((int) mode.getInput()) {
            case 0:
                Packet<?> p = ReceivePacketEvent.getPacket();
                if (p instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity orig = (S12PacketEntityVelocity) p;
                    if (orig.getEntityID() == mc.thePlayer.getEntityId()
                            && (!onlyInAir.isToggled() || !mc.thePlayer.onGround)
                            && (chance.getInput() == 100 || Math.random() < chance.getInput() / 100.0)
                            && (!onlyWhileTargeting.isToggled() || mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null)
                            && (!disableS.isToggled() || !Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()))) {

                        event.cancelEvent();

                        double mx = orig.getMotionX() / 8000.0 * (horizontal.getInput() / 100.0);
                        double my = orig.getMotionY() / 8000.0 * (vertical.getInput() / 100.0);
                        double mz = orig.getMotionZ() / 8000.0 * (horizontal.getInput() / 100.0);

                        mc.thePlayer.motionX += mx;
                        mc.thePlayer.motionY += my;
                        mc.thePlayer.motionZ += mz;
                    }
                }

                if (p instanceof S27PacketExplosion) {
                    S27PacketExplosion orig = (S27PacketExplosion) p;
                    event.cancelEvent();

                    float x = (float) (orig.func_149149_c() * horizontal.getInput() / 100f);
                    float y = (float) (orig.func_149144_d() * vertical.getInput() / 100f);
                    float z = (float) (orig.func_149147_e() * horizontal.getInput() / 100f);
                    Vec3 vel = new Vec3(x, y, z);

                    mc.thePlayer.motionX += vel.xCoord;
                    mc.thePlayer.motionY += vel.yCoord;
                    mc.thePlayer.motionZ += vel.zCoord;
                }
                break;
            case 1:
                Packet<?> packet = ReceivePacketEvent.getPacket();
                if (packet instanceof S12PacketEntityVelocity) {
                    event.cancelEvent();
                }
                break;
            case 2:
                Packet<?> pa = ReceivePacketEvent.getPacket();
                if (pa instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity original = (S12PacketEntityVelocity) pa;


                    int id = original.getEntityID();
                    int motionX = original.getMotionX();
                    int motionY = original.getMotionY();
                    int motionZ = original.getMotionZ();

                    if (id == mc.thePlayer.getEntityId()) {
                        motionX *= 1.9;
                        motionY *= 1.0;
                        motionZ *= 1.9;
                    } else {
                        motionX *= 1.0;
                        motionY *= 1.0;
                        motionZ *= 1.0;
                    }

                    S12PacketEntityVelocity modified = new S12PacketEntityVelocity(id, motionX, motionY, motionZ);
                    event.setPacket(modified);
                } else if (pa instanceof S27PacketExplosion) {
                    S27PacketExplosion original = (S27PacketExplosion) pa;

                    float x = original.func_149149_c() * (1f / 100f);
                    float y = original.func_149144_d() * (1f / 100f);
                    float z = original.func_149147_e() * (1f / 100f);

                    Vec3 newVelocity = new Vec3(x, y, z);

                    S27PacketExplosion modified = new S27PacketExplosion(
                            original.getX(), original.getY(), original.getZ(),
                            original.getStrength(),
                            original.getAffectedBlockPositions(),
                            newVelocity
                    );

                    event.setPacket(modified);
                }

                if (pa instanceof S32PacketConfirmTransaction && mc.thePlayer.hurtTime == 10) {
                    event.cancelEvent();
                    event.setCanceled(true);
                    PacketUtils.sendPacket(new C0FPacketConfirmTransaction(
                            (short) (transaction ? 1 : -1),
                            (short) (transaction ? -1 : 1),
                            transaction
                    ));
                    transaction = !transaction;
                }
                break;
        }
    }
}
