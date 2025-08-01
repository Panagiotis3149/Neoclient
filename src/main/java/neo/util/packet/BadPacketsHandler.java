package neo.util.packet;

import com.mojang.realmsclient.gui.ChatFormatting;
import neo.event.PostUpdateEvent;
import neo.event.ReceivePacketEvent;
import neo.event.SendPacketEvent;
import neo.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BadPacketsHandler {

    public boolean C08, C07, C09, delayAttack, delay;
    private boolean C02;
    public int playerSlot = -1, serverSlot = -1;

    private static boolean slot, attack, swing, block, inventory;

    public static boolean bad() {
        return bad(true, true, true, true, true);
    }

    public static void onBadPacket(@NotNull Packet<?> packet, @NotNull Exception e) {
        try {
            final StringBuilder stackTraces = new StringBuilder();

                Arrays.stream(e.getStackTrace())
                        .limit(7)
                        .parallel()
                        .map(s -> "\n  " + ChatFormatting.RED + "at " + ChatFormatting.AQUA + s)
                        .forEach(stackTraces::append);

                Utils.sendMessage(String.format(
                        "%sCatch %s on processing packet <%s>.%s",
                        ChatFormatting.RED, e.getClass(), packet, stackTraces
                ));
        } catch (Throwable ignored) {
        }
    }

    public static boolean bad(final boolean slot, final boolean attack, final boolean swing, final boolean block, final boolean inventory) {
        return (BadPacketsHandler.slot && slot) ||
                (BadPacketsHandler.attack && attack) ||
                (BadPacketsHandler.swing && swing) ||
                (BadPacketsHandler.block && block) ||
                (BadPacketsHandler.inventory && inventory);
    }

    public static void reset() {
        slot = swing = attack = block = inventory = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSendPacket(SendPacketEvent e) {
        if (e.isCanceled()) return;

        Packet<?> packet = SendPacketEvent.getPacket();

        if (packet instanceof C02PacketUseEntity) {
            if (C07) {
                e.setCanceled(true);
                return;
            }
            C02 = true;
            attack = true;
        }
        else if (packet instanceof C08PacketPlayerBlockPlacement) {
            C08 = true;
            block = true;
        }
        else if (packet instanceof C07PacketPlayerDigging) {
            C07 = true;
            block = true;
        }
        else if (packet instanceof C09PacketHeldItemChange) {
            C09 = true;
            slot = true;

            int newSlot = ((C09PacketHeldItemChange) packet).getSlotId();
            if (newSlot == playerSlot && newSlot == serverSlot) {
                e.setCanceled(true);
                return;
            }
            serverSlot = playerSlot = newSlot;
        }
        else if (packet instanceof C0APacketAnimation) {
            swing = true;
        }
        else if (packet instanceof C0EPacketClickWindow ||
                (packet instanceof C16PacketClientStatus && ((C16PacketClientStatus) packet).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) ||
                packet instanceof C0DPacketCloseWindow) {
            inventory = true;
        }
        else if (packet instanceof C03PacketPlayer) {
            reset();
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        Packet<?> packet = ReceivePacketEvent.getPacket();

        if (packet instanceof S09PacketHeldItemChange) {
            int hotbar = ((S09PacketHeldItemChange) packet).getHeldItemHotbarIndex();
            if (hotbar >= 0 && hotbar < InventoryPlayer.getHotbarSize()) {
                serverSlot = hotbar;
            }
        }
        else if (packet instanceof S0CPacketSpawnPlayer && Minecraft.getMinecraft().thePlayer != null) {
            if (((S0CPacketSpawnPlayer) packet).getEntityID() != Minecraft.getMinecraft().thePlayer.getEntityId()) {
                return;
            }
            playerSlot = -1;
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
