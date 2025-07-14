package neo.util.other;

import neo.event.ReceivePacketEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static neo.Neo.mc;

public class ParticleDistanceHandler {

    @SubscribeEvent
    public void onPacketReceive(ReceivePacketEvent event) {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof S2APacketParticles) {
            final S2APacketParticles wrapper = (S2APacketParticles) packet;

            double dist = mc.thePlayer.getDistanceSq(wrapper.getXCoordinate(), wrapper.getYCoordinate(), wrapper.getZCoordinate());

            if (dist >= 36) {
                event.setCanceled(true);
            }
        }
    }
}
