package neo.util.player.move;

import neo.event.PreMotionEvent;
import neo.module.impl.client.Settings;
import neo.util.Utils;
import neo.util.aim.Vec2;
import neo.util.world.block.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class RotationUtils {
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static float renderPitch;
    public static float prevRenderPitch;
    public static float renderYaw;
    public static float prevRenderYaw;

    public static void setRenderYaw(float yaw) {
        mc.thePlayer.rotationYawHead = yaw;
        if (Settings.rotateBody.isToggled() && Settings.fullBody.isToggled()) {
            mc.thePlayer.renderYawOffset = yaw;
        }
    }

    public static float[] getRotations(BlockPos blockPos, final float n, final float n2) {
        final float[] array = getRotations(blockPos);
        return fixRotation(array[0], array[1], n, n2);
    }

    public static float[] getRotations(final BlockPos blockPos) {
        final double n = blockPos.getX() + 0.45 - mc.thePlayer.posX;
        final double n2 = blockPos.getY() + 0.45 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double n3 = blockPos.getZ() + 0.45 - mc.thePlayer.posZ;
        return new float[] { mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float)(Math.atan2(n3, n) * 57.295780181884766) - 90.0f - mc.thePlayer.rotationYaw), clampTo90(mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float)(-(Math.atan2(n2, MathHelper.sqrt_double(n * n + n3 * n3)) * 57.295780181884766)) - mc.thePlayer.rotationPitch)) };
    }

    public static float interpolateValue(float tickDelta, float old, float newFloat) {
        return old + (newFloat - old) * tickDelta;
    }

    public static float[] getRotations(Entity entity, final float n, final float n2) {
        final float[] array = getRotations(entity);
        if (array == null) {
            return null;
        }
        return fixRotation(array[0], array[1], n, n2);
    }

    public static double distanceFromYaw(final Entity entity, final boolean b) {
        return Math.abs(MathHelper.wrapAngleTo180_double(i(entity.posX, entity.posZ) - ((b && PreMotionEvent.setRenderYaw()) ? RotationUtils.renderYaw : mc.thePlayer.rotationYaw)));
    }

    public static float i(final double n, final double n2) {
        return (float)(Math.atan2(n - mc.thePlayer.posX, n2 - mc.thePlayer.posZ) * 57.295780181884766 * -1.0);
    }

    public static boolean inRange(final BlockPos blockPos, final double n) {
        final float[] array = RotationUtils.getRotations(blockPos);
        final Vec3 getPositionEyes = mc.thePlayer.getPositionEyes(1.0f);
        final float n2 = -array[0] * 0.017453292f;
        final float n3 = -array[1] * 0.017453292f;
        final float cos = MathHelper.cos(n2 - 3.1415927f);
        final float sin = MathHelper.sin(n2 - 3.1415927f);
        final float n4 = -MathHelper.cos(n3);
        final Vec3 vec3 = new Vec3(sin * n4, MathHelper.sin(n3), cos * n4);
        Block block = BlockUtils.getBlock(blockPos);
        IBlockState blockState = BlockUtils.getBlockState(blockPos);
        if (block != null && blockState != null) {
            AxisAlignedBB boundingBox = block.getCollisionBoundingBox(mc.theWorld, blockPos, blockState);
            if (boundingBox != null) {
                Vec3 targetVec = getPositionEyes.addVector(vec3.xCoord * n, vec3.yCoord * n, vec3.zCoord * n);
                MovingObjectPosition intercept = boundingBox.calculateIntercept(getPositionEyes, targetVec);
                return intercept != null;
            }
        }
        return false;
    }

    public static float[] getRotations(final Entity entity) {
        if (entity == null) {
            return null;
        }
        final double n = entity.posX - mc.thePlayer.posX;
        final double n2 = entity.posZ - mc.thePlayer.posZ;
        double n3;
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            n3 = entityLivingBase.posY + entityLivingBase.getEyeHeight() * 0.9 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        } else {
            n3 = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }
        return new float[]{mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float) (Math.atan2(n2, n) * 57.295780181884766) - 90.0f - mc.thePlayer.rotationYaw), clampTo90(mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float) (-(Math.atan2(n3, MathHelper.sqrt_double(n * n + n2 * n2)) * 57.295780181884766)) - mc.thePlayer.rotationPitch) + 3.0f)};
    }

    public static float[] getRotationsPredicted(final Entity entity, final int ticks) {
        if (entity == null) {
            return null;
        }
        if (ticks == 0) {
            return getRotations(entity);
        }
        double posX = entity.posX;
        final double posY = entity.posY;
        double posZ = entity.posZ;
        final double n2 = posX - entity.lastTickPosX;
        final double n3 = posZ - entity.lastTickPosZ;
        for (int i = 0; i < ticks; ++i) {
            posX += n2;
            posZ += n3;
        }
        final double n4 = posX - mc.thePlayer.posX;
        double n5;
        if (entity instanceof EntityLivingBase) {
            n5 = posY + entity.getEyeHeight() * 0.9 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }
        else {
            n5 = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }
        final double n6 = posZ - mc.thePlayer.posZ;
        return new float[] { mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float)(Math.atan2(n6, n4) * 57.295780181884766) - 90.0f - mc.thePlayer.rotationYaw), clampTo90(mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float)(-(Math.atan2(n5, MathHelper.sqrt_double(n4 * n4 + n6 * n6)) * 57.295780181884766)) - mc.thePlayer.rotationPitch) + 3.0f) };
    }

    public static float clampTo90(final float n) {
        return MathHelper.clamp_float(n, -90.0f, 90.0f);
    }

    public static float[] fixRotation(float n, float n2, final float n3, final float n4) {
        float n5 = n - n3;
        final float abs = Math.abs(n5);
        final float n7 = n2 - n4;
        final float n8 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final double n9 = n8 * n8 * n8 * 1.2;
        final float n10 = (float) (Math.round((double) n5 / n9) * n9);
        final float n11 = (float) (Math.round((double) n7 / n9) * n9);
        n = n3 + n10;
        n2 = n4 + n11;
        if (abs >= 1.0f) {
            final int n12 = (int) Settings.randomYawFactor.getInput();
            if (n12 != 0) {
                final int n13 = n12 * 100 + Utils.randomizeInt(-30, 30);
                n += (float) (Utils.randomizeInt(-n13, n13) / 100.0);
            }
        } else if (abs <= 0.04) {
            n += (float) ((abs > 0.0f) ? 0.01 : -0.01);
        }
        return new float[]{n, clampTo90(n2)};
    }

    public static float angle(final double n, final double n2) {
        return (float) (Math.atan2(n - mc.thePlayer.posX, n2 - mc.thePlayer.posZ) * 57.295780181884766 * -1.0);
    }

    public static MovingObjectPosition rayCast(final double distance, final float yaw, final float pitch) {
        final Vec3 getPositionEyes = mc.thePlayer.getPositionEyes(1.0f);
        final float n4 = -yaw * 0.017453292f;
        final float n5 = -pitch * 0.017453292f;
        final float cos = MathHelper.cos(n4 - 3.1415927f);
        final float sin = MathHelper.sin(n4 - 3.1415927f);
        final float n6 = -MathHelper.cos(n5);
        final Vec3 vec3 = new Vec3(sin * n6, MathHelper.sin(n5), cos * n6);
        return mc.theWorld.rayTraceBlocks(getPositionEyes, getPositionEyes.addVector(vec3.xCoord * distance, vec3.yCoord * distance, vec3.zCoord * distance), false, false, true);
    }

    public static MovingObjectPosition rayTraceCustom(double blockReachDistance, float yaw, float pitch) {
        final Vec3 vec3 = mc.thePlayer.getPositionEyes(1.0F);
        final Vec3 vec31 = getVectorForRotation(pitch, yaw);
        final Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
        return mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
    }

    public static Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public static float normalize(float yaw, float min, float max) {
        yaw %= 360.0F;
        if (yaw >= max) {
            yaw -= 360.0F;
        }
        if (yaw < min) {
            yaw += 360.0F;
        }

        return yaw;
    }

    public static Vec2 getRotations(EntityLivingBase theEntity, RotationAt rotationAt) {
        if (theEntity == null || rotationAt == null || mc.thePlayer == null) return new Vec2(0, 0);

        float rotationTypeValue = 3;

        double xDistance;
        double zDistance;
        double yDistance;

        float yaw, pitch;
        if (rotationAt.equals(RotationAt.HEAD))
            rotationTypeValue = 1.314f;
        if (rotationAt.equals(RotationAt.LEGS))
            rotationTypeValue = 9.7236f;
        if (rotationAt.equals(RotationAt.FEET))
            rotationTypeValue = 194.472f;

        xDistance = theEntity.posX - mc.thePlayer.posX;
        zDistance = theEntity.posZ - mc.thePlayer.posZ;
        yDistance = theEntity.posY + (theEntity.getEyeHeight() - 0.1D) / rotationTypeValue - mc.thePlayer.posY - mc.thePlayer.getEyeHeight() / 1.4D;
        double angleHelper = MathHelper.sqrt_double(xDistance * xDistance + zDistance * zDistance);
        yaw = (float) Math.toDegrees(-Math.atan(xDistance / zDistance));
        pitch = (float) -Math.toDegrees(Math.atan(yDistance / angleHelper));
        double v = Math.toDegrees(Math.atan(zDistance / xDistance));
        if ((zDistance < 0.0D) && (xDistance < 0.0D)) {
            yaw = (float) (90.0D + v);
        } else if ((zDistance < 0.0D) && (xDistance > 0.0D)) {
            yaw = (float) (-90.0D + v);
        }

        return new Vec2(yaw, pitch >= 90 ? 90 : pitch <= -90 ? -90 : pitch);
    }

    public static Vec2 getRotations(EntityLivingBase theEntity, RotationAt rotationAt, boolean noParkinsons) {
        if (theEntity == null || rotationAt == null || mc.thePlayer == null) return new Vec2(0, 0);

        if (noParkinsons) {
            double dx = theEntity.posX - mc.thePlayer.posX;
            double dz = theEntity.posZ - mc.thePlayer.posZ;
            double dy = (theEntity.posY + theEntity.getEyeHeight()) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());

            double dist = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float) -Math.toDegrees(Math.atan2(dx, dz));
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

            pitch = Math.max(-90f, Math.min(90f, pitch));
            return new Vec2(yaw, pitch);
        }

        float rotationTypeValue = 3;
        if (rotationAt.equals(RotationAt.HEAD)) rotationTypeValue = 1.314f;
        if (rotationAt.equals(RotationAt.LEGS)) rotationTypeValue = 9.7236f;
        if (rotationAt.equals(RotationAt.FEET)) rotationTypeValue = 194.472f;

        double x = theEntity.posX - mc.thePlayer.posX;
        double z = theEntity.posZ - mc.thePlayer.posZ;
        double y = theEntity.posY + (theEntity.getEyeHeight() - 0.1D) / rotationTypeValue - mc.thePlayer.posY - mc.thePlayer.getEyeHeight() / 1.4D;

        double horizontalDist = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) Math.toDegrees(-Math.atan(x / z));
        float pitch = (float) -Math.toDegrees(Math.atan(y / horizontalDist));
        double v = Math.toDegrees(Math.atan(z / x));

        if ((z < 0.0D) && (x < 0.0D)) yaw = (float) (90.0D + v);
        else if ((z < 0.0D) && (x > 0.0D)) yaw = (float) (-90.0D + v);

        pitch = Math.max(-90f, Math.min(90f, pitch));
        return new Vec2(yaw, pitch);
    }


    @Contract("_, _ -> new")
    public static @NotNull neo.script.classes.Vec3 getNearestPoint(@NotNull AxisAlignedBB from, @NotNull neo.script.classes.Vec3 to) {
        double pointX, pointY, pointZ;
        if (to.x() >= from.maxX) {
            pointX = from.maxX;
        } else pointX = Math.max(to.x(), from.minX);
        if (to.y() >= from.maxY) {
            pointY = from.maxY;
        } else pointY = Math.max(to.y(), from.minY);
        if (to.z() >= from.maxZ) {
            pointZ = from.maxZ;
        } else pointZ = Math.max(to.z(), from.minZ);

        return new neo.script.classes.Vec3(pointX, pointY, pointZ);
    }

}

