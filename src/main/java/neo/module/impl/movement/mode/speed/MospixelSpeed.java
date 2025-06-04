package neo.module.impl.movement.mode.speed;

import neo.module.ModuleManager;
import neo.util.player.move.MoveUtil;
import neo.util.Utils;
import net.minecraft.potion.Potion;


import static neo.module.impl.movement.BHop.offGroundTicks;
import static neo.util.Utils.jumpDown;
import static neo.util.Utils.mc;

public class MospixelSpeed {
    public static void MospixelSpeed() {

        Utils.resetTimer();
        boolean spot = mc.thePlayer.isPotionActive(Potion.moveSpeed);
        if (!MoveUtil.isMoving()) return;
        if (mc.thePlayer.onGround) {
            if (ModuleManager.scaffold.isEnabled()) {
                mc.thePlayer.motionY = 0.38f;
            } else {
                if (jumpDown()) {
                    mc.thePlayer.motionY = 0.42f;
                } else {
                    mc.thePlayer.motionY = 0.4f;
                }
            }
            MoveUtil.strafec(MoveUtil.getAllowedHorizontalDistance() * (spot ? 1.12 : 1.6));
        }
        if (mc.thePlayer.hurtTime > 0) {
            MoveUtil.strafec((MoveUtil.getSpeed() + (float) .09));
            mc.thePlayer.posY = mc.thePlayer.posY + 0.2;
        } else {
            MoveUtil.strafec(MoveUtil.getAllowedHorizontalDistance());
            Utils.getTimer().timerSpeed = 1.1f;
        }
        if (mc.thePlayer.hurtTime == 0 && (offGroundTicks == 6)) {
            mc.thePlayer.motionY = -0.09800000190734863;
        }
        if (offGroundTicks == 3 && spot) {
            mc.thePlayer.motionY -= 0.07;
        }

        if (offGroundTicks == 4 && !spot) {
            mc.thePlayer.motionY += 0.01;
        }
    }
}
