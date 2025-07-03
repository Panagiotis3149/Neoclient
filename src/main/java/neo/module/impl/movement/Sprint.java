package neo.module.impl.movement;

import neo.module.Module;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class Sprint extends Module {
    private final SliderSetting mode = new SliderSetting("Mode", new String[]{"Legit"}, 0);
   // private final SliderSetting omniMode = new SliderSetting("Bypass mode", new String[]{"None", "Legit"}, 0);
    public Sprint() {
        super("Sprint", Module.category.movement, 0);
        this.registerSetting(mode);
    //    this.registerSetting(omniMode);
    }


    @SubscribeEvent
    public void p(PlayerTickEvent e) {
        if (Utils.isnull() && mc.inGameHasFocus) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }

    /*
    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mode.getInput() != 1) return;
        switch ((int) omniMode.getInput()) {
            case 0:
                event.setSprinting(true);
                mc.thePlayer.setSprinting(true);
                mc.thePlayer.sprintingTicksLeft = 600;
                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                break;
            case 1:
                event.setYaw(event.getYaw() + Move.fromMovement(mc.thePlayer.moveForward, mc.thePlayer.moveStrafing).getDeltaYaw());
                if (MoveUtil.canSprint(true)) mc.thePlayer.setSprinting(true);
                break;
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (e.getPacket() instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction actionPacket = (C0BPacketEntityAction) e.getPacket();
            if (actionPacket.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                e.cancelEvent();
            }
        }
    }
 */

    // Temporarily disabled Omni-Sprint to prevent issues.

}