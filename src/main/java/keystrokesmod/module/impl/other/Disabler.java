package keystrokesmod.module.impl.other;

import io.netty.buffer.Unpooled;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Disabler extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final SliderSetting mode;
    private boolean isSpoofing = false;
    private final String[] modes = new String[]{"Geyser"};

    public Disabler() {
        super("Disabler", Module.category.other);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (event.getPacket() instanceof C17PacketCustomPayload) {
            C17PacketCustomPayload packet = (C17PacketCustomPayload) event.getPacket();
            if ("MC|Brand".equals(packet.getChannelName())) {
                if (isSpoofing) return;

                event.cancelEvent();

                if (mode.getInput() == 0) {
                    spoofGeyser();
                }
            }
        }
    }



    private void spoofGeyser() {
        String spoofedBrand = "Geyser";
        byte[] data = spoofedBrand.getBytes();
        PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(data));
        C17PacketCustomPayload spoofPacket = new C17PacketCustomPayload("MC|Brand", buffer);

        isSpoofing = true;
        mc.getNetHandler().getNetworkManager().sendPacket(spoofPacket);
        isSpoofing = false;
    }
}
