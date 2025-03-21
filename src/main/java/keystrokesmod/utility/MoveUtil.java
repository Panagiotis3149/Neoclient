package keystrokesmod.utility;


import keystrokesmod.event.MoveEvent;
import keystrokesmod.mixins.impl.entity.EntityAccessor;
import keystrokesmod.module.impl.movement.TargetStrafe;
import keystrokesmod.script.classes.Vec3;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


import static keystrokesmod.Raven.mc;
import static keystrokesmod.module.impl.movement.BHop.autoJump;
import static keystrokesmod.module.impl.movement.BHop.speed;


public class MoveUtil {
    public static final double WALK_SPEED = 0.221;
    public static final double BUNNY_SLOPE = 0.66;
    public static final double MOD_SPRINTING = 1.3F;
    public static final double MOD_SNEAK = 0.3F;
    public static final double MOD_ICE = 2.5F;
    public static final double MOD_WEB = 0.105 / WALK_SPEED;
    public static final double JUMP_HEIGHT = 0.42F;
    public static final double BUNNY_FRICTION = 159.9F;
    public static final double Y_ON_GROUND_MIN = 0.00001;
    public static final double Y_ON_GROUND_MAX = 0.0626;

    public static final double AIR_FRICTION = 0.9800000190734863D;
    public static final double WATER_FRICTION = 0.800000011920929D;
    public static final double LAVA_FRICTION = 0.5D;
    public static final double MOD_SWIM = 0.115F / WALK_SPEED;
    public static final double[] MOD_DEPTH_STRIDER = {
            1.0F,
            0.1645F / MOD_SWIM / WALK_SPEED,
            0.1995F / MOD_SWIM / WALK_SPEED,
            1.0F / MOD_SWIM,
    };

    public static final double UNLOADED_CHUNK_MOTION = -0.09800000190735147;
    public static final double HEAD_HITTER_MOTION = -0.0784000015258789;


    public static void strafea() {
        strafe(speed(), mc.thePlayer);
    }


    public static void strafe2() {
        if (Utils.isMoving()) {
            if (mc.thePlayer.onGround && autoJump.isToggled()) {
                mc.thePlayer.jump();
            }
            mc.thePlayer.setSprinting(true);
            Utils.setSpeed(Utils.getHorizontalSpeed() + 0.005 * speed.getInput());
        }

    }


    public static void strafe(final double speed, EntityPlayerSP thePlayer) {
        final double yaw = direction();
        mc.thePlayer.motionX = -MathHelper.sin((float) yaw) * speed;
        mc.thePlayer.motionZ = MathHelper.cos((float) yaw) * speed;
    }

    public static void strafe(final double speed, float yaw) {
        yaw = (float) Math.toRadians(yaw);
        mc.thePlayer.motionX = -MathHelper.sin(yaw) * speed;
        mc.thePlayer.motionZ = MathHelper.cos(yaw) * speed;
    }

    public static float getMovementYaw() {
        return mc.thePlayer.rotationYaw;
    }

    public static double direction() {
        float rotationYaw = TargetStrafe.getMovementYaw();

        if (mc.thePlayer.moveForward < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (mc.thePlayer.moveForward < 0) {
            forward = -0.5F;
        } else if (mc.thePlayer.moveForward > 0) {
            forward = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0) {
            rotationYaw -= 70 * forward;
        }

        if (mc.thePlayer.moveStrafing < 0) {
            rotationYaw += 70 * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    public double getTickDist() {
        double xDist = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
        return Math.sqrt(Math.pow(xDist, 2) + Math.pow(zDist, 2));
    }

    public double movementDelta() {
        return Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
    }

    public double speedPotionAmp(final double amp) {
        return mc.thePlayer.isPotionActive(Potion.moveSpeed) ? ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) * amp) : 0;
    }

    public double getMCFriction() {
        float f = 0.91F;

        if (mc.thePlayer.onGround) {
            f = mc.theWorld.getBlockState(new BlockPos(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY) - 1, MathHelper.floor_double(mc.thePlayer.posZ))).getBlock().slipperiness * 0.91F;
        }

        return f;
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.2873D;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }
        return baseSpeed;
    }

    public static double baseSprintMaxSpeed = 0.212802D;

    public double getBps() {
        return (getSpeedDistance() * 20) * Utils.getTimer().timerSpeed;
    }

    public static double getSpeed() {
        return mc.thePlayer == null ? 0 : Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX
                + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
    }

    public static double getSpeed(MoveEvent moveEvent) {
        return mc.thePlayer == null ? 0 : Math.sqrt(moveEvent.x * moveEvent.x + moveEvent.z * moveEvent.z);
    }

    public double getSpeedDistance() {
        double distX = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
        double distZ = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
        return Math.sqrt(distX * distX + distZ * distZ);
    }

    public static  void strafe4() {
        strafe4(getSpeed());
    }


    // PANDAWARE STRAFES ARE FROM PANDAWARE 0.4.3 (old af)

    // Pandaware Strafe
    public static void strafe4(MoveEvent event) {
        strafe4(event, getSpeed());
    }

    // Pandaware Strafe
    public static void strafe4(double movementSpeed) {
        strafe4(null, movementSpeed);
    }

    public static double getLastDistance() {
        return Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ);
    }


    // Pandaware Strafe
    public static  void strafe4(MoveEvent moveEvent, double movementSpeed) {
        if (mc.thePlayer.movementInput.moveForward > 0.0) {
            mc.thePlayer.movementInput.moveForward = (float) 1.0;
        } else if (mc.thePlayer.movementInput.moveForward < 0.0) {
            mc.thePlayer.movementInput.moveForward = (float) -1.0;
        }

        if (mc.thePlayer.movementInput.moveStrafe > 0.0) {
            mc.thePlayer.movementInput.moveStrafe = (float) 1.0;
        } else if (mc.thePlayer.movementInput.moveStrafe < 0.0) {
            mc.thePlayer.movementInput.moveStrafe = (float) -1.0;
        }

        if (mc.thePlayer.movementInput.moveForward == 0.0 && mc.thePlayer.movementInput.moveStrafe == 0.0) {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionZ = 0.0;
        }

        if (mc.thePlayer.movementInput.moveForward != 0.0 && mc.thePlayer.movementInput.moveStrafe != 0.0) {
            mc.thePlayer.movementInput.moveForward *= Math.sin(0.6398355709958845);
            mc.thePlayer.movementInput.moveStrafe *= Math.cos(0.6398355709958845);
        }

        if (moveEvent != null) {
            moveEvent.x = mc.thePlayer.motionX = mc.thePlayer.movementInput.moveForward * movementSpeed * -Math.sin(Math.toRadians(mc.thePlayer.rotationYaw))
                    + mc.thePlayer.movementInput.moveStrafe * movementSpeed * Math.cos(Math.toRadians(mc.thePlayer.rotationYaw));
            moveEvent.z = mc.thePlayer.motionZ = mc.thePlayer.movementInput.moveForward * movementSpeed * Math.cos(Math.toRadians(mc.thePlayer.rotationYaw))
                    - mc.thePlayer.movementInput.moveStrafe * movementSpeed * -Math.sin(Math.toRadians(mc.thePlayer.rotationYaw));
        } else {
            mc.thePlayer.motionX = mc.thePlayer.movementInput.moveForward * movementSpeed * -Math.sin(Math.toRadians(mc.thePlayer.rotationYaw))
                    + mc.thePlayer.movementInput.moveStrafe * movementSpeed * Math.cos(Math.toRadians(mc.thePlayer.rotationYaw));
            mc.thePlayer.motionZ = mc.thePlayer.movementInput.moveForward * movementSpeed * Math.cos(Math.toRadians(mc.thePlayer.rotationYaw))
                    - mc.thePlayer.movementInput.moveStrafe * movementSpeed * -Math.sin(Math.toRadians(mc.thePlayer.rotationYaw));
        }
    }

    public static  double getLowHopMotion(double motion) {
        double base = MathUtil.roundToDecimal(mc.thePlayer.posY - (int) mc.thePlayer.posY, 2);

        if (base == 0.4) {
            return 0.31f;
        } else if (base == 0.71) {
            return 0.05f;
        } else if (base == 0.76) {
            return -0.2f;
        } else if (base == 0.56) {
            return -0.19f;
        } else if (base == 0.42) {
            return -0.12;
        }

        return motion;
    }

    /**
     * Gets the players' movement yaw
     */
    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }


    public static double speed() {
        return Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
    }


    public static int depthStriderLevel() {
        return EnchantmentHelper.getDepthStriderModifier(mc.thePlayer);
    }


    public static boolean enoughMovementForSprinting() {
        return Math.abs(mc.thePlayer.moveForward) >= 0.8F || Math.abs(mc.thePlayer.moveStrafing) >= 0.8F;
    }



    public static boolean canSprint(final boolean legit) {
        return (legit ? mc.thePlayer.moveForward >= 0.8F
                && !mc.thePlayer.isCollidedHorizontally
                && (mc.thePlayer.getFoodStats().getFoodLevel() > 6 || mc.thePlayer.capabilities.allowFlying)
                && !mc.thePlayer.isPotionActive(Potion.blindness)
                && !mc.thePlayer.isUsingItem()
                && !mc.thePlayer.isSneaking()
                : enoughMovementForSprinting());
    }


    public static double jumpBoostMotion(final double motionY) {
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            return motionY + (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
        }
        return motionY;



    }

    public static float simulationStrafeAngle(float currentMoveYaw, float maxAngle) {
        float workingYaw;
        float target = (float) Math.toDegrees(MoveUtil.direction());

        if (Math.abs(currentMoveYaw - target) <= maxAngle) {
            currentMoveYaw = target;
        } else if (currentMoveYaw > target) {
            currentMoveYaw -= maxAngle;
        } else {
            currentMoveYaw += maxAngle;
        }

        workingYaw = currentMoveYaw;

        MoveUtil.strafe(MoveUtil.speed(), workingYaw);

        return workingYaw;
    }

  // LB strafe i think idfk
    public static void strafe3(float speed, MoveEvent moveEvent) {
        double strength = 1.0;

        if (mc.thePlayer != null) {

            if (!Utils.isMoving()) {
                return;
            }

            double prevX = mc.thePlayer.motionX * (1.0 - strength);
            double prevZ = mc.thePlayer.motionZ * (1.0 - strength);
            double useSpeed = speed * strength;

            float yaw = Utils.getYaw(mc.thePlayer);
            double x = (-Math.sin(yaw) * useSpeed) + prevX;
            double z = (Math.cos(yaw) * useSpeed) + prevZ;
        }
    }

    // Strafe5 is RISE strafe
    public static void strafe5(final double speed) {
        strafe(speed, mc.thePlayer);
    }


    public static void strafe5(final double speed, EntityPlayerSP entity) {
        if (!isMoving()) {
            return;
        }

        final double yaw = direction();
        entity.motionX = -MathHelper.sin((float) yaw) * speed;
        entity.motionZ = MathHelper.cos((float) yaw) * speed;
    }

    public static void strafe5(final double speed, float yaw) {
        if (!isMoving()) {
            return;
        }

        yaw = (float) Math.toRadians(yaw);
        mc.thePlayer.motionX = -MathHelper.sin(yaw) * speed;
        mc.thePlayer.motionZ = MathHelper.cos(yaw) * speed;
    }



    /**
     * Basically calculates allowed horizontal distance just like NCP does
     *
     * @return allowed horizontal distance in one tick
     */
    public static double getAllowedHorizontalDistance() {
        return getAllowedHorizontalDistance(true);
    }

    public static double predictedMotion(final double motion, final int ticks) {
        return PlayerMove.predictedMotion(motion, ticks);
    }

    public static double predictedMotionXZ(double motion, int tick, boolean moving) {
        for (int i = 0; i < tick; i++) {
            if (!moving) motion /= 0.5;
            if (motion < 0.005)
                return 0;
        }
        return motion;
    }

    public static @NotNull Vec3 predictedPos(@NotNull EntityLivingBase entity, Vec3 motion, Vec3 result, int predTicks) {
        for (int i = 0; i < predTicks; i++) {
            result = result.add(
                    MoveUtil.predictedMotionXZ(motion.x(), i, MoveUtil.isMoving(entity)),
                    entity.onGround || !BlockUtils.replaceable(new BlockPos(result.toVec3())) ? 0 : MoveUtil.predictedMotion(motion.y(), i),
                    MoveUtil.predictedMotionXZ(motion.z(), i, MoveUtil.isMoving(entity))
            );
        }
        return result;
    }


    /**
     * Basically calculates allowed horizontal distance just like NCP does
     *
     * @return allowed horizontal distance in one tick
     */
    public static double getAllowedHorizontalDistance(boolean allowSprint) {
        double horizontalDistance;
        boolean useBaseModifiers = false;

        if (((EntityAccessor) mc.thePlayer).isInWeb()) {
            horizontalDistance = MOD_WEB * WALK_SPEED;
        } else if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) {
            horizontalDistance = MOD_SWIM * WALK_SPEED;

            final int depthStriderLevel = depthStriderLevel();
            if (depthStriderLevel > 0) {
                horizontalDistance *= MOD_DEPTH_STRIDER[depthStriderLevel];
                useBaseModifiers = true;
            }

        } else if (mc.thePlayer.isSneaking()) {
            horizontalDistance = MOD_SNEAK * WALK_SPEED;
        } else {
            horizontalDistance = WALK_SPEED;
            useBaseModifiers = true;
        }

        if (useBaseModifiers) {
            if (canSprint(false) && allowSprint) {
                horizontalDistance *= MOD_SPRINTING;
            }

            if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 0) {
                horizontalDistance *= 1 + (0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1));
            }

            if (mc.thePlayer.isPotionActive(Potion.moveSlowdown)) {
                horizontalDistance = 0.29;
            }
        }

        return horizontalDistance;
    }

    public static boolean isMoving() {
        return isMoving(mc.thePlayer);
    }

    @Contract(pure = true)
    public static boolean isMoving(@NotNull EntityLivingBase entity) {
        return entity.moveForward != 0 || entity.moveStrafing != 0;
    }

    public static void jump(float height) {
        mc.thePlayer.motionY = height;
        if (mc.thePlayer.isSprinting()) {
            float f = mc.thePlayer.rotationYaw * 0.017453292f;
            mc.thePlayer.motionX -= MathHelper.sin(f) * 0.2;
            mc.thePlayer.motionZ += MathHelper.cos(f) * 0.2;
        }
    }

    public static void jump(float height, float speed) {
        mc.thePlayer.motionY = height;
        if (mc.thePlayer.isSprinting()) {
            float f = mc.thePlayer.rotationYaw * 0.017453292f;
            mc.thePlayer.motionX -= MathHelper.sin(f) * speed;
            mc.thePlayer.motionZ += MathHelper.cos(f) * speed;
        }
    }

    public static void moveFlying(double increase) {
        if (!MoveUtil.isMoving()) return;
        final double yaw = MoveUtil.direction();
        mc.thePlayer.motionX += -MathHelper.sin((float) yaw) * increase;
        mc.thePlayer.motionZ += MathHelper.cos((float) yaw) * increase;
    }

}
