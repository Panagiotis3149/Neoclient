package neo.module.impl.player;

import neo.event.SendPacketEvent;
import neo.module.Module;
import neo.module.setting.impl.SliderSetting;
import neo.util.packet.PacketUtils;
import neo.util.Utils;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FakeLag extends Module {
    private final SliderSetting packetDelay;
    private final ConcurrentHashMap<Packet<?>, Long> delayedPackets = new ConcurrentHashMap<>();

    public FakeLag() {
        super("FakeLag", category.player);
        this.registerSetting(packetDelay = new SliderSetting("Packet delay", 200, 25, 1000, 5, "ms"));
    }

    public String getInfo() {
        return (int) packetDelay.getInput() + "ms";
    }

    public void onEnable() {
        delayedPackets.clear();
    }

    public void onDisable() {
        sendPacket(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.isnull()) {
            sendPacket(false);
            return;
        }
        sendPacket(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSendPacket(SendPacketEvent e) {
        long receiveTime = System.currentTimeMillis();
        if (!Utils.isnull()) {
            sendPacket(false);
            return;
        }
        if (e.isCanceled()) {
            return;
        }
        Packet<?> packet = SendPacketEvent.getPacket();
        if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart || packet instanceof C00PacketServerQuery || packet instanceof C01PacketEncryptionResponse) {
            return;
        }
        if (SendPacketEvent.getPacket() != null && !Utils.isnull()) {
            delayedPackets.put(SendPacketEvent.getPacket(), receiveTime);
        }
        e.setCanceled(true);
    }

    private void sendPacket(boolean delay) {
            Iterator<Map.Entry<Packet<?>, Long>> packets = delayedPackets.entrySet().iterator();
            while (packets.hasNext()) {
                Map.Entry<Packet<?>, Long> entry = packets.next();
                Packet<?> packet = entry.getKey();
                if (packet == null) {
                    continue;
                }
                long receiveTime = entry.getValue();
                long ms = System.currentTimeMillis();
                if (Utils.getDifference(ms, receiveTime) > packetDelay.getInput() || !delay) {
                    PacketUtils.sendPacketNoEvent(packet);
                    packets.remove();
                }
            }
    }
}
