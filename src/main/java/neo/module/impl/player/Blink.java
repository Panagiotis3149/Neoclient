package neo.module.impl.player;

import neo.event.PreUpdateEvent;
import neo.event.SendPacketEvent;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.util.packet.PacketUtils;
import neo.util.render.RenderUtils;
import neo.util.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {
    private final ButtonSetting initialPosition;
    private final List<Packet> blinkedPackets = new ArrayList<>();
    private Vec3 pos;
    private final int color = new Color(0, 255, 0).getRGB();
    private static final int colors = new Color(0, 255, 0).getRGB();
    private int blinkTicks;
    public Blink() {
        super("Blink", category.player);
        this.registerSetting(initialPosition = new ButtonSetting("Show initial position", true));
    }

    @Override
    public void onEnable() {
        blinkedPackets.clear();
        pos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    public void onDisable() {
        synchronized (blinkedPackets) {
            for (Packet packet : blinkedPackets) {
                PacketUtils.sendPacketNoEvent(packet);
            }
        }
        blinkedPackets.clear();
        pos = null;
        blinkTicks = 0;
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (!Utils.isntnull()) {
            this.disable();
            return;
        }
        Packet packet = SendPacketEvent.getPacket();
        if (packet.getClass().getSimpleName().startsWith("S")) {
            return;
        }
        if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart || packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing || packet instanceof C01PacketEncryptionResponse || packet instanceof C00PacketKeepAlive || packet instanceof C0FPacketConfirmTransaction) {
            return;
        }
        blinkedPackets.add(packet);
        e.setCanceled(true);
    }

    @Override
    public String getInfo() {
        return blinkTicks + "";
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        ++blinkTicks;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (!Utils.isntnull() || pos == null || !initialPosition.isToggled()) {
            return;
        }
        drawBox(pos);
    }

    private void drawBox(Vec3 pos) {
        GlStateManager.pushMatrix();
        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bbox = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
        AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - mc.thePlayer.posX + x, bbox.minY - mc.thePlayer.posY + y, bbox.minZ - mc.thePlayer.posZ + z, bbox.maxX - mc.thePlayer.posX + x, bbox.maxY - mc.thePlayer.posY + y, bbox.maxZ - mc.thePlayer.posZ + z);
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(r, g, b, a);
        RenderUtils.drawBoundingBox(axis, r, g, b);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GlStateManager.popMatrix();
    }

    public static void drawBoxi(Vec3 pos) {
        GlStateManager.pushMatrix();
        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bbox = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
        AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - mc.thePlayer.posX + x, bbox.minY - mc.thePlayer.posY + y, bbox.minZ - mc.thePlayer.posZ + z, bbox.maxX - mc.thePlayer.posX + x, bbox.maxY - mc.thePlayer.posY + y, bbox.maxZ - mc.thePlayer.posZ + z);
        float a = (float) (colors >> 24 & 255) / 255.0F;
        float r = (float) (colors >> 16 & 255) / 255.0F;
        float g = (float) (colors >> 8 & 255) / 255.0F;
        float b = (float) (colors & 255) / 255.0F;
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(r, g, b, a);
        RenderUtils.drawBoundingBox(axis, r, g, b);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GlStateManager.popMatrix();
    }
}

