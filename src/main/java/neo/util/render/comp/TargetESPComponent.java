package neo.util.render.comp;

import neo.util.Utils;
import neo.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;


public class TargetESPComponent {

    private static final ResourceLocation glowCircle = new ResourceLocation("neo", "textures/glow_circle.png");
    private static final long lastTime = System.currentTimeMillis();

    public static void render(int mode, Vec3 pos, Color color, float speed) {
        Minecraft mc = Minecraft.getMinecraft();
        switch (mode) {
            case 0:
                renderSigma(pos, color, speed, Utils.getTimer().renderPartialTicks, mc);
                break;
            case 1:
                renderVape(pos, color, color.getAlpha() / 255f, Utils.getTimer().renderPartialTicks, mc);
                break;
            case 2:
                renderRaven(pos, color);
                break;
            case 3:
                renderVape(pos, color, color.getAlpha() / 255f, Utils.getTimer().renderPartialTicks, mc);
                renderSigma(pos, color, speed, Utils.getTimer().renderPartialTicks, mc);
                break;
            case 4:
                float mappedSpeed = mapSpeed(speed);
                renderGhost(pos, mappedSpeed, color, glowCircle, false, mc);
                break;
            default:
                throw new IllegalArgumentException("Invalid mode: " + mode);
        }
    }


    public static void renderRaven(Vec3 pos, Color colori) {
        Minecraft mc = Minecraft.getMinecraft();
        int type = 2;
        double expand = 0.0;
        int color = colori.getRGB();

        float partialTicks = Utils.getTimer().renderPartialTicks;

        double width = 0.6;
        double height = 1.8;

        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;

        float d = (float) expand / 40.0F;

        GlStateManager.pushMatrix();

        if (type == 3) {
            GL11.glTranslated(x, y - 0.2D, z);
            GL11.glRotated(-mc.getRenderManager().playerViewY, 0.0D, 1.0D, 0.0D);
            GlStateManager.disableDepth();
            GL11.glScalef(0.03F + d, 0.03F + d, 0.03F + d);
            int outline = Color.black.getRGB();
            net.minecraft.client.gui.Gui.drawRect(-20, -1, -26, 75, outline);
            net.minecraft.client.gui.Gui.drawRect(20, -1, 26, 75, outline);
            net.minecraft.client.gui.Gui.drawRect(-20, -1, 21, 5, outline);
            net.minecraft.client.gui.Gui.drawRect(-20, 70, 21, 75, outline);
            if (color != 0) {
                net.minecraft.client.gui.Gui.drawRect(-21, 0, -25, 74, color);
                net.minecraft.client.gui.Gui.drawRect(21, 0, 25, 74, color);
                net.minecraft.client.gui.Gui.drawRect(-21, 0, 24, 4, color);
                net.minecraft.client.gui.Gui.drawRect(-21, 71, 25, 74, color);
            } else {
                int st = Utils.getChroma(2L, 0L);
                int en = Utils.getChroma(2L, 1000L);
                RenderUtils.dGR(-21, 0, -25, 74, st, en);
                RenderUtils.dGR(21, 0, 25, 74, st, en);
                net.minecraft.client.gui.Gui.drawRect(-21, 0, 21, 4, en);
                net.minecraft.client.gui.Gui.drawRect(-21, 71, 21, 74, st);
            }
            GlStateManager.enableDepth();
        } else {
            if (color == 0) {
                color = Utils.getChroma(2L, 0L);
            }

            float a = (float) (color >> 24 & 255) / 255.0F;
            float r = (float) (color >> 16 & 255) / 255.0F;
            float g = (float) (color >> 8 & 255) / 255.0F;
            float b = (float) (color & 255) / 255.0F;

            // Hardcoded box around pos, like entity bounding box with some padding
            AxisAlignedBB bbox = new AxisAlignedBB(
                    x - width / 2 - expand,
                    y,
                    z - width / 2 - expand,
                    x + width / 2 + expand,
                    y + height + expand,
                    z + width / 2 + expand
            );

            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glLineWidth(2.0F);
            GL11.glColor4f(r, g, b, a);

            if (type == 1) {
                RenderGlobal.drawSelectionBoundingBox(bbox);
            } else if (type == 2) {
                RenderUtils.drawBoundingBox(bbox, r, g, b);
            }

            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
        }

        GlStateManager.popMatrix();
    }


    public static void renderSigma(Vec3 pos, Color color, float speed, float partialTicks, Minecraft mc) {
        int cycle = (int) (speed * 2);
        int drawTime = (int) (System.currentTimeMillis() % cycle);
        boolean drawMode = drawTime > speed;
        float drawPercent = (float) drawTime / speed;

        if (!drawMode) drawPercent = 1 - drawPercent;
        else drawPercent -= 1;

        drawPercent *= 2;

        if (drawPercent < 1)
            drawPercent = 0.5f * drawPercent * drawPercent * drawPercent;
        else {
            float f = drawPercent - 2;
            drawPercent = 0.5f * (f * f * f + 2);
        }

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

        double width = 0.6;
        double height = 1.8 + 0.1;
        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY + height * drawPercent;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;
        double eased = (height / 3) * ((drawPercent > 0.5) ? 1 - drawPercent : drawPercent) * (drawMode ? -1 : 1);

        for (int segments = 0; segments < 360; segments += 5) {
            double x1 = x - Math.sin(Math.toRadians(segments)) * width;
            double z1 = z + Math.cos(Math.toRadians(segments)) * width;
            double x2 = x - Math.sin(Math.toRadians(segments - 5)) * width;
            double z2 = z + Math.cos(Math.toRadians(segments - 5)) * width;

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0f);
            GL11.glVertex3d(x1, y + eased, z1);
            GL11.glVertex3d(x2, y + eased, z2);
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
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

    public static void renderVape(Vec3 pos, Color color, float alpha, float partialTicks, Minecraft mc) {
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

        double width = 0.6;
        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;
        double height = 1.8;
        double eased = height - 0.2;

        for (int segments = 0; segments < 360; segments += 5) {
            double x1 = x - Math.sin(Math.toRadians(segments)) * width;
            double z1 = z + Math.cos(Math.toRadians(segments)) * width;
            double x2 = x - Math.sin(Math.toRadians(segments - 5)) * width;
            double z2 = z + Math.cos(Math.toRadians(segments - 5)) * width;

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
            GL11.glVertex3d(x1, y, z1);
            GL11.glVertex3d(x2, y, z2);
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0f);
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


    public static void renderGhost(Vec3 pos, float speed, Color color, ResourceLocation glowCircle, boolean white, Minecraft mc) {
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
        int length = 20;

        double y = pos.yCoord + 0.75f;

        RenderUtils.setupOrientationMatrix(pos.xCoord, y + 0.5f, pos.zCoord);

        float[] angles = new float[]{mc.getRenderManager().playerViewY, mc.getRenderManager().playerViewX};

        GL11.glRotated(-angles[0], 0.0, 1.0, 0.0);
        GL11.glRotated(angles[1], 1.0, 0.0, 0.0);

        long now = System.currentTimeMillis();

        for (int i = 0; i < length; i++) {
            double angle = 0.15f * (now - lastTime - (i * distance)) / speed;
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;

            GlStateManager.translate(s, c, -c);
            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);

            int cColor = color.getRGB();
            if (white) cColor = 0xFFFFFFFF;

            RenderUtils.drawImage(glowCircle, 0f, 0f, -size, -size, cColor);

            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);
            GlStateManager.translate(-s, -c, c);
        }

        for (int i = 0; i < length; i++) {
            double angle = 0.15f * (now - lastTime - (i * distance)) / speed;
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;

            GlStateManager.translate(-s, s, -c);
            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);

            int cColor = color.getRGB();
            if (white) cColor = 0xFFFFFFFF;

            RenderUtils.drawImage(glowCircle, 0f, 0f, -size, -size, cColor);

            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);
            GlStateManager.translate(s, -s, c);
        }

        for (int i = 0; i < length; i++) {
            double angle = 0.15f * (now - lastTime - (i * distance)) / speed;
            double s = Math.sin(angle) * radius;
            double c = Math.cos(angle) * radius;

            GlStateManager.translate(-s, -s, c);
            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);

            int cColor = color.getRGB();
            if (white) cColor = 0xFFFFFFFF;

            RenderUtils.drawImage(glowCircle, 0f, 0f, -size, -size, cColor);

            GlStateManager.translate(-size / 2f, -size / 2f, 0);
            GlStateManager.translate(size / 2f, size / 2f, 0);
            GlStateManager.translate(s, s, -c);
        }

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }



    static float mapSpeed(float input) {
        if (input <= 250f) return 15f;
        if (input <= 500f) return 25f;
        if (input <= 750f) return 35f;
        if (input <= 850f) return 45f;
        if (input <= 1000f) return 65f;
        return 70f;
    }

    public static float getSpeed(int index) {
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

