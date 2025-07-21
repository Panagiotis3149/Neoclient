package neo.util.aim;

import com.google.common.base.Predicates;
import neo.util.other.java.mixin.IRotationAccess;
import neo.util.Utils;
import neo.util.other.java.Reflection;
import neo.util.world.block.BlockUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleFunction;

import static neo.Neo.mc;

// This is... Mostly for legit rots, but not fully.
public class QuantumAim {
    public static double ACCURATE_ROTATION_YAW_LEVEL;
    public static double ACCURATE_ROTATION_YAW_VL;
    public static double ACCURATE_ROTATION_PITCH_LEVEL;
    public static double ACCURATE_ROTATION_PITCH_VL;
    public static double ACCURATE_ROTATION_YAW_LEVEL1;
    public static double ACCURATE_ROTATION_YAW_VL1;
    public static double ACCURATE_ROTATION_PITCH_LEVEL1;
    public static double ACCURATE_ROTATION_PITCH_VL1;
    private static double lastX;
    private static double lastY;
    private static double lastZ;
    private static Entity lastTarget;
    private static double x;
    private static double y;
    private static double z;
    private static double lastAngle;


    private static double getDistanceToBlockPos(final BlockPos blockPos) {
        double distance = 1337.0;
        for (float x = (float)blockPos.getX(); x <= blockPos.getX() + 1; x += (float)0.2) {
            for (float y = (float)blockPos.getY(); y <= blockPos.getY() + 1; y += (float)0.2) {
                for (float z = (float)blockPos.getZ(); z <= blockPos.getZ() + 1; z += (float)0.2) {
                    final double d0 = mc.thePlayer.getDistance(x, y, z);
                    if (d0 < distance) {
                        distance = d0;
                    }
                }
            }
        }
        return distance;
    }

    private static ArrayList<BlockPos> getBlockPos() {
        final BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ);
        final ArrayList<BlockPos> blockPoses = new ArrayList<BlockPos>();
        for (int x = playerPos.getX() - 2; x <= playerPos.getX() + 2; ++x) {
            for (int y = playerPos.getY() - 1; y <= playerPos.getY(); ++y) {
                for (int z = playerPos.getZ() - 2; z <= playerPos.getZ() + 2; ++z) {
                    if (BlockUtils.isOkBlock(new BlockPos(x, y, z))) {
                        blockPoses.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        if (!blockPoses.isEmpty()) {
            blockPoses.sort(Comparator.comparingDouble(blockPos -> mc.thePlayer.getDistance(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5)));
        }
        return blockPoses;
    }
    
    
    public static BlockPos getAimBlockPos() {
        final BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ);
        if ((mc.gameSettings.keyBindJump.isKeyDown() || !mc.thePlayer.onGround) && mc.thePlayer.moveForward == 0.0f && mc.thePlayer.moveStrafing == 0.0f && BlockUtils.isOkBlock(playerPos.add(0, -1, 0))) {
            return playerPos.add(0, -1, 0);
        }
        BlockPos blockPos = null;
        final ArrayList<BlockPos> bp = getBlockPos();
        final ArrayList<BlockPos> blockPositions = new ArrayList<BlockPos>();
        if (bp.size() > 0) {
            for (int i = 0; i < Math.min(bp.size(), 18); ++i) {
                blockPositions.add(bp.get(i));
            }
            blockPositions.sort(Comparator.comparingDouble((ToDoubleFunction<? super BlockPos>)QuantumAim::getDistanceToBlockPos));
            if (blockPositions.size() > 0) {
                blockPos = blockPositions.get(0);
            }
        }
        return blockPos;
    }

    public static MovingObjectPosition customRayTrace(Entity entity, double reach, float partialTicks, float yaw, float pitch) {
        Vec3 eyes = entity.getPositionEyes(partialTicks);
        Vec3 look = getCustomLook(entity, partialTicks, yaw, pitch);
        Vec3 end = eyes.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        return entity.worldObj.rayTraceBlocks(eyes, end, false, false, true);
    }

    public static Vec3 getCustomLook(Entity entity, float partialTicks, float yaw, float pitch) {
        if (partialTicks == 1.0f || partialTicks == 2.0f) {
            return ((IRotationAccess) entity).callGetVectorForRotation(pitch, yaw);
        }
        float interpPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        float interpYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        return ((IRotationAccess) entity).callGetVectorForRotation(interpPitch, interpYaw);
    }

    public static float rotateToYaw(final float yawSpeed, final float currentYaw, final float calcYaw) {
        float yaw = updateRotation(currentYaw, calcYaw, yawSpeed + RandomUtil.nextFloat(0.0f, 15.0f));
        final double diffYaw = MathHelper.wrapAngleTo180_float(calcYaw - currentYaw);
        if (-yawSpeed > diffYaw || diffYaw > yawSpeed) {
            yaw += (float)(RandomUtil.nextFloat(1.0f, 2.0f) * Math.sin(mc.thePlayer.rotationPitch *3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679));
        }
        if (yaw == currentYaw) {
            return currentYaw;
        }
        if (mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float f2 = f1 * f1 * f1 * 8.0f;
        final int deltaX = (int)((6.667 * yaw - 6.666666666666667 * currentYaw) / f2);
        final float f3 = deltaX * f2;
        yaw = (float)(currentYaw + f3 * 0.15);
        return yaw;
    }

    public static MovingObjectPosition rayCast(final float partialTicks, final float[] rots) {
        MovingObjectPosition objectMouseOver = null;
        final Entity entity = mc.getRenderViewEntity();
        if (entity != null && mc.theWorld != null) {
            mc.mcProfiler.startSection("pick");
            mc.pointedEntity = null;
            double d0 = mc.playerController.getBlockReachDistance();
            objectMouseOver = customRayTrace(entity, d0, partialTicks, rots[0], rots[1]);
            double d2 = d0;
            final Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            if (mc.playerController.extendedReach()) {
                d0 = 6.0;
                d2 = 6.0;
            }
            else {
                if (d0 > 3.0) {
                    flag = true;
                }
                d0 = d0;
            }
            if (objectMouseOver != null) {
                d2 = objectMouseOver.hitVec.distanceTo(vec3);
            }
            final Vec3 vec4 = getCustomLook(entity, partialTicks, rots[0], rots[1]);
            final Vec3 vec5 = vec3.addVector(vec4.xCoord * d0, vec4.yCoord * d0, vec4.zCoord * d0);
            Entity pointedEntity = null;
            Vec3 vec6 = null;
            final float f = 1.0f;
            final List list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec4.xCoord * d0, vec4.yCoord * d0, vec4.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING));
            double d3 = d2;
            final AxisAlignedBB realBB = null;
            for (int i = 0; i < list.size(); ++i) {
                final Entity entity2 = (Entity) list.get(i);
                final float f2 = entity2.getCollisionBorderSize();
                final AxisAlignedBB axisalignedbb = entity2.getEntityBoundingBox().expand(f2, f2, f2);
                final MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec5);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (d3 >= 0.0) {
                        pointedEntity = entity2;
                        vec6 = ((movingobjectposition == null) ? vec3 : movingobjectposition.hitVec);
                        d3 = 0.0;
                    }
                }
                else if (movingobjectposition != null) {
                    final double d4 = vec3.distanceTo(movingobjectposition.hitVec);
                    if (d4 < d3 || d3 == 0.0) {
                        boolean flag3 = false;
                        if (Reflection.canRiderInteractExists(entity2)) {
                            flag3 = Reflection.callBoolean(entity2, String.valueOf(Reflection.callCanRiderInteract(entity2)), new Object[0]);
                        }
                        if (entity2 == entity.ridingEntity && !flag3) {
                            if (d3 == 0.0) {
                                pointedEntity = entity2;
                                vec6 = movingobjectposition.hitVec;
                            }
                        }
                        else {
                            pointedEntity = entity2;
                            vec6 = movingobjectposition.hitVec;
                            d3 = d4;
                        }
                    }
                }
            }
            if (pointedEntity != null && flag && vec3.distanceTo(vec6) > 3.0) {
                pointedEntity = null;
                objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec6, null, new BlockPos(vec6));
            }
            if (pointedEntity != null && (d3 < d2 || objectMouseOver == null)) {
                objectMouseOver = new MovingObjectPosition(pointedEntity, vec6);
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    pointedEntity = pointedEntity;
                }
            }
        }
        return objectMouseOver;
    }


    private static float[] testRots(final float currentYaw, final float currentPitch, float calcYaw, float calcPitch, final Entity entity, final float speedYaw, final float speedPitch, final Vec3 best, final boolean smartAim, final boolean throughWalls) {
        final double radius = RandomUtil.nextDouble(0.001, 2.0);
        lastAngle = ((lastAngle > 360.0) ? 0.0 : (lastAngle + RandomUtil.nextDouble(-0.4, 1.2)));
        double x = Math.sin(lastAngle) * radius;
        double y = Math.cos(lastAngle) * radius;
        calcYaw += (float)x;
        calcPitch += (float)y;
        float diffYaw = MathHelper.wrapAngleTo180_float(calcYaw - currentYaw);
        if (Math.abs(diffYaw) > 10.0f) {
            if (diffYaw > speedYaw) {
                diffYaw = speedYaw;
            }
            if (diffYaw < -speedYaw) {
                diffYaw = -speedYaw;
            }
        }
        else {
            diffYaw *= RandomUtil.nextFloat(0.3, 0.7);
        }
        float diffPitch = MathHelper.wrapAngleTo180_float(calcPitch - currentPitch);
        if (Math.abs(diffPitch) > 10.0f) {
            if (diffPitch > speedPitch) {
                diffPitch = speedPitch;
            }
            if (diffPitch < -speedPitch) {
                diffPitch = -speedPitch;
            }
        }
        else {
            diffPitch *= RandomUtil.nextFloat(0.3, 0.7);
        }
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase entityPlayer = (EntityLivingBase)entity;
            if (entityPlayer.hurtTime < 1) {
                final MovingObjectPosition objectPosition = rayCast(1.0f, new float[] { currentYaw + diffYaw, currentPitch + diffPitch });
                if (smartAim && !throughWalls) {
                    final double ePosX = best.xCoord;
                    final double ePosY = best.yCoord;
                    final double ePosZ = best.zCoord;
                    x = ePosX - mc.thePlayer.posX;
                    y = ePosY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
                    z = ePosZ - mc.thePlayer.posZ;
                }
                else {
                    final Vec3 entityVec = getBestHitVec(entity);
                    x = entityVec.xCoord - mc.thePlayer.posX;
                    y = entityVec.yCoord - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
                    z = entityVec.zCoord - mc.thePlayer.posZ;
                }
                final float newCalcYaw = (float)(MathHelper.atan2(z, x) * 180.0 /3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679 - 90.0);
                final float newCalcPitch = (float)(-(MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0 /3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679));
                float diffY = MathHelper.wrapAngleTo180_float(newCalcYaw - currentYaw);
                if (Math.abs(diffY) > -1.0f) {
                    if (diffY > speedYaw) {
                        diffY = speedYaw;
                    }
                    if (diffY < -speedYaw) {
                        diffY = -speedYaw;
                    }
                }
                else {
                    diffY *= RandomUtil.nextFloat(0.3, 0.7);
                }
                float diffP = MathHelper.wrapAngleTo180_float(newCalcPitch - currentPitch);
                if (Math.abs(diffP) > -1.0f) {
                    if (diffP > speedPitch) {
                        diffP = speedPitch;
                    }
                    if (diffP < -speedPitch) {
                        diffP = -speedPitch;
                    }
                }
                else {
                    diffP *= RandomUtil.nextFloat(0.3, 0.7);
                }
                final MovingObjectPosition objectPosition2 = rayCast(1.0f, new float[] { currentYaw + diffY, currentPitch + diffP });
                if (objectPosition != null && objectPosition2 != null && objectPosition2.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && objectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                    diffYaw = diffY;
                    diffPitch = diffP;
                }
            }
        }
        if (entity == null) {
            ACCURATE_ROTATION_YAW_LEVEL = 0.0;
            ACCURATE_ROTATION_YAW_VL = 0.0;
            ACCURATE_ROTATION_PITCH_LEVEL = 0.0;
            ACCURATE_ROTATION_PITCH_VL = 0.0;
        }
        float bestYaw = currentYaw + diffYaw;
        float bestPitch = currentPitch + diffPitch;
        final MovingObjectPosition objectPosition3 = rayCast(1.0f, new float[] { bestYaw, bestPitch });
        float yawSpeed = Math.abs(bestYaw % 360.0f - currentYaw % 360.0f);
        float perfectYaw = basicRotation(entity, bestYaw, bestPitch, false)[0];
        double bestYawRotationDistance = Math.abs(bestYaw - perfectYaw);
        float pitchSpeed = Math.abs(bestPitch % 360.0f - currentPitch % 360.0f);
        float perfectPitch = basicRotation(entity, bestYaw, bestPitch, false)[1];
        double bestPitchRotationDistance = Math.abs(bestPitch - perfectPitch);
        final boolean targetIsMoving = Math.abs(entity.posX - entity.lastTickPosX) > 0.01 || Math.abs(entity.posZ - entity.lastTickPosZ) > 0.01;
        if (yawSpeed > 0.5f && targetIsMoving) {
            double correctYaw = ACCURATE_ROTATION_YAW_LEVEL / 0.25 / 0.8;
            if (bestYawRotationDistance / 0.8 < 2.0) {
                correctYaw += 2.0 - bestYawRotationDistance / 0.8;
            }
            bestYaw = (float)((RandomUtil.nextInt(0, 100000) % 2 == 0) ? (bestYaw - correctYaw) : (bestYaw + correctYaw));
        }
        while (bestPitch == perfectPitch) {
            bestPitch += (float)(RandomUtil.nextFloat(-1.0f, 1.0f) / 4.0f + RandomUtil.nextFloat(-1.0f, 1.0f) / 4.0f + MathHelper.clamp_float((float)ThreadLocalRandom.current().nextGaussian(), -1.0f, 1.0f) / 4.0f + RandomUtil.randomSin() / 4.0);
        }
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
            if (entityLivingBase.hurtTime <= 4) {
                final MovingObjectPosition objectPosition4 = rayCast(1.0f, new float[] { bestYaw, bestPitch });
                if (objectPosition3 == null || objectPosition4 == null || objectPosition4.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || objectPosition3.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {}
            }
        }
        yawSpeed = Math.abs(bestYaw % 360.0f - currentYaw % 360.0f);
        perfectYaw = basicRotation(entity, bestYaw, bestPitch, false)[0];
        bestYawRotationDistance = Math.abs(bestYaw - perfectYaw);
        pitchSpeed = Math.abs(bestPitch % 360.0f - currentPitch % 360.0f);
        for (perfectPitch = basicRotation(entity, bestYaw, bestPitch, false)[1], bestPitchRotationDistance = Math.abs(bestPitch - perfectPitch); bestPitchRotationDistance == 0.0; bestPitchRotationDistance = Math.abs(bestPitch - perfectPitch)) {
            bestPitch += (float)(RandomUtil.nextFloat(-1.0f, 1.0f) / 4.0f + RandomUtil.nextFloat(-1.0f, 1.0f) / 4.0f + MathHelper.clamp_float((float)ThreadLocalRandom.current().nextGaussian(), -1.0f, 1.0f) / 4.0f + RandomUtil.randomSin() / 4.0);
        }
        if (yawSpeed > 0.5f) {
            if (targetIsMoving) {
                ACCURATE_ROTATION_YAW_LEVEL += 2.0 - bestYawRotationDistance / 0.8;
                ACCURATE_ROTATION_YAW_LEVEL = Math.max(0.0, ACCURATE_ROTATION_YAW_LEVEL);
                final int suspiciousLevel = (int)ACCURATE_ROTATION_YAW_LEVEL;
                if (suspiciousLevel > 12) {
                    ++ACCURATE_ROTATION_YAW_VL;
                    if (ACCURATE_ROTATION_YAW_VL > 3.0) {
                        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Too accurate yaw rotation §7(§c" + bestYawRotationDistance + " §7| " + ACCURATE_ROTATION_YAW_LEVEL + " §7| " + yawSpeed + ") "));
                    }
                }
            }
        }
        else if (ACCURATE_ROTATION_YAW_VL > 0.0) {
            ACCURATE_ROTATION_YAW_VL -= 1.0E-5;
        }
        if (pitchSpeed > 0.5f) {
            if (targetIsMoving && bestPitchRotationDistance == 0.0 && currentPitch != bestPitch) {
                ACCURATE_ROTATION_PITCH_LEVEL += 2.0 - bestPitchRotationDistance / 0.8;
                ACCURATE_ROTATION_PITCH_LEVEL = Math.max(0.0, ACCURATE_ROTATION_PITCH_LEVEL);
                final int suspiciousLevel = (int)ACCURATE_ROTATION_PITCH_LEVEL;
                if (suspiciousLevel > 8) {
                    ++ACCURATE_ROTATION_PITCH_VL;
                    if (ACCURATE_ROTATION_PITCH_VL > 3.0) {
                        Utils.sendMessage("Too accurate pitch rotation §7(§c" + bestPitchRotationDistance + " §7| " + ACCURATE_ROTATION_PITCH_LEVEL + " §7| " + yawSpeed + ") ");
                    }
                }
            }
        }
        else if (ACCURATE_ROTATION_PITCH_VL > 0.0) {
            ACCURATE_ROTATION_PITCH_VL -= 1.0E-5;
        }
        lastTarget = entity;
        return new float[] { bestYaw, bestPitch };
    }

    public static float updateRotation(final float current, final float calc, final float maxDelta) {
        float f = MathHelper.wrapAngleTo180_float(calc - current);
        if (f > maxDelta) {
            f = maxDelta;
        }
        if (f < -maxDelta) {
            f = -maxDelta;
        }
        return current + f;
    }


    public static float interpolateRotation(final float current, final float predicted, float percentage) {
        final float f = MathHelper.wrapAngleTo180_float(predicted - current);
        if (f <= 10.0f && f >= -10.0f) {
            percentage = 1.0f;
        }
        return current + percentage * f;
    }

    private static double[] heuristics(final Entity entity, final double[] xyz) {
        final double boxSize = 0.2;
        final float f11 = entity.getCollisionBorderSize();
        final double minX = MathHelper.clamp_double(xyz[0] - boxSize, entity.getEntityBoundingBox().minX - f11, entity.getEntityBoundingBox().maxX + f11);
        final double minY = MathHelper.clamp_double(xyz[1] - boxSize, entity.getEntityBoundingBox().minY - f11, entity.getEntityBoundingBox().maxY + f11);
        final double minZ = MathHelper.clamp_double(xyz[2] - boxSize, entity.getEntityBoundingBox().minZ - f11, entity.getEntityBoundingBox().maxZ + f11);
        final double maxX = MathHelper.clamp_double(xyz[0] + boxSize, entity.getEntityBoundingBox().minX - f11, entity.getEntityBoundingBox().maxX + f11);
        final double maxY = MathHelper.clamp_double(xyz[1] + boxSize, entity.getEntityBoundingBox().minY - f11, entity.getEntityBoundingBox().maxY + f11);
        final double maxZ = MathHelper.clamp_double(xyz[2] + boxSize, entity.getEntityBoundingBox().minZ - f11, entity.getEntityBoundingBox().maxZ + f11);
        xyz[0] = MathHelper.clamp_double(xyz[0] + RandomUtil.randomSin(), minX, maxX);
        xyz[1] = MathHelper.clamp_double(xyz[1] + RandomUtil.randomSin(), minY, maxY);
        xyz[2] = MathHelper.clamp_double(xyz[2] + RandomUtil.randomSin(), minZ, maxZ);
        return xyz;
    }

    public static Vec3 getBestHitVec(final Entity entity) {
        final Vec3 positionEyes = mc.thePlayer.getPositionEyes(1.0f);
        final float f11 = entity.getCollisionBorderSize();
        final AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox().expand(f11, f11, f11);
        final double ex = MathHelper.clamp_double(positionEyes.xCoord, entityBoundingBox.minX, entityBoundingBox.maxX);
        final double ey = MathHelper.clamp_double(positionEyes.yCoord, entityBoundingBox.minY, entityBoundingBox.maxY);
        final double ez = MathHelper.clamp_double(positionEyes.zCoord, entityBoundingBox.minZ, entityBoundingBox.maxZ);
        return new Vec3(ex, ey, ez);
    }

    public static Vec3 getBestHitVec(final Entity entity, final float partialTicks) {
        final Vec3 positionEyes = mc.thePlayer.getPositionEyes(partialTicks);
        final float f11 = entity.getCollisionBorderSize();
        final double x = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        final double y = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
        final double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
        final float width = entity.width / 2.0f;
        AxisAlignedBB entityBoundingBox = entityBoundingBox = new AxisAlignedBB(x - width, y, z - width, x + width, y + entity.height, z + width);
        final double ex = MathHelper.clamp_double(positionEyes.xCoord, entityBoundingBox.minX, entityBoundingBox.maxX);
        final double ey = MathHelper.clamp_double(positionEyes.yCoord, entityBoundingBox.minY, entityBoundingBox.maxY);
        final double ez = MathHelper.clamp_double(positionEyes.zCoord, entityBoundingBox.minZ, entityBoundingBox.maxZ);
        return new Vec3(ex, ey, ez);
    }

    public static float[] mouseSens(float yaw, float pitch, final float lastYaw, final float lastPitch) {
        if (mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        if (yaw == lastYaw && pitch == lastPitch) {
            return new float[] { yaw, pitch };
        }
        final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float f2 = f1 * f1 * f1 * 8.0f;
        final int deltaX = (int)((6.667 * yaw - 6.667 * lastYaw) / f2);
        final int deltaY = (int)((6.667 * pitch - 6.667 * lastPitch) / f2) * -1;
        final float f3 = deltaX * f2;
        final float f4 = deltaY * f2;
        yaw = (float)(lastYaw + f3 * 0.15);
        final float f5 = (float)(lastPitch - f4 * 0.15);
        pitch = MathHelper.clamp_float(f5, -90.0f, 90.0f);
        return new float[] { yaw, pitch };
    }

    public static float[] basicRotation(final Entity entity, final float currentYaw, final float currentPitch, final boolean random) {
        final Vec3 ePos = getBestHitVec(entity);
        final double x = ePos.xCoord - mc.thePlayer.posX;
        final double y = ePos.yCoord - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double z = ePos.zCoord - mc.thePlayer.posZ;
        final float calcYaw = (float)(MathHelper.atan2(z, x) * 180.0 /3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679 - 90.0);
        final float calcPitch = (float)(-(MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0 /3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679));
        float yaw = updateRotation(currentYaw, calcYaw, 180.0f);
        float pitch = updateRotation(currentPitch, calcPitch, 180.0f);
        if (random) {
            yaw += (float)ThreadLocalRandom.current().nextGaussian();
            pitch += (float) ThreadLocalRandom.current().nextGaussian();
        }
        return mouseSens(yaw, pitch, currentYaw, currentPitch);
    }

    public static void checkRotationAnalysis(final EntityLivingBase target, final float yaw, final float pitch, final float prevYaw, final float prevPitch) {
        if (target == null) {
            ACCURATE_ROTATION_YAW_LEVEL1 = 0.0;
            ACCURATE_ROTATION_YAW_VL1 = 0.0;
            ACCURATE_ROTATION_PITCH_LEVEL1 = 0.0;
            ACCURATE_ROTATION_PITCH_VL1 = 0.0;
            return;
        }
        final float yawSpeed = Math.abs(yaw % 360.0f - prevYaw % 360.0f);
        final float perfectYaw = basicRotation(target, yaw, pitch, false)[0];
        final double bestYawRotationDistance = Math.abs(yaw - perfectYaw);
        final float pitchSpeed = Math.abs(pitch % 360.0f - prevPitch % 360.0f);
        final float perfectPitch = basicRotation(target, yaw, pitch, false)[1];
        final double bestPitchRotationDistance = Math.abs(pitch - perfectPitch);
        final boolean targetIsMoving = Math.abs(target.posX - target.lastTickPosX) > 0.01 || Math.abs(target.posZ - target.lastTickPosZ) > 0.01;
        if (yawSpeed > 0.5f) {
            if (targetIsMoving) {
                ACCURATE_ROTATION_YAW_LEVEL1 += 2.0 - bestYawRotationDistance / 0.8;
                ACCURATE_ROTATION_YAW_LEVEL1 = Math.max(0.0, ACCURATE_ROTATION_YAW_LEVEL1);
                final int suspiciousLevel = (int)ACCURATE_ROTATION_YAW_LEVEL1;
                if (suspiciousLevel > 12) {
                    ++ACCURATE_ROTATION_YAW_VL1;
                    if (ACCURATE_ROTATION_YAW_VL1 > 3.0) {}
                }
            }
        }
        else if (ACCURATE_ROTATION_YAW_VL1 > 0.0) {
            ACCURATE_ROTATION_YAW_VL1 -= 1.0E-5;
        }
        if (pitchSpeed > 0.5f) {
            if (targetIsMoving && bestPitchRotationDistance == 0.0 && prevPitch != pitch) {
                ACCURATE_ROTATION_PITCH_LEVEL1 += 2.0 - bestPitchRotationDistance / 0.8;
                ACCURATE_ROTATION_PITCH_LEVEL1 = Math.max(0.0, ACCURATE_ROTATION_PITCH_LEVEL1);
                final int suspiciousLevel = (int)ACCURATE_ROTATION_PITCH_LEVEL1;
                if (suspiciousLevel > 8) {
                    ++ACCURATE_ROTATION_PITCH_VL1;
                    if (ACCURATE_ROTATION_PITCH_VL1 > 3.0) {}
                }
            }
        }
        else if (ACCURATE_ROTATION_PITCH_VL1 > 0.0) {
            ACCURATE_ROTATION_PITCH_VL1 -= 1.0E-5;
        }
    }

    public static float[] backRotate(final float yawSpeed, final float pitchSpeed, final float currentYaw, final float currentPitch, final float calcYaw, final float calcPitch) {
        float yaw = updateRotation(currentYaw, calcYaw + RandomUtil.nextFloat(-2.0f, 2.0f), 20.0f + RandomUtil.nextFloat(0.0f, 15.0f));
        float pitch = updateRotation(currentPitch, calcPitch + RandomUtil.nextFloat(-2.0f, 2.0f), 10.0f + RandomUtil.nextFloat(0.0f, 15.0f));
        yaw += (float)(ThreadLocalRandom.current().nextGaussian() * 0.6);
        pitch += (float)(ThreadLocalRandom.current().nextGaussian() * 0.6);
        if (mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        final float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float f2 = f1 * f1 * f1 * 8.0f;
        final int deltaX = (int)((6.667 * yaw - 6.6666667 * currentYaw) / f2);
        final int deltaY = (int)((6.667 * pitch - 6.666667 * currentPitch) / f2) * -1;
        final float f3 = deltaX * f2;
        final float f4 = deltaY * f2;
        yaw = (float)(currentYaw + f3 * 0.15);
        final float f5 = (float)(currentPitch - f4 * 0.15);
        pitch = MathHelper.clamp_float(f5, -90.0f, 90.0f);
        return new float[] { yaw, pitch };
    }
    
    public static float[] faceEntityCustom(final Entity entity, float yawSpeed, float pitchSpeed, final float currentYaw, final float currentPitch, final String randomMode, final boolean interpolateRotation, final boolean smartAim, final boolean stopOnTarget, final float randomStrength, final Vec3 best, final boolean throughWalls, final boolean advancedRots, final boolean heuristics, final boolean intave, final boolean bestHitVec) {
        if (smartAim && !throughWalls) {
            double ePosX = best.xCoord;
            double ePosY = best.yCoord;
            double ePosZ = best.zCoord;
            if (heuristics) {
                final double[] xyz = heuristics(entity, new double[] { ePosX, ePosY, ePosZ });
                ePosX = xyz[0];
                ePosY = xyz[1];
                ePosZ = xyz[2];
            }
            x = ePosX - mc.thePlayer.posX;
            y = ePosY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
            z = ePosZ - mc.thePlayer.posZ;
        }
        else {
            double ex = entity.posX;
            double ey = entity.posY + entity.getEyeHeight();
            double ez = entity.posZ;
            if (bestHitVec) {
                final Vec3 entityVec = getBestHitVec(entity);
                ex = entityVec.xCoord;
                ey = entityVec.yCoord;
                ez = entityVec.zCoord;
            }
            if (heuristics) {
                final double[] xyz = heuristics(entity, new double[] { ex, ey, ez });
                ex = xyz[0];
                ey = xyz[1];
                ez = xyz[2];
            }
            x = ex - mc.thePlayer.posX;
            y = ey - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
            z = ez - mc.thePlayer.posZ;
        }
        final float calcYaw = (float)(Math.atan2(z, x) * 180.0 / 3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679 - 90.0);
        final float calcPitch = (float)(-(MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0 /3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679));
        if (stopOnTarget && mc.objectMouseOver != null && mc.objectMouseOver.entityHit == entity) {
            yawSpeed = 0.0f;
            pitchSpeed = 0.0f;
        }
        final double diffYaw = MathHelper.wrapAngleTo180_float(calcYaw - currentYaw);
        final double diffPitch = MathHelper.wrapAngleTo180_float(calcPitch - currentPitch);
        float yaw;
        float pitch;
        if (interpolateRotation) {
            yaw = interpolateRotation(currentYaw, calcYaw, yawSpeed / RandomUtil.nextFloat(170.0f, 180.0f));
            pitch = interpolateRotation(currentPitch, calcPitch, pitchSpeed / RandomUtil.nextFloat(170.0f, 180.0f));
        }
        else if (heuristics) {
            final float[] f = testRots(currentYaw, currentPitch, calcYaw, calcPitch, entity, yawSpeed, pitchSpeed, best, smartAim, throughWalls);
            yaw = f[0];
            pitch = f[1];
        }
        else {
            yaw = updateRotation(currentYaw, calcYaw, yawSpeed);
            pitch = updateRotation(currentPitch, calcPitch, pitchSpeed);
        }
        switch (randomMode) {
            case "Basic": {
                yaw += (float)(intave ? (RandomUtil.nextSecureFloat(1.0, 2.0) * Math.sin(pitch *3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679) * randomStrength) : (ThreadLocalRandom.current().nextGaussian() * randomStrength));
                pitch += (float)(intave ? (RandomUtil.nextSecureFloat(1.0, 2.0) * Math.sin(yaw *3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679) * randomStrength) : (ThreadLocalRandom.current().nextGaussian() * randomStrength));
                break;
            }
            case "OnlyRotation": {
                if (-yawSpeed > diffYaw || diffYaw > yawSpeed || -pitchSpeed > diffPitch || diffPitch > pitchSpeed) {
                    yaw += (float)(intave ? (RandomUtil.nextSecureFloat(1.0, 2.0) * Math.sin(pitch *3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679) * randomStrength) : (ThreadLocalRandom.current().nextGaussian() * randomStrength));
                    pitch += (float)(intave ? (RandomUtil.nextSecureFloat(1.0, 2.0) * Math.sin(yaw *3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679) * randomStrength) : (ThreadLocalRandom.current().nextGaussian() * randomStrength));
                    break;
                }
                break;
            }
            case "Doubled": {
                final float random1 = RandomUtil.nextSecureFloat(-randomStrength, randomStrength);
                final float random2 = RandomUtil.nextSecureFloat(-randomStrength, randomStrength);
                final float random3 = RandomUtil.nextSecureFloat(-randomStrength, randomStrength);
                final float random4 = RandomUtil.nextSecureFloat(-randomStrength, randomStrength);
                yaw += RandomUtil.nextSecureFloat(Math.min(random1, random2), Math.max(random1, random2));
                pitch += RandomUtil.nextSecureFloat(Math.min(random3, random4), Math.max(random3, random4));
                break;
            }
        }
        if (advancedRots) {
            pitch += (float)(Math.sin(0.06981317007977318 * (updateRotation(currentYaw, calcYaw, 180.0f) - yaw)) * 8.0);
        }
        if (mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        final float f2 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float f3 = f2 * f2 * f2 * 8.0f;
        final int deltaX = (int)((6.667 * yaw - 6.667 * currentYaw) / f3);
        final int deltaY = (int)((6.667 * pitch - 6.667 * currentPitch) / f3) * -1;
        final float f4 = deltaX * f3;
        final float f5 = deltaY * f3;
        yaw = (float)(currentYaw + f4 * 0.15);
        final float f6 = (float)(currentPitch - f5 * 0.15);
        pitch = MathHelper.clamp_float(f6, -90.0f, 90.0f);
        lastX = entity.posX;
        lastY = entity.posY;
        lastZ = entity.posZ;
        if (entity instanceof EntityLivingBase) {
            checkRotationAnalysis((EntityLivingBase)entity, yaw, pitch, currentYaw, currentPitch);
        }
        return new float[] { yaw, pitch };
    }
}
