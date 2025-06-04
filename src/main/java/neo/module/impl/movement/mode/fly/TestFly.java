
package neo.module.impl.movement.mode.fly;

import neo.event.PreMotionEvent;
import neo.util.packet.PacketUtils;
import neo.util.player.move.MoveUtil;
import neo.util.world.block.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;

public class TestFly {

    public static double moveSpeed;
    static Minecraft mc = Minecraft.getMinecraft();

    public static void TestFly(PreMotionEvent e) {
        final float speed = 0.27f;

        mc.thePlayer.motionY = -1E-10D
                + (mc.gameSettings.keyBindJump.isKeyDown() ? speed : 0.0D)
                - (mc.gameSettings.keyBindSneak.isKeyDown() ? speed : 0.0D);

        if (mc.thePlayer.onGround) {
            MoveUtil.stop();
        }

            MoveUtil.partialStrafePercent(25);
            if (BlockUtils.blockRelativeToPlayer(0, -2.5, 0).isFullBlock()) {
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, MoveUtil.roundToGround(mc.thePlayer.posY - (2.5 - (Math.random() / 100))), mc.thePlayer.posZ, false));
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));

                mc.thePlayer.jump();
            }

    }

    }
