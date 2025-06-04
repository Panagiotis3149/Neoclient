package neo.util.aim;

import akka.japi.Pair;
import neo.script.classes.Vec3;
import neo.util.player.move.MoveUtil;
import neo.util.player.move.PlayerRotation;
import neo.util.player.move.RotationUtils;
import neo.util.Utils;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static neo.Neo.mc;

public class AimSimulator {
    private double xRandom = 0;
    private double yRandom = 0;
    private double zRandom = 0;
    private long lastNoiseRandom = System.currentTimeMillis();
    private double lastNoiseDeltaX = 0;
    private double lastNoiseDeltaY = 0;
    private double lastNoiseDeltaZ = 0;
    private final List<AxisAlignedBB> boxHistory = new ArrayList<>(101);

    private final boolean nearest = false;
    private final double nearestAcc = 0.8;

    private final boolean lazy = false;
    private final double lazyAcc = 0.95;

    private final boolean noise = false;
    private final Pair<Float, Float> noiseRandom = new Pair<>(0.35F, 0.5F);
    private final double noiseSpeed = 1;
    private final long noiseDelay = 100;

    private final boolean delay = false;
    private final int delayTicks = 1;

    private final boolean scale = false;
    private final double scaleX = 1;
    private final double scaleY = 1;

    private final boolean offset = false;
    private final double offsetX = 0;
    private final double offsetY = 0;
    private final boolean offsetPre = true;

    @Getter
    private Vec3 hitPos = Vec3.ZERO;


    public @NotNull Pair<Float, Float> getRotation(@NotNull EntityLivingBase target) {
        AxisAlignedBB targetBox = target.getEntityBoundingBox();
        if (scale) {
            targetBox = targetBox.expand(
                    (targetBox.maxX - targetBox.minX) * (scaleX - 1),
                    (targetBox.maxY - targetBox.minY) * (scaleY - 1),
                    (targetBox.maxZ - targetBox.minZ) * (scaleX - 1)
            );
        }

        if (boxHistory.size() >= 101) {
            boxHistory.remove(boxHistory.size() - 1);
        }
        while (boxHistory.size() < 101) {
            boxHistory.add(0, targetBox);
        }

        float yaw, pitch;

        final double yDiff = target.posY - mc.thePlayer.posY;
        Vec3 targetPosition;

        AxisAlignedBB aimBox = delay ? boxHistory.get(delayTicks) : targetBox;
        if (nearest) {
            targetPosition = RotationUtils.getNearestPoint(aimBox, Utils.getEyePos());
            if (MoveUtil.isMoving() || MoveUtil.isMoving(target))
                targetPosition = targetPosition.add(Utils.randomizeDouble(nearestAcc - 1, 1 - nearestAcc) * 0.4, Utils.randomizeDouble(nearestAcc - 1, 1 - nearestAcc) * 0.4, Utils.randomizeDouble(nearestAcc - 1, 1 - nearestAcc) * 0.4);
        } else {
            targetPosition = new Vec3((aimBox.maxX + aimBox.minX) / 2, aimBox.minY + target.getEyeHeight() - 0.15, (aimBox.maxZ + aimBox.minZ) / 2);
        }

        if (offset && offsetPre) {
            targetPosition = targetPosition.add(offsetX, offsetY, offsetX);
        }

        if (yDiff >= 0 && lazy) {
            if (targetPosition.y() - yDiff > target.posY) {
                targetPosition = new Vec3(targetPosition.x(), targetPosition.y() - yDiff, targetPosition.z());
            } else {
                targetPosition = new Vec3(target.posX, target.posY + 0.2, target.posZ);
            }
            if (!target.onGround && (MoveUtil.isMoving() || MoveUtil.isMoving(target)))
                targetPosition.y += Utils.randomizeDouble(lazyAcc - 1, 1 - lazyAcc) * 0.4;
        }

        if (noise) {
            if (System.currentTimeMillis() - lastNoiseRandom >= noiseDelay) {
                xRandom = random(noiseRandom.first());
                yRandom = random(noiseRandom.second());
                zRandom = random(noiseRandom.first());
                lastNoiseRandom = System.currentTimeMillis();
            }

            lastNoiseDeltaX = rotMove(xRandom, lastNoiseDeltaX, noiseSpeed);
            lastNoiseDeltaY = rotMove(yRandom, lastNoiseDeltaY, noiseSpeed);
            lastNoiseDeltaZ = rotMove(zRandom, lastNoiseDeltaZ, noiseSpeed);

            targetPosition.x = normal(targetBox.maxX, targetBox.minX, targetPosition.x + lastNoiseDeltaX);
            targetPosition.y = normal(targetBox.maxY, targetBox.minY, targetPosition.y + lastNoiseDeltaY);
            targetPosition.z = normal(targetBox.maxZ, targetBox.minZ, targetPosition.z + lastNoiseDeltaZ);
        }

        if (offset && !offsetPre) {
            targetPosition = targetPosition.add(offsetX, offsetY, offsetX);
        }

        yaw = PlayerRotation.getYaw(targetPosition);
        pitch = PlayerRotation.getPitch(targetPosition);
        hitPos = targetPosition;

        return new Pair<>(yaw, pitch);
    }

    private static float random(double multiple) {
        return (float) ((Math.random() - 0.5) * 2 * multiple);
    }

    private static double normal(double max, double min, double current) {
        if (current >= max) return max;
        return Math.max(current, min);
    }

    public static float rotMove(double target, double current, double diff) {
        return rotMoveNoRandom((float) target, (float) current, (float) diff);
    }

    public static float rotMoveNoRandom(float target, float current, float diff) {
        float delta;
        if (target > current) {
            float dist1 = target - current;
            float dist2 = current + 360 - target;
            if (dist1 > dist2) {  // 另一边移动更近
                delta = -current - 360 + target;
            } else {
                delta = dist1;
            }
        } else if (target < current) {
            float dist1 = current - target;
            float dist2 = target + 360 - current;
            if (dist1 > dist2) {  // 另一边移动更近
                delta = current + 360 + target;
            } else {
                delta = -dist1;
            }
        } else {
            return current;
        }

        delta = RotationUtils.normalize(delta, -180, 180);

        if (Math.abs(delta) < 0.1 * Math.random() + 0.1) {
            return current;
        } else if (Math.abs(delta) <= diff) {
            return current + delta;
        } else {
            if (delta < 0) {
                return current - diff;
            } else if (delta > 0) {
                return current + diff;
            } else {
                return current;
            }
        }
    }

    public static boolean yawEquals(float yaw1, float yaw2) {
        return Math.abs(RotationUtils.normalize(yaw1, -180, 180) - RotationUtils.normalize(yaw2, -180, 180)) < 0.1;
    }

    public static boolean equals(@NotNull Vec2 rot1, @NotNull Vec2 rot2) {
        return yawEquals(rot1.x, rot2.x) && Math.abs(rot1.y - rot2.y) < 0.1;
    }
}