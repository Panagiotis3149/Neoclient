package neo.module.impl.movement.mode.speed;


import neo.event.StrafeEvent;
import neo.module.impl.other.RotationHandler;
import neo.util.Utils;
import neo.util.player.move.MoveUtil;


import static neo.util.Utils.mc;

public class MatrixSpeed {
    public static void MatrixSpeed(StrafeEvent e) {
        Utils.getTimer().timerSpeed = 1.0025f;

        if (!mc.thePlayer.onGround) {
            RotationHandler.setMoveFix(RotationHandler.MoveFix.Silent);
            e.setYaw(mc.thePlayer.rotationYaw + 45);
        } else if (mc.thePlayer.onGround && e.getFriction() + 0.003 <= 1.0) {
            e.setFriction((float) (e.getFriction() + 0.003));
        }

        MoveUtil.moveFlying(0.000399);
    }
}
