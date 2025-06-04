package neo.util;

import neo.clickgui.ClickGui;
import neo.clickgui.menu.MainMenu;
import neo.script.ScriptDefaults;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;

import static neo.Variables.clientName;
import static neo.util.Utils.mc;

public class RPC {
    private final String clientId = "1301947537074163834";
    private boolean started;
    static boolean ingameguis = mc.currentScreen != null && (mc.currentScreen instanceof ClickGui || mc.currentScreen instanceof GuiChat);

    public RPC() {
    }

    public void onUpdate() {
        if (!started) {
            DiscordRPC.discordInitialize(clientId, new DiscordEventHandlers.Builder().build(), true);
            started = true;
            new Thread(() -> {
                while (started) {
                    updatePresence();
                    DiscordRPC.discordRunCallbacks();
                    try { Thread.sleep(500); } catch (InterruptedException e) { break; }
                }
            }, "Discord RPC Updater").start();
        }
    }

    private void updatePresence() {
        DiscordRichPresence.Builder p = new DiscordRichPresence.Builder("");
        String ip = ScriptDefaults.client.getServerIP();
        if ("ewwor".equals(ip)) {
            p.setDetails("Likely idle. (In Menu)");

            if (mc.currentScreen instanceof MainMenu) {
                p.setDetails("Likely idle. (In Menu)");
            } if (mc.currentScreen instanceof GuiMultiplayer) {
                p.setDetails("Hmm... Which server should i destroy today?");
            } if (mc.currentScreen instanceof ClickGui) {
                p.setDetails("Configuring Modules... (ClickGUI)");
            } if (mc.currentScreen instanceof GuiChat) {
                p.setDetails("Talking... (Chat)");
            } if (mc.currentScreen instanceof GuiDisconnected) {
                p.setDetails("Most likely bad config. (Disconnected)");
            } if (mc.currentScreen instanceof GuiVideoSettings) {
                p.setDetails("I need to change my video settings, I'm lagging.");
            } if (mc.currentScreen instanceof GuiConnecting || mc.currentScreen instanceof GuiDownloadTerrain) {
                p.setDetails("I found a server to cheat on...");
            }
        } else {
            if (mc.currentScreen instanceof ClickGui) {
                p.setDetails("Configuring Modules... (ClickGUI)");
            } if (mc.currentScreen instanceof GuiChat) {
                p.setDetails("Talking... (Chat)");
            } else if (!ingameguis)
            p.setDetails("Destroying " + ip + " With " + clientName + "!");
        }
        p.setBigImage("logo", "bypass").setStartTimestamps(System.currentTimeMillis());
        p.setStartTimestamps(0);
        p.setEndTimestamp(0);
        p.setParty("ae488379-351d-4a4f-ad32-2b9b01c91657",0,0);
        DiscordRPC.discordUpdatePresence(p.build());
    }

    public void onDisable() {
        DiscordRPC.discordShutdown();
        started = false;
    }

    public static void main() {
        RPC rpc = new RPC();
        rpc.onUpdate();
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
