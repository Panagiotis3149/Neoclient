package neo.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SendPacketEvent extends Event {
    private static Packet<?> packet;
    private boolean cancelPacket;

    public SendPacketEvent(Packet<?> packet) {
        packet = packet;
    }

    public static Packet<?> getPacket() { return packet; }


    public boolean isCanceled() {
        return cancelPacket;
    }

    public void cancelEvent() {
        this.cancelPacket = true;
        this.setCanceled(true);
    }
}