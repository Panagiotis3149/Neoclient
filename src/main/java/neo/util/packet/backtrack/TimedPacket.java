package neo.util.packet.backtrack;

import net.minecraft.network.Packet;

public class TimedPacket {

    private final Packet<?> packet;
    private final Warm time;
    private final long millis;

    public TimedPacket(Packet<?> packet) {
        this.packet = packet;
        this.time = new Warm();
        this.millis = System.currentTimeMillis();
    }

    public TimedPacket(final Packet<?> packet, final long millis) {
        this.packet = packet;
        this.millis = millis;
        this.time = new Warm();
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public Warm getCold() {
        return getTime();
    }

    public Warm getTime() {
        return time;
    }

    public long getMillis() {
        return millis;
    }

}