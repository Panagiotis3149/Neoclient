package neo.util.command;

import neo.util.other.SmartKeyboard;
import net.minecraft.network.play.client.C01PacketChatMessage;
import neo.event.SendPacketEvent;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.util.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class BindCommand {

    private final ModuleManager moduleManager;

    public BindCommand(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (!(event.getNonStaticPacket() instanceof C01PacketChatMessage)) return;
        C01PacketChatMessage chat = (C01PacketChatMessage) event.getNonStaticPacket();
        String msg = chat.getMessage().toLowerCase();


        if (msg.startsWith(".bind ")) {

            event.setCanceled(true);
            event.cancelEvent();

            String[] parts = msg.split("\\s+");
            if (parts.length != 3) {
                Utils.sendMessage("&cUsage: .bind <module> <key>");
                return;
            }

            String moduleName = parts[1];
            String keyName = parts[2].toUpperCase();

            Module module = moduleManager.getModuleCI(moduleName);
            if (module == null) {
                Utils.sendMessage("&cModule not found: " + moduleName);
                return;
            }

            int keyIndex = SmartKeyboard.getKeyIndexLoose(keyName);


            if (keyName.equals("NONE")) {
                module.setBind(0);
                Utils.sendMessage("&aUnbound &b" + moduleName);
                return;
            }

            if (keyIndex == Keyboard.KEY_NONE) {
                Utils.sendMessage("&cUnknown key: " + SmartKeyboard.getGuess(keyName) + " (Is it spelled right?)");
                return;
            }

            module.setBind(keyIndex);
            Utils.sendMessage("&aBound &b" + moduleName + " &ato &b" + keyName + " (code " + keyIndex + ")");
        } else if (msg.startsWith(".bind")) {
            Utils.sendMessage("&cUsage: .bind <module> <key>");
        }
    }
}
