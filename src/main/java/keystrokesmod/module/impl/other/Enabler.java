package keystrokesmod.module.impl.other;

import keystrokesmod.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;

import java.util.Random;

public class Enabler extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private static final Random random = new Random();

    public Enabler() {
        super("AnticheatEnabler", Module.category.other);
    }


    public void onUpdate() {
        C03PacketPlayer.C04PacketPlayerPosition packet1 = new C03PacketPlayer.C04PacketPlayerPosition(
                99999999, 99999999, 99999999, false
        );
        C03PacketPlayer.C04PacketPlayerPosition packet2 = new C03PacketPlayer.C04PacketPlayerPosition(
                111231, -1839217123, 300000000, true
        );
        C03PacketPlayer.C04PacketPlayerPosition packet3 = new C03PacketPlayer.C04PacketPlayerPosition(
                0, 256, 0, false
        );
        C03PacketPlayer.C05PacketPlayerLook packet5 = new C03PacketPlayer.C05PacketPlayerLook(
                361, 361, false
        );
        C03PacketPlayer.C06PacketPlayerPosLook packet6 = new C03PacketPlayer.C06PacketPlayerPosLook(10000, 10000, 10000, 400, 400, false);

        C13PacketPlayerAbilities packet4 = new C13PacketPlayerAbilities();
        packet4.setFlying(true);
        packet4.setFlySpeed(1000000000);

        float randomYaw = random.nextFloat() * 1000.0f;
        float randomPitch = random.nextFloat() * 1000.0f;

        mc.thePlayer.rotationYaw = randomYaw;
        mc.thePlayer.rotationPitch = randomPitch;

        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet1);
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet2);
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet3);
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet4);
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet5);
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet6);
    }

    public void onDisable() {
    }
}