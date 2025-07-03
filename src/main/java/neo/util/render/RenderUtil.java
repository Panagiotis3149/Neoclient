package neo.util.render;


import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static neo.Neo.mc;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_POINT_BIT;


// RenderUtil for skidded stuff, yes...

public class RenderUtil {

    public static void glColor(final Color color) {
        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }



    public void scissor(double x, double y, double width, double height) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        final ScaledResolution sr = scaledResolution;
        final double scale = sr.getScaleFactor();

        y = sr.getScaledHeight() - y;

        x *= scale;
        y *= scale;
        width *= scale;
        height *= scale;

        GL11.glScissor((int) x, (int) (y - height), (int) width, (int) height);
    }


    public static void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, alpha);
    }

    public static int interpolateColor(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return interpolateColorC(color1, color2, amount).getRGB();
    }

    private static Color interpolateColorColor(Color start, Color end, float progress) {
        int r = (int) (start.getRed() + (end.getRed() - start.getRed()) * progress);
        int g = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * progress);
        int b = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * progress);
        return new Color(r, g, b);
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        Color cColor1 = new Color(color1);
        Color cColor2 = new Color(color2);
        return interpolateColorC(cColor1, cColor2, amount).getRGB();
    }


    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(ColorUtils.interpolateInt(color1.getRed(), color2.getRed(), amount),
                ColorUtils.interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                ColorUtils.interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                ColorUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static void color(int color) {
        color(color, (float) (color >> 24 & 255) / 255.0F);
    }

    public static void drawUnfilledCircle(double x, double y, float radius, float lineWidth, int color) {
        GLUtil.setup2DRendering();
        color(color);
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINE_LOOP);

        for (int i = 0; i <= 360; i++) {
            double angle = Math.toRadians(i);
            GL11.glVertex2d(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius);
        }

        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GLUtil.end2DRendering();
    }


    /**
     * Better to use gl state manager to avoid bugs
     */
    public static void start() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
    }

    /**
     * Better to use gl state manager to avoid bugs
     */
    public static void stop() {
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }


    public static void rectangle(final double x, final double y, final double width, final double height, final Color color) {
        start();

        if (color != null) {
            glColor(color);
        }

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();

        stop();
    }

}
