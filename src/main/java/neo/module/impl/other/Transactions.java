package neo.module.impl.other;

import neo.event.ReceivePacketEvent;
import neo.module.Module;
import neo.util.Utils;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Transactions extends Module {
    long lastPacketTime = -1;
    long delay = -1;


    public Transactions() {
        super("Transactions", Module.category.other);
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.getNonStaticPacket() instanceof S32PacketConfirmTransaction) {
            S32PacketConfirmTransaction pkt = (S32PacketConfirmTransaction) event.getNonStaticPacket();
            int action = pkt.getActionNumber();
            long now = System.currentTimeMillis();

            delay = (lastPacketTime == -1) ? -1 : now - lastPacketTime;
            lastPacketTime = now;

            if (delay == -1) Utils.sendMessage("&aActionID: &e" + action + " &7Delay: &bN/A");
            else Utils.sendMessage("&aActionID: &e" + action + " &7Delay: &b" + delay + "ms");

        }
    }

    @Override
    public void onDisable() {
        lastPacketTime = -1;
        delay = -1;
    }
}
