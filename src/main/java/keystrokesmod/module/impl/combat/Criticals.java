package keystrokesmod.module.impl.combat;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class Criticals extends Module {
    public int ticksSinceVelocity = Integer.MAX_VALUE;

    public Criticals() {
        super("Criticals", category.combat);
        this.registerSetting(new DescriptionSetting("Makes you get a critical hit every time you attack. (NCP)"));
    }

    @Override
    public void onEnable() {
        ticksSinceVelocity = Integer.MAX_VALUE;
    }

    @Override
    public void onUpdate() {
        ticksSinceVelocity++;
    }

    @SubscribeEvent
    public void onPacketReceive(@NotNull ReceivePacketEvent event) {
        if (ReceivePacketEvent.getPacket() instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity) ReceivePacketEvent.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                ticksSinceVelocity = 0;
            }
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!isEnabled()) return;
        if (ticksSinceVelocity <= 18 && mc.thePlayer.fallDistance < 1.3) {
            event.setOnGround(false);
        }
    }
}