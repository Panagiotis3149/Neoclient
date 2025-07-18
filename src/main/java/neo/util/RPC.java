package neo.util;

import neo.gui.altmgr.*;
import neo.gui.click.ClickGui;
import neo.gui.menu.AltMGR;
import neo.gui.menu.MainMenu;
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

    public RPC() {}

    public void onUpdate() {
        if (!started) {
            DiscordRPC.discordInitialize(clientId, new DiscordEventHandlers.Builder().build(), true);
            started = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (started) {
                        updatePresence();
                        DiscordRPC.discordRunCallbacks();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }, "Discord RPC Updater").start();
        }
    }

    private void updatePresence() {
        DiscordRichPresence.Builder p = new DiscordRichPresence.Builder("");

        String ip = ScriptDefaults.client.getServerIP();
        boolean ingameGuis = mc.currentScreen != null &&
                (mc.currentScreen instanceof ClickGui || mc.currentScreen instanceof GuiChat);

        if ("ewwor".equals(ip)) {
            if (mc.currentScreen instanceof MainMenu) {
                p.setDetails("Likely idle. (In Menu)");
            } else if (mc.currentScreen instanceof GuiMultiplayer) {
                p.setDetails("Hmm... Which server should i destroy today?");
            } else if (mc.currentScreen instanceof ClickGui) {
                p.setDetails("Configuring Modules... (ClickGUI)");
            } else if (mc.currentScreen instanceof GuiChat) {
                p.setDetails("Talking... (Chat)");
            } else if (mc.currentScreen instanceof GuiDisconnected) {
                p.setDetails("Most likely bad config. (Disconnected)");
            } else if (mc.currentScreen instanceof GuiVideoSettings) {
                p.setDetails("I need to change my video settings, I'm lagging.");
            } else if (mc.currentScreen instanceof GuiConnecting || mc.currentScreen instanceof GuiDownloadTerrain) {
                p.setDetails("I found a server to cheat on...");
            } else if (mc.currentScreen instanceof AltMGR
                    || mc.currentScreen instanceof AddAccount
                    || mc.currentScreen instanceof CrackedLogin
                    || mc.currentScreen instanceof MicrosoftLogin
                    || mc.currentScreen instanceof CredentialLogin
                    || mc.currentScreen instanceof CookieLogin
                    || mc.currentScreen instanceof TokenLogin
            ) {
              p.setDetails("Adding Alts...");
            } else if (mc.currentScreen instanceof GuiYesNo) {
                p.setDetails("Questioning life choices (GuiY/N)");
            }  else {
                p.setDetails("Other GUIs");
            }
        } else {
            if (mc.currentScreen instanceof ClickGui) {
                p.setDetails("Configuring Modules... (ClickGUI)");
            } else if (mc.currentScreen instanceof GuiChat) {
                p.setDetails("Talking... (Chat)");
            } else if (!ingameGuis) {
                p.setDetails("Destroying " + ip + " With " + clientName + "!");
            }
        }

        p.setBigImage("logo", "bypass");
        p.setStartTimestamps(System.currentTimeMillis());
        p.setParty("ae488379-351d-4a4f-ad32-2b9b01c91657", 0, 0);
        DiscordRPC.discordUpdatePresence(p.build());
    }

    public void onDisable() {
        DiscordRPC.discordShutdown();
        started = false;
    }
}
