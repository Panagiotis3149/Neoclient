package neo.util.packet;

import neo.event.PostUpdateEvent;
import neo.event.ReceivePacketEvent;
import neo.event.SendPacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BadPacketsHandler { // ensures you don't get banned
    public boolean C08;
    public boolean C07;
    private boolean C02;
    public boolean C09;
    public boolean delayAttack;
    public boolean delay;
    public int playerSlot = -1;
    public int serverSlot = -1;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSendPacket(SendPacketEvent e) {
        if (e.isCanceled()) {
            return;
        }
        if (SendPacketEvent.getPacket() instanceof C02PacketUseEntity) { // sending a C07 on the same tick as C02 can ban, this usually happens when you unblock and attack on the same tick
            if (C07) {
                e.setCanceled(true);
                return;
            }
            C02 = true;
        }
        else if (SendPacketEvent.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            C08 = true;
        }
        else if (SendPacketEvent.getPacket() instanceof C07PacketPlayerDigging) {
            C07 = true;
        }
        else if (SendPacketEvent.getPacket() instanceof C09PacketHeldItemChange) {
            if (((C09PacketHeldItemChange) SendPacketEvent.getPacket()).getSlotId() == playerSlot && ((C09PacketHeldItemChange) SendPacketEvent.getPacket()).getSlotId() == serverSlot) {
                e.setCanceled(true);
                return;
            }
            C09 = true;
            serverSlot = playerSlot = ((C09PacketHeldItemChange) SendPacketEvent.getPacket()).getSlotId();
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (ReceivePacketEvent.getPacket() instanceof S09PacketHeldItemChange) {
            S09PacketHeldItemChange packet = (S09PacketHeldItemChange) ReceivePacketEvent.getPacket();
            if (packet.getHeldItemHotbarIndex() >= 0 && packet.getHeldItemHotbarIndex() < InventoryPlayer.getHotbarSize()) {
                serverSlot = packet.getHeldItemHotbarIndex();
            }
        }
        else if (ReceivePacketEvent.getPacket() instanceof S0CPacketSpawnPlayer && Minecraft.getMinecraft().thePlayer != null) {
            if (((S0CPacketSpawnPlayer) ReceivePacketEvent.getPacket()).getEntityID() != Minecraft.getMinecraft().thePlayer.getEntityId()) {
                return;
            }
            this.playerSlot = -1;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPostUpdate(PostUpdateEvent e) {
        if (delay) {
            delayAttack = false;
            delay = false;
        }
        if (C08 || C09) {
            delay = true;
            delayAttack = true;
        }
        C08 = C07 = C02 = C09 = false;
    }
}
