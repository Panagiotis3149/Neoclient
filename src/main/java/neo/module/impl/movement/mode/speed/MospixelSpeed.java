package neo.module.impl.movement.mode.speed;

import neo.module.ModuleManager;
import neo.util.player.move.MoveUtil;
import neo.util.Utils;
import net.minecraft.potion.Potion;


import static neo.module.impl.movement.BHop.offGroundTicks;
import static neo.util.Utils.jumpDown;
import static neo.util.Utils.mc;

public class MospixelSpeed {

    private static boolean reset;
    public static double speed;

    public static void MospixelSpeed() {
        Utils.resetTimer();
        boolean spot = mc.thePlayer.isPotionActive(Potion.moveSpeed);
        final double base = MoveUtil.getAllowedHorizontalDistance();
        if (!MoveUtil.isMoving()) return;
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = ModuleManager.scaffold.isEnabled() ? 0.38f :
                    jumpDown() ? 0.42f : 0.4f;
        }
        if (mc.thePlayer.hurtTime > 0) {
            speed = MoveUtil.getSpeed() + 0.09f;
            mc.thePlayer.posY += 0.2;
        } else if (mc.thePlayer.hurtTime == 0 && offGroundTicks == 6) {
            mc.thePlayer.motionY = -0.09800000190734863;
        }

        if (offGroundTicks == 3 && spot) mc.thePlayer.motionY -= 0.07;
        if (offGroundTicks == 4 && !spot) mc.thePlayer.motionY += 0.01;
        if (MoveUtil.isMoving()) {
            switch (offGroundTicks) {
                case 0:
                    speed = base * 1.625f;
                    break;

                case 1:
                    speed -= 0.62 * (speed - base);
                    break;

                default:
                    speed -= speed / MoveUtil.BUNNY_FRICTION;
                    break;
            }

            Utils.getTimer().timerSpeed = 1.05f;
            reset = false;
            Utils.setSpeed(speed);
        } else if (!reset) {
            speed = MoveUtil.getAllowedHorizontalDistance();
            Utils.getTimer().timerSpeed = 1;
            reset = true;
        }
    }
}

