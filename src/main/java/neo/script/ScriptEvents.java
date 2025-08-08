package neo.script;

import neo.Neo;
import neo.event.*;
import neo.module.Module;
import neo.script.classes.Entity;
import neo.script.classes.PlayerState;
import neo.script.packets.clientbound.SPacket;
import neo.script.packets.serverbound.CPacket;
import neo.script.packets.serverbound.PacketHandler;
import neo.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ScriptEvents {
    public Module module;

    public ScriptEvents(Module module) {
        this.module = module;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
        if (e.type == 2 || !Utils.isnull()) {
            return;
        }
        final String r = Utils.stripColor(e.message.getUnformattedText());
        if (r.isEmpty()) {
            return;
        }
        if (Neo.scriptManager.invokeBoolean("onChat", module, e.message.getUnformattedText()) == 0) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (e.isCanceled() || SendPacketEvent.getPacket() == null) {
            return;
        }
        if (SendPacketEvent.getPacket().getClass().getSimpleName().startsWith("S")) {
            return;
        }
        CPacket a = PacketHandler.convertServerBound(SendPacketEvent.getPacket());
        if (a != null && Neo.scriptManager.invokeBoolean("onPacketSent", module, a) == 0) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (e.isCanceled() || ReceivePacketEvent.getPacket() == null) {
            return;
        }
        SPacket a = PacketHandler.convertClientBound(ReceivePacketEvent.getPacket());
        if (a != null && Neo.scriptManager.invokeBoolean("onPacketReceived", module, a) == 0) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent e) {
        if (!Utils.isnull()) {
            return;
        }
        Neo.scriptManager.invoke("onRenderWorld", module, e.partialTicks);
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        Neo.scriptManager.invoke("onPreUpdate", module);
    }

    @SubscribeEvent
    public void onPostUpdate(PostUpdateEvent e) {
        Neo.scriptManager.invoke("onPostUpdate", module);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !Utils.isnull()) {
            return;
        }
        Neo.scriptManager.invoke("onRenderTick", module, e.renderTickTime);
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) {
        PlayerState playerState = new PlayerState(e);
        Neo.scriptManager.invoke("onPreMotion", module, playerState);
        if (e.isEquals(playerState)) {
            return;
        }
        if (e.getYaw() != playerState.yaw) {
            e.setYaw(playerState.yaw);
        }
        e.setPitch(playerState.pitch);
        e.setPosX(playerState.x);
        e.setPosY(playerState.y);
        e.setPosZ(playerState.z);
        e.setOnGround(playerState.onGround);
        e.setSprinting(playerState.isSprinting);
        e.setSneaking(playerState.isSneaking);
    }

    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent e) {
        if (e.entity == null) {
            return;
        }
        if (e.entity == Minecraft.getMinecraft().thePlayer) {
            Neo.scriptManager.invoke("onWorldJoin", module, ScriptDefaults.client.getPlayer());
            ScriptManager.localPlayer = new Entity(Minecraft.getMinecraft().thePlayer);
            return;
        }
        Neo.scriptManager.invoke("onWorldJoin", module, new Entity(e.entity));
    }

    @SubscribeEvent
    public void onPostInput(PostPlayerInputEvent e) {
        Neo.scriptManager.invoke("onPostPlayerInput", module);
    }

    @SubscribeEvent
    public void onPostMotion(PostMotionEvent e) {
        Neo.scriptManager.invoke("onPostMotion", module);
    }

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (Neo.scriptManager.invokeBoolean("onMouse", module, e.button, e.buttonstate) == 0) {
            e.setCanceled(true);
        }
    }
}
