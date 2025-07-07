package neo.util.command;

import neo.module.impl.other.NameHider;
import net.minecraft.network.play.client.C01PacketChatMessage;
import neo.event.SendPacketEvent;
import neo.util.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NameHiderCommand {

    public NameHiderCommand() {
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (!(event.getNonStaticPacket() instanceof C01PacketChatMessage)) return;

        C01PacketChatMessage chatPacket = (C01PacketChatMessage) event.getNonStaticPacket();
        String message = chatPacket.getMessage().trim();

        if (message.toLowerCase().startsWith(".namehider ")) {
            event.cancelEvent();

            String[] args = message.split("\\s+", 2);
            if (args.length < 2 || args[1].isEmpty()) {
                Utils.sendMessage("&cUsage: .namehider <newName>");
                return;
            }

            String newName = args[1];


            NameHider.setFakeName(newName);

            Utils.sendMessage("&aFake name set to: &b" + newName);
        } else if (message.equalsIgnoreCase(".namehider")) {
            event.cancelEvent();
            Utils.sendMessage("&cUsage: .namehider <newName>");
        }
    }
}
