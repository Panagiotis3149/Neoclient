package keystrokesmod.module.impl.movement;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.event.SprintEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Move;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class Sprint extends Module {
    private final SliderSetting mode = new SliderSetting("Mode", new String[]{"Legit", "Omni"}, 0);
    private final SliderSetting omniMode = new SliderSetting("Bypass mode", new String[]{"None", "Legit"}, 0);
    public static boolean omni = false;
    public static boolean stopSprint = false;

    public Sprint() {
        super("Sprint", Module.category.movement, 0);
        this.registerSetting(mode);
        this.registerSetting(omniMode);
    }

    public static boolean omni() {
        final SprintEvent event = new SprintEvent(
                MoveUtil.isMoving(),
                omni || ModuleManager.sprint != null && ModuleManager.sprint.isEnabled() && ModuleManager.sprint.mode.getInput() == 1
        );

        return event.isSprint() && event.isOmni();
    }

    public static boolean stopSprint() {
        final SprintEvent event = new SprintEvent(!stopSprint, false);

        return !event.isSprint();
    }

    @SubscribeEvent
    public void p(PlayerTickEvent e) {
        if (Utils.nullCheck() && mc.inGameHasFocus) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mode.getInput() != 1) return;



        switch ((int) omniMode.getInput()) {
            case 0:
                event.setSprinting(true);
                mc.thePlayer.setSprinting(true);
                break;
            case 1:
                event.setYaw(event.getYaw() + Move.fromMovement(mc.thePlayer.moveForward, mc.thePlayer.moveStrafing).getDeltaYaw());
                if (MoveUtil.canSprint(true)) mc.thePlayer.setSprinting(true);
                break;
        }

        if (MoveUtil.isMoving())
            mc.thePlayer.setSprinting(true);
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (omniMode.getInput() == 0 && mode.getInput() == 1 && e.getPacket() instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction actionPacket = (C0BPacketEntityAction) e.getPacket();
            if (actionPacket.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                e.cancelEvent();
            }
        }
    }




}