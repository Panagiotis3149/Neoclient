package neo.util.command;

import neo.util.render.Theme;
import net.minecraft.network.play.client.C01PacketChatMessage;
import neo.event.SendPacketEvent;
import neo.util.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HelpCommand {


    public HelpCommand() {
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (!(event.getNonStaticPacket() instanceof C01PacketChatMessage)) return;
        C01PacketChatMessage chatPacket = (C01PacketChatMessage) event.getNonStaticPacket();
        String message = chatPacket.getMessage().toLowerCase();

        if (message.startsWith(".help") || message.startsWith(".?")) {
            event.setCanceled(true);
            event.cancelEvent();


            Utils.sendMessage(Theme.wrap("Command Help"));
            Utils.sendMessage("." + Theme.wrap("bind") + " §7<module> <key> " + Theme.wrap("-") + " §fBinds a module to a key");
            Utils.sendMessage("." + Theme.wrap("toggle") + " §7<module> " + Theme.wrap("-") + " §fToggles a module");
            Utils.sendMessage("." + Theme.wrap("namehider") + " §7<name> " + Theme.wrap("-") + " §fChanges the fake name");
            Utils.sendMessage("." + Theme.wrap("anticheat") + "§7/" + Theme.wrap("ac") + " " + Theme.wrap("-") + " §fGuesses the Server AC");
            Utils.sendMessage("." + Theme.wrap("help") + "§7/" + Theme.wrap("?") + " " + Theme.wrap("-") + " §fShows this menu");

        }
    }
}
