package neo.module.impl.movement.mode.speed;

import neo.Neo;
import neo.module.impl.player.Scaffold;
import neo.util.player.move.MoveUtil;
import neo.util.Utils;
import net.minecraft.potion.Potion;


import static neo.module.impl.movement.BHop.offGroundTicks;
import static neo.util.Utils.mc;

public class BMCSpeed {
    public static boolean reset;
    public static double speed;
    static final boolean spot = mc.thePlayer.isPotionActive(Potion.moveSpeed);
    static double a = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
    static double fr = mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.367 : 0.255;


    public static void BMCSpeed() {
        if (Utils.isnull()) {
            Utils.resetTimer();
            if (Neo.moduleManager.scaffold.isEnabled()) {
                return;
            }
            if (MoveUtil.isMoving()) {
                MoveUtil.strafec(Math.max(MoveUtil.getSpeed(), MoveUtil.getAllowedHorizontalDistance()));
                if (mc.thePlayer.onGround) {
                    MoveUtil.jump(0.42F);
                    MoveUtil.strafec(spot ? 0.60 : 0.48);
                }
                if (mc.thePlayer.hurtTime > 0) {
                    MoveUtil.strafec(0.5);
                }
                if (a < fr) {
                    MoveUtil.strafec(fr);
                }
                if (offGroundTicks == 4 && mc.thePlayer.motionY > 0) {
                    mc.thePlayer.motionY = -0.09800000190734863;
                }
                if (mc.thePlayer.hurtTime == 0 && (offGroundTicks == 5)) {
                    mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 1);
                }
                if (mc.thePlayer.isCollidedHorizontally) {
                    speed = MoveUtil.getAllowedHorizontalDistance();
                }

                reset = false;

                Utils.getTimer().timerSpeed = 1.01f;
            } else if (!reset) {
                speed = 0;
                Utils.resetTimer();

                reset = true;
                speed = MoveUtil.getAllowedHorizontalDistance();
            }
        }
    }
}
