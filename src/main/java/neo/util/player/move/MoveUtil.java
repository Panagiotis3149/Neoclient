package neo.util.player.move;


import neo.event.MoveEvent;
import neo.event.PreMotionEvent;
import neo.mixins.impl.entity.EntityAccessor;
import neo.module.impl.movement.TargetStrafe;
import neo.script.classes.Vec3;
import neo.util.world.block.BlockUtils;
import neo.util.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static neo.Neo.mc;
import static neo.Neo.moduleManager;


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

    public static final List<Double> WATER_VALUES = new ArrayList<>();

    public static final double UNLOADED_CHUNK_MOTION = -0.09800000190735147;
    public static final double HEAD_HITTER_MOTION = -0.0784000015258789;


    public static void strafea() {
        strafe(speed(), mc.thePlayer);
    }

    public static void strafe(final double speed) {
        strafe(speed, mc.thePlayer);
    }

    public static void setFSpeed(final double n) {
        if (n == 0.0) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionX = 0;
            return;
        }
        double n3 = mc.thePlayer.movementInput.moveForward;
        double n4 = mc.thePlayer.movementInput.moveStrafe;
        float rotationYaw = mc.thePlayer.rotationYaw;
        if (n3 == 0.0 && n4 == 0.0) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionX = 0;
        }
        else {
            if (n3 != 0.0) {
                if (n4 > 0.0) {
                    rotationYaw += ((n3 > 0.0) ? -45 : 45);
                }
                else if (n4 < 0.0) {
                    rotationYaw += ((n3 > 0.0) ? 45 : -45);
                }
                n4 = 0.0;
                if (n3 > 0.0) {
                    n3 = 1.0;
                }
                else if (n3 < 0.0) {
                    n3 = -1.0;
                }
            }
            final double radians = Math.toRadians(rotationYaw + 90.0f);
            final double sin = Math.sin(radians);
            final double cos = Math.cos(radians);
            mc.thePlayer.motionX = n3 * n * cos + n4 * n * sin;
            mc.thePlayer.motionZ = n3 * n * sin - n4 * n * cos;
        }
    }
    
    public static List<Double> getWaterValues() {
        WATER_VALUES.add(0.05999999821185753);
        WATER_VALUES.add(0.051999998867515274);
        WATER_VALUES.add(0.06159999881982969);
        WATER_VALUES.add(0.06927999889612124);
        WATER_VALUES.add(0.07542399904870933);
        WATER_VALUES.add(0.08033919924402255);
        WATER_VALUES.add(0.08427135945886732);
        WATER_VALUES.add(0.0874170876776148);
        WATER_VALUES.add(0.08993367029011523);
        WATER_VALUES.add(0.0519469373041872);
        WATER_VALUES.add(-0.05647059355944606);
        WATER_VALUES.add(0.03812980539822064);
        WATER_VALUES.add(-0.035014067535591664);
        WATER_VALUES.add(-0.04453032983624894);
        WATER_VALUES.add(0.019999999105927202);
        WATER_VALUES.add(-0.07159953051526458);
        WATER_VALUES.add(0.020820931761605266);
        WATER_VALUES.add(0.0010261658043049238);
        WATER_VALUES.add(-0.023717291273619878);
        WATER_VALUES.add(-0.010724939925282229);
        return WATER_VALUES;
    }

    public static double defaultSpeed() {
        double baseSpeed = 0.2873;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        return baseSpeed;
    }

    public static double defaultSpeedBase(double base) {
        double baseSpeed = base;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        return baseSpeed;
    }

    public static double roundToGround(final double posY) {
        return Math.round(posY / 0.015625) * 0.015625;
    }

    public static void partialStrafePercent(double percentage) {
        percentage /= 100;
        percentage = Math.min(1, Math.max(0, percentage));

        double motionX = mc.thePlayer.motionX;
        double motionZ = mc.thePlayer.motionZ;

        MoveUtil.strafe(Utils.getHorizontalSpeed());

        mc.thePlayer.motionX = motionX + (mc.thePlayer.motionX - motionX) * percentage;
        mc.thePlayer.motionZ = motionZ + (mc.thePlayer.motionZ - motionZ) * percentage;
    }

    public static double getJumpBoostMotion() {
        if (mc.thePlayer.isPotionActive(Potion.jump))
            return (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1;

        return 0;
    }


    public static void setMotion(double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionZ = 0.0;
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float)(forward > 0.0 ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            mc.thePlayer.motionX = (forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f)));
            mc.thePlayer.motionZ =(forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f)));
        }
    }



    public static void stop() {
        mc.thePlayer.motionX = 0;
        mc.thePlayer.motionZ = 0;
    }

    public static void strafec(double speed) {
        float yaw = mc.thePlayer.rotationYaw;
        float strafe = 45.0f;
        if (mc.thePlayer.moveForward < 0.0f) {
            strafe = -45.0f;
            yaw += 180.0f;
        }
        if (mc.thePlayer.moveStrafing > 0.0f) {
            yaw -= strafe;
            if (mc.thePlayer.moveForward == 0.0f) {
                yaw -= 45.0f;
            }
        } else if (mc.thePlayer.moveStrafing < 0.0f) {
            yaw += strafe;
            if (mc.thePlayer.moveForward == 0.0f) {
                yaw += 45.0f;
            }
        }
        float movementYaw = (float) Math.toRadians(yaw);
        if (isMoving()) {
            mc.thePlayer.motionZ = Math.cos(movementYaw) * speed;
            mc.thePlayer.motionX = -Math.sin(movementYaw) * speed;
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
        return Math.sqrt(xDist * xDist + zDist * zDist);
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



    public static void useDiagonalSpeed() {
        KeyBinding[] gameSettings = new KeyBinding[]{mc.gameSettings.keyBindForward, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft};

        final int[] down = {0};

        Arrays.stream(gameSettings).forEach(keyBinding -> {
            down[0] = down[0] + (keyBinding.isKeyDown() ? 1 : 0);
        });

        boolean active = down[0] == 1;

        if (!active) return;

        final double groundIncrease = (0.1299999676734952 - 0.12739998266255503) + 1E-7 - 1E-8;
        final double airIncrease = (0.025999999334873708 - 0.025479999685988748) - 1E-8;
        final double increase = mc.thePlayer.onGround ? groundIncrease : airIncrease;

        moveFlying(increase);
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

    public static void lookStrafe(PreMotionEvent e) {
        if (!moduleManager.killAura.isEnabled()) {
            float yaw = e.getYaw();
            float mYaw = yaw;
            boolean left = mc.gameSettings.keyBindLeft.isKeyDown();
            boolean right = mc.gameSettings.keyBindRight.isKeyDown();
            boolean forward = mc.gameSettings.keyBindForward.isKeyDown();
            boolean back = mc.gameSettings.keyBindBack.isKeyDown();
            if (forward && left) mYaw = yaw - 45;
            else if (forward && right) mYaw = yaw + 45;
            else if (back && left) mYaw = yaw - 135;
            else if (back && right) mYaw = yaw + 135;
            else if (forward) mYaw = yaw;
            else if (back) mYaw = yaw + 180;
            else if (left) mYaw = yaw - 90;
            else if (right) mYaw = yaw + 90;
            e.setYaw(Move.fromDeltaYaw(mYaw).getDeltaYaw());
        }
    }



    public double getSpeedDistance() {
        double distX = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
        double distZ = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;
        return Math.sqrt(distX * distX + distZ * distZ);
    }

    public static  void strafe4() {
        strafe4(getSpeed());
    }


    // Pandaware Strafe
    public static void strafe4(double movementSpeed) {
        strafe4(null, movementSpeed);
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

    // Strafe5 is RISE strafe
    public static void strafe5(final double speed) {
        strafe5(speed, mc.thePlayer);
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
