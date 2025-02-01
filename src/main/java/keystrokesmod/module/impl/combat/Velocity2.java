package keystrokesmod.module.impl.combat;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class Velocity2 extends Module {
    public static SliderSetting mode;
    private String[] modes = new String[]{"CancelPacket"};

    public Velocity2() {
        super("Velocity2", Module.category.combat, 0);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (this.isEnabled()) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof S12PacketEntityVelocity) {
                handleVelocityPacket((S12PacketEntityVelocity) packet, event);
            }
        }
    }

    private void handleVelocityPacket(S12PacketEntityVelocity packet, ReceivePacketEvent event) {
        switch ((int) mode.getInput()) {
            case 0:
                if (this.isEnabled()) {
                event.cancelEvent();
                }
                break;
        }
    }
}
