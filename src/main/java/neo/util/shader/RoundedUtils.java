package neo.util.shader;

import neo.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RoundedUtils {
    public static ShaderUtils roundedShader = new ShaderUtils("roundedRect");
    public static ShaderUtils riseShader = new ShaderUtils("roundedRectRise");


    /**
     * Draws a rounded rectangle at the given coordinates with the given lengths
     *
     * @param x      The top left x coordinate of the rectangle
     * @param y      The top y coordinate of the rectangle
     * @param width  The width which is used to determine the second x rectangle
     * @param height The height which is used to determine the second y rectangle
     * @param radius The radius for the corners of the rectangles (>0)
     * @param color  The color used to draw the rectangle
     */
    public static void drawRise(final float x, final float y, final float width, final float height, final float radius, final Color color, boolean leftTop, boolean rightTop, boolean rightBottom, boolean leftBottom) {

        riseShader.setUniformf("u_size", width,  height);
        riseShader.setUniformf("u_radius", radius );
        riseShader.setUniformf("u_color", color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        riseShader.setUniformf("u_edges", leftTop ? 1.0F : 0.0F, rightTop ? 1.0F : 0.0F, rightBottom ? 1.0F : 0.0F, leftBottom ? 1.0F : 0.0F);

        GlStateManager.enableBlend();
        ShaderUtils.drawQuads(x, y, width, height);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }

    public static void drawRise(final float x, final float y, final float width, final float height, final float radius, final int argb, boolean leftTop, boolean rightTop, boolean rightBottom, boolean leftBottom) {


        riseShader.setUniformf("u_size", width, height);
        riseShader.setUniformf("u_radius", radius);
        riseShader.setUniformf("u_color", getRed(argb), getGreen(argb), getRed(argb), getAlpha(argb));
        riseShader.setUniformf("u_edges", leftTop ? 1.0F : 0.0F, rightTop ? 1.0F : 0.0F, rightBottom ? 1.0F : 0.0F, leftBottom ? 1.0F : 0.0F);

        GlStateManager.enableBlend();
        ShaderUtils.drawQuads(x, y, width, height);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }



    public static void drawRise(final double x, final double y, final double width, final double height, final double radius, final Color color, boolean leftTop, boolean rightTop, boolean rightBottom, boolean leftBottom) {
        drawRise((float) x, (float) y, (float) width, (float) height, (float) radius, color, leftTop, rightTop, rightBottom, leftBottom);
    }

    public static void drawRise(final double x, final double y, final double width, final double height, final double radius, final Color color) {
        drawRise((float) x, (float) y, (float) width, (float) height, (float) radius, color, true, true, true, true);
    }

    public static void drawRound(float x, float y, float width, float height, float radius, int color) {
        RenderUtils.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        RenderUtils.setAlphaLimit(0);

        roundedShader.init();
        setupRoundedRectUniforms(x, y, width, height, radius, roundedShader);
        roundedShader.setUniformf("color", getRed(color), getGreen(color), getBlue(color), getAlpha(color));

        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }

    public static void drawRound(float x, float y, float width, float height, float radius, Color color) {
        RenderUtils.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        RenderUtils.setAlphaLimit(0);
        roundedShader.init();

        setupRoundedRectUniforms(x, y, width, height, radius, roundedShader);
        roundedShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }


    private static void setupRoundedRectUniforms(float x, float y, float width, float height, float radius, ShaderUtils roundedTexturedShader) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        roundedTexturedShader.setUniformf("location", x * sr.getScaleFactor(),
                (Minecraft.getMinecraft().displayHeight - (height * sr.getScaleFactor())) - (y * sr.getScaleFactor()));
        roundedTexturedShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        roundedTexturedShader.setUniformf("radius", radius * sr.getScaleFactor());
    }




    private static float getRed(int color) {
        return (color >> 16 & 0xFF) / 255.0F;
    }

    private static float getGreen(int color) {
        return (color >> 8 & 0xFF) / 255.0F;
    }

    private static float getBlue(int color) {
        return (color & 0xFF) / 255.0F;
    }

    private static float getAlpha(int color) {
        return (color >> 24 & 0xFF) / 255.0F;
    }
}