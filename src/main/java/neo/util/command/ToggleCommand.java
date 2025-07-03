package neo.util.command;

import net.minecraft.network.play.client.C01PacketChatMessage;
import neo.event.SendPacketEvent;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.util.Utils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ToggleCommand {

    private final ModuleManager moduleManager;

    public ToggleCommand(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (event.getPacket() instanceof C01PacketChatMessage) {
            C01PacketChatMessage chatPacket = (C01PacketChatMessage) event.getPacket();
            String message = chatPacket.getMessage().toLowerCase(); // Convert message to lower case

            if (message.startsWith(".t ") || message.startsWith(".toggle ")) {
                event.setCanceled(true);
                event.cancelEvent();

                String[] args = message.split(" ");
                if (args.length > 1) {
                    event.setCanceled(true);
                    event.cancelEvent();
                    String moduleName = args[1];
                    Module module = moduleManager.getModuleCI(moduleName);

                    if (module != null) {
                        if (module.isEnabled()) {
                            event.setCanceled(true);
                            event.cancelEvent();
                            module.disable();
                            Utils.sendMessage("&cDisabled module: " + moduleName);
                        } else {
                            event.setCanceled(true);
                            event.cancelEvent();
                            module.enable();
                            Utils.sendMessage("&aEnabled module: " + moduleName);
                        }
                    } else {
                        event.setCanceled(true);
                        event.cancelEvent();
                        Utils.sendMessage("&cModule not found: " + moduleName);
                    }
                } else {
                    event.setCanceled(true);
                    event.cancelEvent();
                    Utils.sendMessage("&cUsage: .t {modulename}");
                }
            }
        }
    }
}
