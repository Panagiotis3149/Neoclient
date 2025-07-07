package neo.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SendPacketEvent extends Event {
    private static Packet<?> staticPacket; // static for peeking
    private final Packet<?> packet; // actual instance packet

    private boolean cancelPacket;

    public SendPacketEvent(Packet<?> packet) {
        this.packet = packet;
        staticPacket = packet;
    }

    public static Packet<?> getPacket() {
        return staticPacket;
    }

    public Packet<?> getNonStaticPacket() {
        return packet;
    }

    public boolean isCanceled() {
        return cancelPacket;
    }

    public void cancelEvent() {
        this.cancelPacket = true;
        this.setCanceled(true);
    }
}
