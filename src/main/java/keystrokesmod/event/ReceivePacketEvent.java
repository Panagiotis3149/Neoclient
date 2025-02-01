package keystrokesmod.event;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.lang.reflect.Field;

@Cancelable
public class ReceivePacketEvent extends Event {
    private static Packet<?> packet;
    private boolean cancelPacket;

    public ReceivePacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public static Packet<?> getPacket() {
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
