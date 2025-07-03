package neo.module.impl.render;

import neo.module.Module;
import neo.module.impl.combat.KillAura;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.other.MathUtil;
import neo.util.render.RenderUtils;
import neo.util.render.Theme;
import neo.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import java.awt.*;


public class TargetESP extends Module {

    private final SliderSetting alpha;
    private final String[] modes = new String[]{"Ring", "Vape", "Raven", "Both", "Ghost"};
    private final String[] speeds = new String[]{"250", "500", "750", "850", "1000", "1250"};
    private final SliderSetting mode;
    private final SliderSetting speed;
    public static SliderSetting theme;
    public static ButtonSetting white;
    public Color color;
    private final ResourceLocation glowCircle = new ResourceLocation("neo", "textures/glow_circle.png");
    private final long lastTime = System.currentTimeMillis();

    public TargetESP() {
        super("TargetESP", category.render);
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(speed = new SliderSetting("Speed", speeds, 0));
        this.registerSetting(alpha = new SliderSetting("Alpha", 200, 0, 255, 5));
        this.registerSetting(white = new ButtonSetting("White", false));
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (KillAura.target == null) return;

        color = RenderUtils.toColor(RenderUtils.toArgb(RenderUtils.toRgbColor(Theme.getGradient(theme.getInput(), 0.0)), alpha.getInput()));
        if (white.isToggled()) {
            color = Color.WHITE;
        }
        switch ((int) mode.getInput()) {
            case 0:
                renderSigma(KillAura.target, getSpeedByIndex((int) speed.getInput()));
                break;
            case 1:
                renderVape(KillAura.target);
                break;
            case 2:
                renderRaven(KillAura.target);
                break;
            case 3:
                renderVape(KillAura.target);
                renderSigma(KillAura.target, getSpeedByIndex((int) speed.getInput()));
                break;
            case 4:
                renderGhost(KillAura.target, e.partialTicks, mapSpeed(getSpeedByIndex((int) speed.getInput())));
                break;
            default:
                Utils.sendDebugMessage("Invalid mode: " + mode.getInput());
                break;
        }
    }

    public void renderRaven(@NotNull EntityLivingBase target) {
        RenderUtils.renderEntity(target, 2, 0.0, 0.0, Theme.getGradient((int) theme.getInput(), 0), target.hurtTime != 0);
    }

    public void renderSigma(@NotNull EntityLivingBase target, float speed) {
        int cycle = (int) (speed * 2);

        int drawTime = (int) (System.currentTimeMillis() % cycle);
        boolean drawMode = drawTime > speed;
        float drawPercent = drawTime / speed;

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
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (float) (alpha.getInput() / 255f));
            GL11.glVertex3d(x1, y, z1);
            GL11.glVertex3d(x2, y, z2);
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0);
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

    public void renderGhost(@NotNull EntityLivingBase target, float ep, float speed) {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.shadeModel(7425);
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 1, 0, 1);
        double radius = 0.67;
        float size = 0.4f;
        double distance = 19;
        int lenght = 20;

        Vec3 interpolated = MathUtil.interpolate(new Vec3(target.lastTickPosX, target.lastTickPosY, target.lastTickPosZ), target.getPositionVector(), ep);
        double y = interpolated.yCoord + 0.75f;

        RenderUtils.setupOrientationMatrix(interpolated.xCoord, y + 0.5f, interpolated.zCoord);

        float[] idk = new float[]{mc.getRenderManager().playerViewY, mc.getRenderManager().playerViewX};

        GL11.glRotated(-idk[0], 0.0, 1.0, 0.0);
        GL11.glRotated(idk[1], 1.0, 0.0, 0.0);

        for (int i = 0; i < lenght; i++) {
            double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;
            GlStateManager.translate(s, (c), -c);
            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);
            int color = Theme.getGradient((int) theme.getInput(), 0);
            if (white.isToggled()) {
                color = 0xFFFFFFFF;
            }
            RenderUtils.drawImage(glowCircle, 0f, 0f, -size, -size, color);
            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);
            GlStateManager.translate(-(s), -(c), (c));
        }
        for (int i = 0; i < lenght; i++) {
            double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;
            GlStateManager.translate(-s, s, -c);
            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);
            int color = Theme.getGradient((int) theme.getInput(), 0);
            if (white.isToggled()) {
                color = 0xFFFFFFFF;
            }
            RenderUtils.drawImage(glowCircle, 0f, 0f, -size, -size, color);
            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);
            GlStateManager.translate((s), -(s), (c));
        }
        for (int i = 0; i < lenght; i++) {
            double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;
            GlStateManager.translate(-(s), -(s), (c));
            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);
            int color = Theme.getGradient((int) theme.getInput(), 0);
            if (white.isToggled()) {
                color = 0xFFFFFFFF;
            }
            RenderUtils.drawImage(glowCircle, 0f, 0f, -size, -size, color);
            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);
            GlStateManager.translate((s), (s), -(c));
        }
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    float mapSpeed(float input) {
        if (input <= 250f) return 15f;
        if (input <= 500f) return 25f;
        if (input <= 750f) return 35f;
        if (input <= 850f) return 45f;
        if (input <= 1000f) return 65f;
        return 70f;
    }

    public float getSpeedByIndex(int index) {
        switch (index) {
            case 0: return 250f;
            case 1: return 500f;
            case 2: return 750f;
            case 3: return 850f;
            case 4: return 1000f;
            case 5: return 1250f;
            default: throw new IndexOutOfBoundsException("ewwor");
        }
    }


}