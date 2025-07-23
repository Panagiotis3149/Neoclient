package neo.module.impl.movement.mode.fly;

import neo.module.impl.movement.Fly;
import neo.util.packet.PacketUtils;
import neo.util.player.move.MoveUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

import static neo.util.Utils.mc;

public class MospixelFly {

    public static void MospixelFly() {

        if (!MoveUtil.isMoving() || mc.thePlayer.isCollidedHorizontally) {
            Fly.stage = -1;
        }

        if (Fly.ticksl == 135) {
            Fly.stage = -1;
            Fly.ticks = 0;
            PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 5, mc.thePlayer.posY + 1, mc.thePlayer.posZ + 5, true));
            Fly.ticksl = 0;
        }

        switch (Fly.stage) {
            case -1:
                mc.thePlayer.motionY = (-0.00001);
                return;
            case 0:
                Fly.moveSpeed = 0.3;
                break;
            case 1:
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = 0.3999;
                    Fly.moveSpeed *= 2.14;
                }
                break;
            case 2:
                Fly.moveSpeed = 1.5;
                break;
            default:
                Fly.moveSpeed -= Fly.moveSpeed / 109;
                mc.thePlayer.motionY = (-0.00001);
                break;
        }

        mc.thePlayer.jumpMovementFactor = 0F;
        MoveUtil.strafe4(Math.max(Fly.moveSpeed, MoveUtil.getAllowedHorizontalDistance()));
        Fly.stage++;
    }

}
