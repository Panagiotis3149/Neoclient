package keystrokesmod.module.impl.other;

import keystrokesmod.event.RotationEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MHelper extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();

    public MHelper() {
        super("Miniblox Helper", Module.category.other);
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        event.setMoveFix(RotationHandler.MoveFix.Strict);
    }

    @SubscribeEvent
    public void onPacketSend(SendPacketEvent event) {
        if (event.getPacket() instanceof C03PacketPlayer) {
            sendC0CPacket();
        }
    }

    private void sendC0CPacket() {
        mc.getNetHandler().addToSendQueue(new C0CPacketInput(mc.thePlayer.moveStrafing, mc.thePlayer.moveForward, mc.thePlayer.movementInput.jump, mc.thePlayer.movementInput.sneak));
    }

}