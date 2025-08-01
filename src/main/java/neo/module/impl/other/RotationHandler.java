package neo.module.impl.other;

import neo.event.MoveInputEvent;
import neo.event.PreMotionEvent;
import neo.event.PreUpdateEvent;
import neo.event.RotationEvent;
import neo.module.Module;
import neo.module.impl.movement.TargetStrafe;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.player.move.MoveUtil;
import neo.util.player.move.RotationUtils;
import neo.util.aim.AimSimulator;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class RotationHandler extends Module {
    public static final ButtonSetting rotateBody = new ButtonSetting("Rotate body", true);
    public static final ButtonSetting fullBody = new ButtonSetting("Full body", false);
    public static final SliderSetting randomYawFactor = new SliderSetting("Random yaw factor", 1.0, 0.0, 10.0, 1.0);
    private static final SliderSetting defaultMoveFix = new SliderSetting("Default MoveFix", new String[]{"None", "Silent", "Strict"}, 0);
    private static @Nullable Float movementYaw = null;
    private static @Nullable Float rotationYaw = null;
    private static float prevRotationYaw;
    private static @Nullable Float rotationPitch = null;
    private static float prevRotationPitch;
    private static boolean isSet = false;
    private static MoveFix moveFix = MoveFix.None;
    private final SliderSetting smoothBack = new SliderSetting("Smooth back", new String[]{"None", "Default"}, 0);
    private final SliderSetting aimSpeed = new SliderSetting("Aim speed", 5, 1, 15, 0.1);

    public RotationHandler() {
        super("RotationHandler", category.other);
        this.registerSetting(defaultMoveFix);
        this.registerSetting(smoothBack);
        this.registerSetting(aimSpeed);
        this.registerSetting(new DescriptionSetting("Classic"));
        this.registerSetting(rotateBody);
        this.registerSetting(fullBody);
        this.registerSetting(randomYawFactor);
        this.registerSetting(new DescriptionSetting("Debug"));
        this.canBeEnabled = false;
    }



    public static float getPrevRotationYaw() {
        return prevRotationYaw;
    }

    public static float getPrevRotationPitch() {
        return prevRotationPitch;
    }

    public static boolean isSet() {
        return isSet;
    }

    public static float getMovementYaw(Entity entity) {
        if (entity instanceof EntityPlayerSP && movementYaw != null)
            return movementYaw;
        return entity.rotationYaw;
    }

    public static void setMovementYaw(float movementYaw) {
        RotationHandler.movementYaw = movementYaw;
    }

    public static MoveFix getMoveFix() {
        if (moveFix != null)
            return moveFix;
        return MoveFix.values()[(int) defaultMoveFix.getInput()];
    }

    public static void setMoveFix(MoveFix moveFix) {
        RotationHandler.moveFix = moveFix;
    }

    public static float getRotationYaw() {
        return getRotationYaw(mc.thePlayer.rotationYaw);
    }

    public static void setRotationYaw(float rotationYaw) {
        if (AimSimulator.yawEquals(rotationYaw, mc.thePlayer.rotationYaw)) {
            RotationHandler.rotationYaw = null;
            return;
        }
        RotationHandler.rotationYaw = rotationYaw;
    }

    public static float getRotationYaw(float yaw) {
        if (rotationYaw != null)
            return rotationYaw;
        return yaw;
    }

    public static float getRotationPitch() {
        return getRotationPitch(mc.thePlayer.rotationPitch);
    }

    public static void setRotationPitch(float rotationPitch) {
        if (rotationPitch == mc.thePlayer.rotationPitch) {
            RotationHandler.rotationPitch = null;
            return;
        }
        RotationHandler.rotationPitch = rotationPitch;
    }

    public static float getRotationPitch(float pitch) {
        if (rotationPitch != null)
            return rotationPitch;
        return pitch;
    }

    @NotNull
    public static Vec3 getLook(float partialTicks) {
        if (partialTicks == 1.0F) {
            return RotationUtils.getVectorForRotation(RotationHandler.getRotationPitch(), RotationHandler.getRotationYaw());
        } else {
            float f = RotationHandler.getPrevRotationPitch() + (RotationHandler.getRotationPitch() - RotationHandler.getPrevRotationPitch()) * partialTicks;
            float f1 = RotationHandler.getPrevRotationYaw() + (RotationHandler.getRotationYaw() - RotationHandler.getPrevRotationYaw()) * partialTicks;
            return RotationUtils.getVectorForRotation(f, f1);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreUpdate(PreUpdateEvent event) {
        prevRotationYaw = getRotationYaw();
        prevRotationPitch = getRotationPitch();
        if (isSet && mc.currentScreen == null) {
            float viewYaw = RotationUtils.normalize(mc.thePlayer.rotationYaw, -180, 180);
            float viewPitch = RotationUtils.normalize(mc.thePlayer.rotationPitch, -180, 180);
            switch ((int) smoothBack.getInput()) {
                case 0:
                    rotationYaw = null;
                    rotationPitch = null;
                    break;
                case 1:
                    setRotationYaw(AimSimulator.rotMove(viewYaw, getRotationYaw(), (float) aimSpeed.getInput()));
                    setRotationPitch(AimSimulator.rotMove(viewPitch, getRotationPitch(), (float) aimSpeed.getInput()));
                    break;
            }
        }

        if (AimSimulator.yawEquals(getRotationYaw(), mc.thePlayer.rotationYaw)) rotationYaw = null;
        if (getRotationPitch() == mc.thePlayer.rotationPitch) rotationPitch = null;

        RotationEvent rotationEvent = new RotationEvent(getRotationYaw(), getRotationPitch(), MoveFix.values()[(int) defaultMoveFix.getInput()]);
        MinecraftForge.EVENT_BUS.post(rotationEvent);
        isSet = (rotationEvent.isSet() || rotationYaw != null || rotationPitch != null) && rotationEvent.isSmoothBack();
        if (isSet) {
            rotationYaw = rotationEvent.getYaw();
            rotationPitch = rotationEvent.getPitch();
            moveFix = rotationEvent.getMoveFix();
        } else {
            movementYaw = null;
            moveFix = null;
        }
    }

    /**
     * Fix movement
     *
     * @param event before update living entity (move)
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMoveInput(MoveInputEvent event) {
        if (isSet) {
            switch (moveFix) {
                case None:
                    movementYaw = null;
                    break;
                case Silent:
                    movementYaw = getRotationYaw();

                    final float forward = event.getForward();
                    final float strafe = event.getStrafe();

                    final double angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MoveUtil.direction(TargetStrafe.getMovementYaw(), forward, strafe)));

                    if (forward == 0 && strafe == 0) {
                        return;
                    }

                    float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

                    for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
                        for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                            if (predictedStrafe == 0 && predictedForward == 0) continue;

                            final double predictedAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MoveUtil.direction(movementYaw, predictedForward, predictedStrafe)));
                            final double difference = Math.abs(angle - predictedAngle);

                            if (difference < closestDifference) {
                                closestDifference = (float) difference;
                                closestForward = predictedForward;
                                closestStrafe = predictedStrafe;
                            }
                        }
                    }

                    event.setForward(closestForward);
                    event.setStrafe(closestStrafe);
                    break;
                case Strict:
                    movementYaw = getRotationYaw();
                    break;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreMotion(PreMotionEvent event) {
        if (rotationYaw != null) {
            final float yaw = rotationYaw;
            event.setYaw(yaw);
            // RenderUtils.renderPitch handle this
//            mc.thePlayer.rotationYawHead = yaw;

        }
        if (rotationPitch != null) {
            final float pitch = rotationPitch;
            event.setPitch(pitch);
            // RenderUtils.renderPitch handle this
//            mc.thePlayer.renderPitchHead = pitch;
        }
    }


    public enum MoveFix {
        None,
        Silent,
        Strict;

        public static final String[] MODES = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);
    }
}