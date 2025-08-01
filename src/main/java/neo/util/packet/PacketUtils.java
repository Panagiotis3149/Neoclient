package neo.util.packet;

import neo.script.classes.Vec3;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static neo.Neo.mc;

public class PacketUtils {
    public static List<Packet<?>> skipSendEvent = new ArrayList<>();
    public static List<Packet<?>> skipReceiveEvent = new ArrayList<>();

    public static void sendPacketNoEvent(Packet<?> packet) {
        if (packet == null || packet.getClass().getSimpleName().startsWith("S")) {
            return;
        }
        skipSendEvent.add(packet);
        mc.thePlayer.sendQueue.addToSendQueue(packet);
    }


    public static void sendPacket(Packet<?> packet) {
        if (packet == null || packet.getClass().getSimpleName().startsWith("S")) {
            return;
        }
        mc.thePlayer.sendQueue.addToSendQueue(packet);
    }

    public static void receivePacketNoEvent(Packet<INetHandlerPlayClient> packet) {
        try {
            skipReceiveEvent.add(packet);
            packet.processPacket(mc.getNetHandler());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void receivePacket(Packet<INetHandlerPlayClient> packet) {
        try {
            packet.processPacket(mc.getNetHandler());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void receivePacket2(Packet<?> packet) {
        if (packet == null)
            return;
        try {
            Packet<INetHandlerPlayClient> casted = castPacket(packet);
            casted.processPacket(mc.getNetHandler());
        } catch (ThreadQuickExitException ignored) {
        } catch (Exception e) {
            BadPacketsHandler.onBadPacket(packet, e);
        }
    }

    public static @NotNull Optional<Vec3> getPos(Packet<?> packet) {
        if (packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            final C03PacketPlayer.C06PacketPlayerPosLook p = (C03PacketPlayer.C06PacketPlayerPosLook) packet;
            return Optional.of(new Vec3(p.getPositionX(), p.getPositionY(), p.getPositionZ()));
        }
        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            final C03PacketPlayer.C04PacketPlayerPosition p = (C03PacketPlayer.C04PacketPlayerPosition) packet;
            return Optional.of(new Vec3(p.getPositionX(), p.getPositionY(), p.getPositionZ()));
        }
        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            final C03PacketPlayer.C04PacketPlayerPosition p = (C03PacketPlayer.C04PacketPlayerPosition) packet;
            return Optional.of(new Vec3(p.getPositionX(), p.getPositionY(), p.getPositionZ()));
        }
        return Optional.empty();
    }

    public static <H extends INetHandler> Packet<H> castPacket(Packet<?> packet) throws ClassCastException {
        return (Packet<H>) packet;
    }
}