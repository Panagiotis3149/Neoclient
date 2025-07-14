package neo.module.impl.other;

import neo.event.PreMotionEvent;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.util.other.GuiDetectionHandler;
import neo.util.packet.BadPacketsHandler;
import neo.util.packet.PacketUtils;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class Disabler extends Module {
    private final ButtonSetting vulcanomni = new ButtonSetting("Vulcan OmniSprint", false);
    private final ButtonSetting vulcansj = new ButtonSetting("Vulcan Strafe & Jump", false);
    private final ButtonSetting vulcanclick = new ButtonSetting("Vulcan Clicker", false);


    public Disabler() {
        super("Disabler", Module.category.other);
        this.registerSetting(vulcanomni, vulcansj, vulcanclick);
    }

    @Override
    public void onEnable() {

    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) {
        if (vulcanomni.isToggled()) {
            PacketUtils.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
            PacketUtils.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
        }

        if (vulcansj.isToggled()) {
            if (mc.thePlayer.ticksExisted % 5 == 0) {
                PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer), EnumFacing.UP));
            }
        }

        if (vulcanclick.isToggled() && mc.thePlayer.ticksExisted % 100 == 0 && !BadPacketsHandler.bad(false, true, true, false, false) && !GuiDetectionHandler.isInGUI()) {
            PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, new BlockPos(mc.thePlayer), EnumFacing.UP));
        }
    }


    @Override
    public void onDisable() {
    }
}