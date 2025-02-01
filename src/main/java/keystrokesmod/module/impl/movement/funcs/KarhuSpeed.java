package keystrokesmod.module.impl.movement.funcs;

import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.util.MathHelper;
import static keystrokesmod.Raven.mc;
import static keystrokesmod.module.impl.movement.BHop.autoJump;
import static keystrokesmod.utility.MoveUtil.direction;

public class KarhuSpeed {

    private static int LowHopTimes;
    private static boolean FullJumpRunning = false;
    private static int Ticks11 = 0;

    public static void KarhuSpeed() {
        Ticks11++;
        Utils.getTimer().timerSpeed = 1.01f;

        if (Ticks11 >= 11) {
            FullJumpRunning = false;
            Ticks11 = 0;
        }

        if (!MoveUtil.isMoving() && !FullJumpRunning) return;
        mc.thePlayer.setSprinting(true);

        if (Utils.isMoving() && mc.thePlayer.onGround) {
            MoveUtil.strafe(0.453, mc.thePlayer);
            if (Utils.getHorizontalSpeed() < (MoveUtil.baseSprintMaxSpeed - 0.03)) {
                final double yaw = direction();
                mc.thePlayer.motionX -= MathHelper.sin((float) yaw) + 0.012;
                mc.thePlayer.motionZ += MathHelper.cos((float) yaw) + 0.012;
            }

        }

        if (Utils.isMoving() && mc.thePlayer.onGround && autoJump.isToggled()) {
            if (LowHopTimes < 4) {
                mc.thePlayer.motionY = 0.37f;
                float f = mc.thePlayer.rotationYaw * 0.017453292f;
                mc.thePlayer.motionX -= MathHelper.sin(f) * 0.012;
                mc.thePlayer.motionZ += MathHelper.cos(f) * 0.012;
                LowHopTimes++;
            }

            if (Utils.isMoving() && !mc.thePlayer.onGround) {
                double moveSpeed = MoveUtil.getBaseMoveSpeed() * 1.17f;
                MoveUtil.strafe4(moveSpeed * (MoveUtil.BUNNY_SLOPE * 0.96));
                if (Utils.getHorizontalSpeed() < (MoveUtil.getBaseMoveSpeed() - 0.005)) {
                    final double yaw = direction();
                    mc.thePlayer.motionX -= MathHelper.sin((float) yaw) + 0.0029;
                    mc.thePlayer.motionZ += MathHelper.cos((float) yaw) + 0.0029;
                }
            }

            if (LowHopTimes == 3) {
                KarhuSpeed2();
            }

        }
    }

    public static void KarhuSpeed2() {
        final double yaw = direction();
        mc.thePlayer.setSprinting(true);

        if (Utils.getHorizontalSpeed() < (MoveUtil.getBaseMoveSpeed() - 0.05)) {
            mc.thePlayer.motionX = -MathHelper.sin((float) yaw) + 0.035;
            mc.thePlayer.motionZ = MathHelper.cos((float) yaw) + 0.035;
        } else {
            Utils.setSpeed(MoveUtil.getBaseMoveSpeed());
        }

        FullJumpRunning = true;
        mc.thePlayer.jump();
        LowHopTimes = 0;
    }
}
