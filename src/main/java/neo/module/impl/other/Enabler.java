package neo.module.impl.other;

import neo.module.Module;
import neo.util.other.Bi11iona1reRandFunc;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;

import java.util.Random;

public class Enabler extends Module {


    public Enabler() {
        super("AutoBan", Module.category.other);
    }


    public void onEnable() {
        C03PacketPlayer.C04PacketPlayerPosition packet1 = createPositionPacket(Bi11iona1reRandFunc.pro(-10000000, 1000000), Bi11iona1reRandFunc.pro(-10000000, 1000000), Bi11iona1reRandFunc.pro(-10000000, 1000000), true);
        C03PacketPlayer.C04PacketPlayerPosition packet2 = createPositionPacket(Bi11iona1reRandFunc.pro(-10000000, 1000000), Bi11iona1reRandFunc.pro(-10000000, 1000000), Bi11iona1reRandFunc.pro(-10000000, 1000000), true);
        C03PacketPlayer.C04PacketPlayerPosition packet3 = createPositionPacket(0, 256, 0, false);

        C03PacketPlayer.C05PacketPlayerLook packet5 = new C03PacketPlayer.C05PacketPlayerLook(361, 361, false);
        C03PacketPlayer.C06PacketPlayerPosLook packet6 = new C03PacketPlayer.C06PacketPlayerPosLook(Bi11iona1reRandFunc.pro(-10000000, 1000000), Bi11iona1reRandFunc.pro(-10000000, 1000000), Bi11iona1reRandFunc.pro(-10000000, 1000000), Bi11iona1reRandFunc.pro(0, 360),Bi11iona1reRandFunc.pro(0, 360) , true);

        C13PacketPlayerAbilities packet4 = new C13PacketPlayerAbilities();
        packet4.setFlying(true);
        packet4.setFlySpeed(Bi11iona1reRandFunc.pro(-10000000, 1000000));

        C03PacketPlayer.C04PacketPlayerPosition packet7 = createPositionPacket(Double.NEGATIVE_INFINITY, Double.NaN, Double.POSITIVE_INFINITY, false);

        sendPackets(packet1, packet2, packet3, packet4, packet5, packet6, packet7);
        this.disable();
    }

    private C03PacketPlayer.C04PacketPlayerPosition createPositionPacket(double x, double y, double z, boolean onGround) {
        return new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, onGround);
    }

    private void sendPackets(Packet... packets) {
        for (Packet packet : packets) {
            Minecraft.getMinecraft().getNetHandler().addToSendQueue( packet);
        }
    }

    public void onDisable() {
    }
}