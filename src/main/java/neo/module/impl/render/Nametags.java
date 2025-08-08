package neo.module.impl.render;

import neo.module.Module;
import neo.module.impl.player.Freecam;
import neo.module.impl.combat.AntiBot;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.render.RenderUtils;
import neo.util.Utils;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class Nametags extends Module {
    private final SliderSetting scale;
    private final ButtonSetting autoScale;
    private final ButtonSetting drawBackground;
    private final ButtonSetting renderSelf;
    private final String[] modes = new String[]{"Classic", "Modern"};
    private final SliderSetting mode;
    private final Map<EntityPlayer, double[]> entityPositions = new HashMap();
    private final int backGroundColor = new Color(0, 0, 0, 65).getRGB();
    private final int friendColor = new Color(0, 255, 0, 255).getRGB();
    private final int enemyColor = new Color(255, 0, 0, 255).getRGB();

    public Nametags() {
        super("Nametags", category.render, 0);
        registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(scale = new SliderSetting("Scale", 1.0, 0.25, 2.0, 0.05));
        this.registerSetting(autoScale = new ButtonSetting("Auto-scale", true));
        this.registerSetting(drawBackground = new ButtonSetting("Draw background", true));
        this.registerSetting(renderSelf = new ButtonSetting("Render self", false));
    }

    @SubscribeEvent
    public void onRenderTick(RenderGameOverlayEvent.Post ev) {
        if (!Utils.isnull()) {
            return;
        }
        if (ev.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        GlStateManager.pushMatrix();
        ScaledResolution scaledRes = new ScaledResolution(mc);
        double twoDScale = scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D);
        GlStateManager.scale(twoDScale, twoDScale, twoDScale);
        for (EntityPlayer entityPlayer : entityPositions.keySet()) {
            GlStateManager.pushMatrix();

            double[] renderPositions = entityPositions.get(entityPlayer);
            if ((renderPositions[3] < 0) || (renderPositions[3] >= 1)) {
                GlStateManager.popMatrix();
                continue;
            }

            GlStateManager.translate(renderPositions[0], renderPositions[1], 0);

            double rawScaleSetting = scale.getInput();
            double scaleSetting = rawScaleSetting * 10;
            double nameTagScale = scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0D) * scaleSetting;

            final float renderPartialTicks = Utils.getTimer().renderPartialTicks;
            final EntityPlayer player = (Freecam.freeEntity == null) ? mc.thePlayer : Freecam.freeEntity;
            final double deltaX = player.lastTickPosX + (player.posX - player.lastTickPosX) * renderPartialTicks - (entityPlayer.lastTickPosX + (entityPlayer.posX - entityPlayer.lastTickPosX) * renderPartialTicks);
            final double deltaY = player.lastTickPosY + (player.posY - player.lastTickPosY) * renderPartialTicks - (entityPlayer.lastTickPosY + (0.0) * renderPartialTicks);
            final double deltaZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * renderPartialTicks - (entityPlayer.lastTickPosZ + (entityPlayer.posZ - entityPlayer.lastTickPosZ) * renderPartialTicks);
            double distance = MathHelper.sqrt_double(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            if (!autoScale.isToggled()) {
                if (renderSelf.isToggled() && entityPlayer == mc.thePlayer) {
                    distance = 3;
                }
                nameTagScale = rawScaleSetting / (Math.max(distance, 3) / 10);
            } else {
                distance = Math.min(1, Math.max(0.7, 1 - (0.012 * Math.max(distance, 1))));
                nameTagScale *= distance;
            }

            GlStateManager.scale(nameTagScale, nameTagScale, nameTagScale);

            FontRenderer font = FontManager.productSansLight18;
            String name = entityPlayer.getDisplayName().getFormattedText() + " " + Utils.getHealthStr(entityPlayer);
            int strWidth = mc.fontRendererObj.getStringWidth(name) / 2;
            int strWidthi = (int) (font.getStringWidth(name) / 2);

            int x1 = -strWidth - 1;
            int y1 = -10;
            int x2 = strWidth + 1;
            int y2 = 8 - 9;
            int x1a = -strWidthi - 1;
            int y1a = -10;
            int x2a = strWidthi + 1;
            int y2a = 8 - 9;

            switch ((int) mode.getInput()) {
                case 0:
                    if (drawBackground.isToggled()) {
                        RenderUtils.drawRect(x1, y1, x2, y2, backGroundColor);
                    }
                    if (Utils.isFriended(entityPlayer)) {
                        RenderUtils.drawOutline(x1, y1, x2, y2, 2, friendColor);
                    } else if (Utils.isEnemy(entityPlayer)) {
                        RenderUtils.drawOutline(x1, y1, x2, y2, 2, enemyColor);
                    }
                    mc.fontRendererObj.drawString(name, -strWidth, -9, -1, true);
                    break;
                case 1:
                    if (drawBackground.isToggled()) {
                        RoundedUtils.drawRound((float) x1a, y1a, x2a - x1a, y2a - y1a , 4, new Color(0, 0, 0, 148).getRGB());
                    }
                    font.drawString(name, -strWidthi, -9, -1, false);
                    break;
                default:
                    mc.fontRendererObj.drawString(name, -strWidth, -9, -1, false);
                    break;
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent renderWorldLastEvent) {
        if (!Utils.isnull()) {
            return;
        }
        updatePositions();
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre e) {
        if (e.entity instanceof EntityPlayer && (e.entity != mc.thePlayer || renderSelf.isToggled()) && e.entity.deathTime == 0) {
            final EntityPlayer entityPlayer = (EntityPlayer) e.entity;
            if (entityPlayer.getDisplayNameString().isEmpty() || (entityPlayer != mc.thePlayer && AntiBot.isBot(entityPlayer))) {
                return;
            }
            e.setCanceled(true);
        }
    }

    private void updatePositions() {
        entityPositions.clear();
        final float pTicks = Utils.getTimer().renderPartialTicks;
        for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            if (entityPlayer == mc.thePlayer && (!renderSelf.isToggled() || mc.gameSettings.thirdPersonView == 0)) {
                continue;
            }
            if (entityPlayer.getDisplayNameString().isEmpty() || (entityPlayer != mc.thePlayer && AntiBot.isBot(entityPlayer))) {
                continue;
            }

            double interpolatedX = entityPlayer.lastTickPosX + (entityPlayer.posX - entityPlayer.lastTickPosX) * pTicks - mc.getRenderManager().viewerPosX;
            double interpolatedY = entityPlayer.lastTickPosY + (entityPlayer.posY - entityPlayer.lastTickPosY) * pTicks - mc.getRenderManager().viewerPosY;
            double interpolatedZ = entityPlayer.lastTickPosZ + (entityPlayer.posZ - entityPlayer.lastTickPosZ) * pTicks - mc.getRenderManager().viewerPosZ;

            interpolatedY += entityPlayer.isSneaking() ? entityPlayer.height - 0.05 : entityPlayer.height + 0.27;

            double[] convertedPosition = convertTo2D(interpolatedX, interpolatedY, interpolatedZ);
            if (convertedPosition == null) {
                continue;
            }
            if (convertedPosition[2] >= 0.0D && convertedPosition[2] < 1.0D) {
                double[] headConvertedPosition = convertTo2D(interpolatedX, interpolatedY + 1.0D, interpolatedZ);
                double height = Math.abs(headConvertedPosition[1] - convertedPosition[1]);
                entityPositions.put(entityPlayer, new double[]{convertedPosition[0], convertedPosition[1], height, convertedPosition[2]});
            }
        }
    }

    public static double[] convertTo2D(double x, double y, double z) {
        FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(2982, modelView);
        GL11.glGetFloat(2983, projection);
        GL11.glGetInteger(2978, viewport);
        boolean result = GLU.gluProject((float)x, (float)y, (float)z, modelView, projection, viewport, screenCoords);
        if (result) {
            return new double[] { screenCoords.get(0), org.lwjgl.opengl.Display.getHeight() - screenCoords.get(1), screenCoords.get(2) };
        }
        return null;
    }
}
