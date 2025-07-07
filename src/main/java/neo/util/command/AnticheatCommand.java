package neo.util.command;

import neo.event.ReceivePacketEvent;
import neo.util.Utils;
import neo.util.server.anticheat.AnticheatUtils;
import net.minecraft.network.play.client.C01PacketChatMessage;
import neo.event.SendPacketEvent;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedList;
import java.util.Queue;

public class AnticheatCommand {

    private boolean active = false;
    private long startTime;
    private long lastPacketTime = -1;

    private final Queue<Integer> collectedActions = new LinkedList<>();
    private final Queue<Long> collectedDelays = new LinkedList<>();

    public AnticheatCommand() {
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!active) return;

        if (System.currentTimeMillis() - startTime > 20000) {
            active = false;
            collectedActions.clear();
            collectedDelays.clear();
            Utils.sendMessage("&cAnticheat guess timed out.");
            return;
        }

        if (event.getNonStaticPacket() instanceof S32PacketConfirmTransaction) {
            int action = ((S32PacketConfirmTransaction) event.getNonStaticPacket()).getActionNumber();
            long now = System.currentTimeMillis();

            if (lastPacketTime != -1) {
                long delay = now - lastPacketTime;
                collectedDelays.offer(delay);
            } else {
                collectedDelays.offer(0L);
            }

            lastPacketTime = now;
            collectedActions.offer(action);

            if (collectedActions.size() >= 40) {
                active = false;
                int[] actions = collectedActions.stream().mapToInt(i -> i).toArray();
                long[] delays = collectedDelays.stream().mapToLong(i -> i).toArray();

                String guess = AnticheatUtils.detectPattern(actions, delays);
                Utils.sendMessage("&aAnticheat guess: &c" + guess);

                collectedActions.clear();
                collectedDelays.clear();
            }
        }
    }


    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (!(event.getNonStaticPacket() instanceof C01PacketChatMessage)) return;
        String msg = ((C01PacketChatMessage) event.getNonStaticPacket()).getMessage().toLowerCase().trim();
        if (!msg.equals(".ac") && !msg.equals(".anticheat")) return;

        event.setCanceled(true);
        event.cancelEvent();

        if (active) {
            Utils.sendMessage("&cAlready Guessing.");
            return;
        }

        active = true;
        startTime = System.currentTimeMillis();
        collectedActions.clear();
        Utils.sendMessage("&7Guessing...");
    }
}
