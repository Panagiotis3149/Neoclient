package neo.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ReceivePacketEvent extends Event {
    private static Packet<?> staticPacket;
    private Packet<?> packet;

    private boolean cancelPacket;

    public ReceivePacketEvent(Packet<?> packet) {
        this.packet = packet;
        staticPacket = packet;
    }

    public static Packet<?> getPacket() {
        return staticPacket;
    }

    public Packet<?> getNonStaticPacket() {
        return packet;
    }

    public void setPacket(Packet<?> newPacket) {
        this.packet = newPacket;
    }

    public boolean isCanceled() {
        return cancelPacket;
    }

    public void cancelEvent() {
        this.cancelPacket = true;
        this.setCanceled(true);
    }
}
