package neo.module.impl.movement.mode.speed;

import neo.event.StrafeEvent;

import neo.module.impl.movement.BHop;
import neo.util.player.move.MoveUtil;
import neo.util.world.block.BlockUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static neo.Neo.mc;

public class VulcanSpeed {
    private static double lastTickPosX = 0;
    private static double lastLastTickPosX = 0;
    private static double lastTickPosZ = 0;
    private static double lastLastTickPosZ = 0;

    public static void onTick(TickEvent e) {
        if (mc.thePlayer == null) return;
        lastLastTickPosX = lastTickPosX;
        lastTickPosX = mc.thePlayer.posX;

        lastLastTickPosZ = lastTickPosZ;
        lastTickPosZ = mc.thePlayer.posZ;
    }


    public static void VulcanSpeed(StrafeEvent event) {
        if (!MoveUtil.isMoving()) return;

        if (MoveUtil.speed() < 0.22) {
            MoveUtil.strafe(0.22);
        }


        double speed = Math.hypot(
                mc.thePlayer.motionX - (mc.thePlayer.lastTickPosX - lastLastTickPosX),
                mc.thePlayer.motionZ - (mc.thePlayer.lastTickPosZ - lastLastTickPosZ)
        );

        if (speed < 0.022) {
            MoveUtil.strafea();
        }

        switch (BHop.offGroundTicks) {
            case 0:
                mc.thePlayer.jump();

                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    int amp = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                    double boost = 0.06 * (1 + amp) + 0.487;
                    if (MoveUtil.speed() < boost) {
                        MoveUtil.strafe(boost);
                    }
                } else {
                    if (MoveUtil.speed() < 0.487) {
                        MoveUtil.strafe(0.487);
                    }
                }
                break;

            case 9:
                if (!(BlockUtils.blockRelativeToPlayer(0, mc.thePlayer.motionY, 0) instanceof BlockAir)) {
                    MoveUtil.strafea();
                }

                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    int amp = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                    double targetSpeed;

                    if (amp + 1 >= 2) {
                        targetSpeed = 0.06 * (1 + amp) + 0.46;
                    } else if (amp + 1 == 1) {
                        targetSpeed = 0.06 * (1 + amp) + 0.385;
                    } else {
                        targetSpeed = 0.299;
                    }

                    if (MoveUtil.speed() < targetSpeed) {
                        MoveUtil.strafe(targetSpeed);
                    }
                }
                break;

            case 1:
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    int amp = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                    double targetSpeed;

                    if (amp + 1 >= 2) {
                        targetSpeed = 0.06 * (1 + amp) + 0.487;
                    } else if (amp + 1 == 1) {
                        targetSpeed = 0.06 * (1 + amp) + 0.41;
                    } else {
                        targetSpeed = 0.3355;
                    }

                    if (MoveUtil.speed() < targetSpeed) {
                        MoveUtil.strafe(targetSpeed);
                    } else {
                        MoveUtil.strafea();
                    }
                }
                break;

            case 4:
                mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 1);
                break;
        }
    }
}
