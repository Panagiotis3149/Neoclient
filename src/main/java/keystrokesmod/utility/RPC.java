package keystrokesmod.utility;

import keystrokesmod.script.ScriptDefaults;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

import java.util.Objects;

import static keystrokesmod.Variables.clientName;

public class RPC {
    private final String clientId = "1301947537074163834";
    private boolean started;

    public RPC() {
    }

    public void onUpdate() {
        if (!started) {
            DiscordRPC.discordInitialize(clientId, new DiscordEventHandlers.Builder().setReadyEventHandler(user -> {
                DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder("");
                if ((Objects.equals(ScriptDefaults.client.getServerIP(), "ewwor"))) { presence.setDetails("Likely idle. (In Menu)"); }
                else if (!Objects.equals(ScriptDefaults.client.getServerIP(), "ewwor")) {
                    assert !Objects.equals(ScriptDefaults.client.getServerIP(), "ewwor");
                    presence.setDetails("Cheating on " + ScriptDefaults.client.getServerIP() + " With " + clientName);
                }
                presence.setBigImage("logo", "bipas").setStartTimestamps(System.currentTimeMillis());
                presence.setStartTimestamps(0);
                presence.setEndTimestamp(0);
                presence.setParty("ae488379-351d-4a4f-ad32-2b9b01c91657", 0, 0 );
                DiscordRPC.discordUpdatePresence(presence.build());
            }).build(), true);
            new Thread(() -> {
                while (started) {
                    DiscordRPC.discordRunCallbacks();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Discord RPC Callback").start();
            started = true;
        }
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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
