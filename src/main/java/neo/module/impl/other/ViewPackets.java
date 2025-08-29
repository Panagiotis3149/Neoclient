package neo.module.impl.other;

import neo.event.ReceivePacketEvent;
import neo.event.SendPacketEvent;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ViewPackets extends Module {
    private final ButtonSetting includeCancelled = new ButtonSetting("Include cancelled", true);
    private final ButtonSetting sent = new ButtonSetting("Sent", false);
    private final ButtonSetting ignoreC00 = new ButtonSetting("Ignore C00", false);
    private final ButtonSetting ignoreC03 = new ButtonSetting("Ignore C03", false);
    private final ButtonSetting compactC03 = new ButtonSetting("Compact C03", false);
    private final ButtonSetting ignoreC0F = new ButtonSetting("Ignore C0F", false);
    private final ButtonSetting received = new ButtonSetting("Received", false);

    private Packet<?> lastPacket;
    private long tick;

    private static final String ITEM = "&7Item: &b";
    private static final String POS = "&7Position: &b";
    private static final String DIR = "&7Direction: &b";
    private static final String OFF = "&7Offset: &b";
    private static final String ACT = "\n&7Action: &b";
    private static final String SEP = "&7, &b";
    private static final String MC = "minecraft:";

    public ViewPackets() {
        super("PacketViewer", category.other);
        registerSetting(includeCancelled, sent, ignoreC0F, ignoreC03, ignoreC00, received, compactC03);
    }

    public void onDisable() {
        lastPacket = null;
        tick = 0;
    }

    private static String boolColor(boolean b) {
        return b ? "&atrue" : "&cfalse";
    }

    private void sendMessage(Packet<?> packet, boolean receivedPacket) {
        if (Utils.isSingleplayer()) {
            singleplayerMessage();
            return;
        }
        if (Utils.isnull()) return;
        String base = receivedPacket ? ("&a" + packet.getClass().getSimpleName()) : getPacketInfo(packet);
        String color = (compactC03.isToggled() && packet instanceof C03PacketPlayer) ? "&6" : "&d";
        String label = color + packet.getClass().getSimpleName();

        ChatComponentText msg = new ChatComponentText(Utils.formatColor("&7[&dR&7]&r &7" + (receivedPacket ? "Received" : "Sent") + " packet (t:&b" + tick + "&7): "));
        ChatStyle style = new ChatStyle();
        style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(Utils.formatColor(base))));
        msg.appendSibling(new ChatComponentText(Utils.formatColor(label)).setChatStyle(style));
        mc.thePlayer.addChatMessage(msg);
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (!sent.isToggled()) return;
        if (Utils.isSingleplayer()) {
            singleplayerMessage();
            return;
        }
        Packet<?> p = e.getNonStaticPacket();
        if (e.isCanceled() && !includeCancelled.isToggled()) return;
        if (ignoreC00.isToggled() && p instanceof C00PacketKeepAlive) return;
        if (ignoreC0F.isToggled() && p instanceof C0FPacketConfirmTransaction) return;

        if (p instanceof C03PacketPlayer) {
            if (ignoreC03.isToggled()) return;
            if (compactC03.isToggled() && (lastPacket == null || lastPacket instanceof C03PacketPlayer)) return;
        }

        lastPacket = p;
        sendMessage(p, false);
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (Utils.isSingleplayer()) {
            singleplayerMessage();
            return;
        }
        if (!received.isToggled()) return;

        Packet<?> p = e.getNonStaticPacket();

        sendMessage(p, true);
    }

    private String getPacketInfo(Packet<?> p) {
        String s = "&a" + p.getClass().getSimpleName();

        if (p instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging pk = (C07PacketPlayerDigging) p;
            BlockPos pos = pk.getPosition();
            s += "\n&7Status: &b" + pk.getStatus().name() + "\n&7Facing: &b" + pk.getFacing().name();
            s += "\n" + POS + pos.getX() + SEP + pos.getY() + SEP + pos.getZ();
        } else if (p instanceof C09PacketHeldItemChange) {
            s += "\nSwap to slot: &b" + ((C09PacketHeldItemChange) p).getSlotId();
        } else if (p instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction pk = (C0BPacketEntityAction) p;
            s += ACT + pk.getAction().name() + "\n&7Aux data: &b" + pk.getAuxData();
        } else if (p instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement pk = (C08PacketPlayerBlockPlacement) p;
            BlockPos pos = pk.getPosition();
            s += "\n" + ITEM + (pk.getStack() == null ? "null" : pk.getStack().getItem().getRegistryName().replace(MC, ""));
            s += "\n" + DIR + pk.getPlacedBlockDirection();
            s += "\n" + POS + pos.getX() + SEP + pos.getY() + SEP + pos.getZ();
            s += "\n" + OFF + round(pk.getPlacedBlockOffsetX()) + SEP + round(pk.getPlacedBlockOffsetY()) + SEP + round(pk.getPlacedBlockOffsetZ());
        } else if (p instanceof C02PacketUseEntity) {
            C02PacketUseEntity pk = (C02PacketUseEntity) p;
            Entity e = pk.getEntityFromWorld(mc.theWorld);
            Vec3 hitVec = pk.getHitVec();
            s += ACT + pk.getAction().name();
            s += "\n&7Target: &b" + (e == null ? "null" : e.getName());
            s += "\n&7Hit vec: &b" + (hitVec == null ? "null" : round(hitVec.xCoord) + SEP + round(hitVec.yCoord) + SEP + round(hitVec.zCoord));
        } else if (p instanceof C01PacketChatMessage) {
            s += "\n&7Length: &b" + ((C01PacketChatMessage) p).getMessage().length();
        } else if (p instanceof C17PacketCustomPayload) {
            s += "\n&7Channel: &b" + ((C17PacketCustomPayload) p).getChannelName();
        } else if (p instanceof C15PacketClientSettings) {
            C15PacketClientSettings pk = (C15PacketClientSettings) p;
            s += "\n&7Language: &b" + pk.getLang() + "\n&7Chat visibility: &b" + pk.getChatVisibility().name();
        } else if (p instanceof C00PacketKeepAlive) {
            s += "\n&7Key: &b" + ((C00PacketKeepAlive) p).getKey();
        } else if (p instanceof C16PacketClientStatus) {
            s += "\n&7Status: &b" + ((C16PacketClientStatus) p).getStatus().name();
        } else if (p instanceof C10PacketCreativeInventoryAction) {
            C10PacketCreativeInventoryAction pk = (C10PacketCreativeInventoryAction) p;
            s += "\n&7Slot: &b" + pk.getSlotId();
            s += "\n" + ITEM + (pk.getStack() == null ? "null" : pk.getStack().getItem().getRegistryName().replace(MC, ""));
        } else if (p instanceof C0EPacketClickWindow) {
            C0EPacketClickWindow pk = (C0EPacketClickWindow) p;
            s += "\n&7Window: &b" + pk.getWindowId();
            s += "\n&7Slot: &b" + pk.getSlotId();
            s += "\n&7Button: &b" + pk.getUsedButton();
            s += ACT + pk.getActionNumber();
            s += "\n&7Mode: &b" + pk.getMode();
            s += "\n" + ITEM + (pk.getClickedItem() == null ? "null" : pk.getClickedItem().getItem().getRegistryName().replace(MC, ""));
        } else if (p instanceof C0FPacketConfirmTransaction) {
            s += "\n&7Window: &b" + ((C0FPacketConfirmTransaction) p).getWindowId();
            s += "\n&7Uid: &b" + ((C0FPacketConfirmTransaction) p).getUid();
        } else if (p instanceof C03PacketPlayer) {
            C03PacketPlayer pk = (C03PacketPlayer) p;
            s += "\n" + POS + round(pk.getPositionX()) + SEP + round(pk.getPositionY()) + SEP + round(pk.getPositionZ());
            s += "\n&7Rotations: &b" + round(pk.getYaw()) + SEP + round(pk.getPitch());
            s += "\n&7Ground: " + boolColor(pk.isOnGround());
            s += "\n&7Moving: " + boolColor(pk.isMoving());
            s += "\n&7Rotating: " + boolColor(pk.getRotating());
        }

        return s + "\n&7Client tick: &e" + tick;
    }

    private static double round(double n) {
        return Utils.rnd(n, 3);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (Utils.isSingleplayer()) {
            singleplayerMessage();
            return;
        }

        if (e.phase == TickEvent.Phase.START) tick++;
    }

    private void singleplayerMessage() {
        Utils.sendMessage("§c You're in singleplayer! (Crash Fix)");
    }
}
