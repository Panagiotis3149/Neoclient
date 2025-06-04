package neo.module.impl.movement.mode.longjump;

import neo.event.MoveEvent;
import neo.module.impl.movement.LongJump;
import neo.util.player.move.MoveUtil;
import net.minecraft.potion.Potion;
import static neo.util.Utils.mc;

public class MospixelLongjump {

    public static double moveSpeed;
    public static double lastDistance;
    public static boolean jumped;

    public static void MospixelLongJump(MoveEvent e) {
        if (MoveUtil.isMoving()) {
            if (mc.thePlayer.onGround) {
                e.y = mc.thePlayer.motionY + 0.42f + (mc.thePlayer.isPotionActive(Potion.jump)
                        ? MoveUtil.getJumpBoostMotion() : 0);
                moveSpeed = MoveUtil.getBaseMoveSpeed() * 2.2;
                jumped = true;
            } else if (jumped) {
                moveSpeed = lastDistance - lastDistance * 0.01f;
            } else {
                moveSpeed = lastDistance - lastDistance / 240;
            }
            if (LongJump.offGroundTicks == 5 && !mc.thePlayer.isPotionActive(Potion.jump))
                e.y = mc.thePlayer.motionY - 0.01;

            MoveUtil.strafe4(moveSpeed = Math.max(moveSpeed, MoveUtil.getBaseMoveSpeed()));
        }
    }
}
