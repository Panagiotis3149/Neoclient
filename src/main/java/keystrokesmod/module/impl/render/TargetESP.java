package keystrokesmod.module.impl.render;

import keystrokesmod.event.Render2DEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import java.awt.*;


public class TargetESP extends Module {

    private final SliderSetting normalRed;
    private final SliderSetting normalGreen;
    private final SliderSetting normalBlue;
    private final SliderSetting normalAlpha;
    private final SliderSetting hitRed;
    private final SliderSetting hitGreen;
    private final SliderSetting hitBlue;
    private final SliderSetting hitAlpha;
    private String[] modes = new String[]{"Sigma", "Ring", "Raven"};
    private final SliderSetting mode;
    private final SliderSetting theme;
    Color color = new Color(255, 255, 255, 128);

    public TargetESP() {
        super("TargetESP", category.render);
        this.registerSetting(new DescriptionSetting("Normal color"));
        this.registerSetting(normalRed = new SliderSetting("Normal red", 100, 0, 255, 1));
        this.registerSetting(normalGreen = new SliderSetting("Normal green", 100, 0, 255, 1));
        this.registerSetting(normalBlue = new SliderSetting("Normal blue", 190, 0, 255, 1));
        this.registerSetting(normalAlpha = new SliderSetting("Normal alpha", 100, 0, 255, 1));
        this.registerSetting(new DescriptionSetting("Hit color"));
        this.registerSetting(hitRed = new SliderSetting("Hit red", 255, 0, 255, 1));
        this.registerSetting(hitGreen = new SliderSetting("Hit green", 0, 0, 255, 1));
        this.registerSetting(hitBlue = new SliderSetting("Hit blue", 0, 0, 255, 1));
        this.registerSetting(hitAlpha = new SliderSetting("Hit alpha", 100, 0, 255, 1));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
    }

    @Override
    public void onUpdate() {
        color = new Color((int) normalRed.getInput(), (int) normalGreen.getInput(), (int) normalBlue.getInput(), (int) normalAlpha.getInput());
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (KillAura.target == null) return;
        switch ((int) mode.getInput()) {
            case 0:
                renderSigma(KillAura.target);
                break;
            case 1:
                renderVape(KillAura.target);
                break;
            case 2:
                renderRaven(KillAura.target);
                break;
            default:
                Utils.sendDebugMessage("Invalid mode: " + mode.getInput());
                break;
        }
    }

    public void renderRaven(@NotNull EntityLivingBase target) {
        RenderUtils.renderEntity(target, 2, 0.0, 0.0, Theme.getGradient((int) theme.getInput(), 0), target.hurtTime != 0);
    }

    public void renderSigma(@NotNull EntityLivingBase target) {
        int drawTime = (int) (System.currentTimeMillis() % 2000);
        boolean drawMode = drawTime > 1000;
        float drawPercent = drawTime / 1000f;

        if (!drawMode) {
            drawPercent = 1 - drawPercent;
        } else {
            drawPercent -= 1;
        }

        drawPercent = drawPercent * 2;

        if (drawPercent < 1) {
            drawPercent = 0.5f * drawPercent * drawPercent * drawPercent;
        } else {
            float f = drawPercent - 2;
            drawPercent = 0.5f * (f * f * f + 2);
        }

        Minecraft mc = Minecraft.getMinecraft();
        mc.entityRenderer.disableLightmap();
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        mc.entityRenderer.disableLightmap();

        double radius = target.width;
        double height = target.height + 0.1;
        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosX;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosY + height * drawPercent;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosZ;
        double eased = (height / 3) * ((drawPercent > 0.5) ? 1 - drawPercent : drawPercent) * ((drawMode) ? -1 : 1);

        for (int segments = 0; segments < 360; segments += 5) {

            double x1 = x - Math.sin(segments * Math.PI / 180F) * radius;
            double z1 = z + Math.cos(segments * Math.PI / 180F) * radius;
            double x2 = x - Math.sin((segments - 5) * Math.PI / 180F) * radius;
            double z2 = z + Math.cos((segments - 5) * Math.PI / 180F) * radius;

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 0.0f);
            GL11.glVertex3d(x1, y + eased, z1);
            GL11.glVertex3d(x2, y + eased, z2);
            GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
            GL11.glVertex3d(x2, y, z2);
            GL11.glVertex3d(x1, y, z1);
            GL11.glEnd();
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex3d(x2, y, z2);
            GL11.glVertex3d(x1, y, z1);
            GL11.glEnd();
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    public void renderVape(@NotNull EntityLivingBase target) {
        mc.entityRenderer.disableLightmap();
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        mc.entityRenderer.disableLightmap();

        double radius = target.width;
        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosX;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosY;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosZ;
        double eased = target.height - 0.2;

        for (int segments = 0; segments < 360; segments += 5) {

            double x1 = x - Math.sin(segments * Math.PI / 180F) * radius;
            double z1 = z + Math.cos(segments * Math.PI / 180F) * radius;
            double x2 = x - Math.sin((segments - 5) * Math.PI / 180F) * radius;
            double z2 = z + Math.cos((segments - 5) * Math.PI / 180F) * radius;

            GL11.glBegin(GL11.GL_QUADS);
            if (target.hurtTime > 0) {
                GL11.glColor4f((float) (hitRed.getInput() / 255f), (float) (hitGreen.getInput() / 255f), (float) (hitBlue.getInput() / 255f), (float) (hitAlpha.getInput() / 255f));
            } else {
                GL11.glColor4f((float) (normalRed.getInput() / 255f), (float) (normalGreen.getInput() / 255f), (float) (normalBlue.getInput() / 255f), (float) (normalAlpha.getInput() / 255f));
            }
            GL11.glVertex3d(x1, y, z1);
            GL11.glVertex3d(x2, y, z2);
            if (target.hurtTime > 0) {
                GL11.glColor4f((float) (hitRed.getInput() / 255f), (float) (hitGreen.getInput() / 255f), (float) (hitBlue.getInput() / 255f), 0);
            } else {
                GL11.glColor4f((float) (normalRed.getInput() / 255f), (float) (normalGreen.getInput() / 255f), (float) (normalBlue.getInput() / 255f), 0);
            }
            GL11.glVertex3d(x2, y + eased, z2);
            GL11.glVertex3d(x1, y + eased, z1);
            GL11.glEnd();
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex3d(x2, y + eased, z2);
            GL11.glVertex3d(x1, y + eased, z1);
            GL11.glEnd();
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}