package keystrokesmod.module.impl.combat;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class Velocity2 extends Module {
    public static SliderSetting mode;
    private String[] modes = new String[]{"CancelPacket"};

    public Velocity2() {
        super("Velocity2", Module.category.combat, 0);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
    }




//   public void onUpdate() {
   //     switch ((int) mode.getInput()) {
 //           case 1:
  //              // tank sotanorsu/enorsu/norsu for da methode! (https://github.com/enorsu/LiquidBouncePlusPlus/)
  //              if (mc.thePlayer.hurtTime > 1 && mc.thePlayer.onGround) {
     //               Utils.startBlink(new C03PacketPlayer(true), new C0CPacketInput());
      //              mc.thePlayer.motionX *= 0.5;
      //              mc.thePlayer.motionY *= 0.75;
       //             Utils.stopBlink();
         //       }
    //            break;
    //    }
   // }

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
