package neo.module.impl.movement.mode.speed;

import neo.util.player.move.MoveUtil;
import neo.util.Utils;
import net.minecraft.potion.Potion;


import static neo.module.impl.movement.BHop.offGroundTicks;
import static neo.util.Utils.mc;

public class BMCSpeed {
    public static boolean reset;
    public static double speed;
    static final boolean spot = mc.thePlayer.isPotionActive(Potion.moveSpeed);


    public static void BMCSpeed() {
        if (Utils.isntnull()) {
            Utils.resetTimer();
            if (MoveUtil.isMoving()) {
                MoveUtil.strafec(Math.max(MoveUtil.getSpeed(), MoveUtil.getAllowedHorizontalDistance()));
                if (mc.thePlayer.onGround) {
                    MoveUtil.jump(0.42F);
                    MoveUtil.strafec(MoveUtil.getAllowedHorizontalDistance() * (spot ? 1.1 : 1.5));
                }
                if (mc.thePlayer.hurtTime > 0) {
                    MoveUtil.strafec(0.5);
                }
                if (mc.thePlayer.hurtTime == 0 && (offGroundTicks == 5)) {
                    mc.thePlayer.motionY = -0.09800000190734863;
                }
                if (mc.thePlayer.isCollidedHorizontally) {
                    speed = MoveUtil.getAllowedHorizontalDistance();
                }

                reset = false;
            } else if (!reset) {
                speed = 0;

                reset = true;
                speed = MoveUtil.getAllowedHorizontalDistance();
            }
        }
    }
}
