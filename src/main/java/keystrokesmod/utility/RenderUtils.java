package keystrokesmod.utility;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import keystrokesmod.module.impl.player.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import java.awt.*;

import static keystrokesmod.utility.ColorUtils.interpolateInt;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

public class RenderUtils {
    private static Minecraft mc = Minecraft.getMinecraft();
    public static boolean ring_c = false;
    private static final Int2IntOpenHashMap shadowCache = new Int2IntOpenHashMap(5);

    public static void renderBlock(BlockPos blockPos, int color, boolean outline, boolean shade) {
        renderBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1, 1, 1, color, outline, shade);
    }

    public static void renderBlock(BlockPos blockPos, int color, double y2, boolean outline, boolean shade) {
        renderBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1, y2, 1, color, outline, shade);
    }

    public static void scissor(double x, double y, double width, double height) {
        final ScaledResolution sr = new ScaledResolution(mc);
        final double scale = sr.getScaleFactor();

        y = sr.getScaledHeight() - y;

        x *= scale;
        y *= scale;
        width *= scale;
        height *= scale;

        GL11.glScissor((int) x, (int) (y - height), (int) width, (int) height);
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color toColor(int argb) {
        return new Color(argb, true); // true ensures that the alpha channel is respected
    }

    public static Color toRgbColor(int rgb) {
        return new Color(rgb); // No alpha channel is considered
    }


    public static int toArgb(Color color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color.getRGB() & 0xFFFFFF);
    }

    public static int toArgb(int rgb, int alpha) {
        return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }



    public static void drawRect(double left, double top, double right, double bottom, int color) {
        float f3 = (color >> 24 & 255) / 255.0F;
        float f = (color >> 16 & 255) / 255.0F;
        float f1 = (color >> 8 & 255) / 255.0F;
        float f2 = (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


    public static void drawRectBehind(double left, double top, double right, double bottom, int color) {
        float f3 = (color >> 24 & 255) / 255.0F; // Alpha
        float f = (color >> 16 & 255) / 255.0F;  // Red
        float f1 = (color >> 8 & 255) / 255.0F;  // Green
        float f2 = (color & 255) / 255.0F;        // Blue
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);

        worldrenderer.begin(7, DefaultVertexFormats.POSITION);

        // Lower z-index for drawing behind
        worldrenderer.pos(left, bottom, -1.0D).endVertex();
        worldrenderer.pos(right, bottom, -1.0D).endVertex();
        worldrenderer.pos(right, top, -1.0D).endVertex();
        worldrenderer.pos(left, top, -1.0D).endVertex();

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }



    public static void drawOutline(float x, float y, float x2, float y2, float lineWidth, int color) {
        float f5 = (float) ((color >> 24) & 255) / 255.0F;
        float f6 = (float) ((color >> 16) & 255) / 255.0F;
        float f7 = (float) ((color >> 8) & 255) / 255.0F;
        float f8 = (float) (color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glPushMatrix();
        GL11.glColor4f(f6, f7, f8, f5);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public static void renderBox(int x, int y, int z, double x2, double y2, double z2, int color, boolean outline, boolean shade) {
        double xPos = x - mc.getRenderManager().viewerPosX;
        double yPos = y - mc.getRenderManager().viewerPosY;
        double zPos = z - mc.getRenderManager().viewerPosZ;
        GL11.glPushMatrix();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        float n8 = (color >> 24 & 0xFF) / 255.0f;
        float n9 = (color >> 16 & 0xFF) / 255.0f;
        float n10 = (color >> 8 & 0xFF) / 255.0f;
        float n11 = (color & 0xFF) / 255.0f;
        GL11.glColor4f(n9, n10, n11, n8);
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(xPos, yPos, zPos, xPos + x2, yPos + y2, zPos + z2);
        if (outline) {
            RenderGlobal.drawSelectionBoundingBox(axisAlignedBB);
        }
        if (shade) {
            drawBoundingBox(axisAlignedBB, n9, n10, n11);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight;
    }

    public static void disableGL2D() {
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glDisable(GL_BLEND);
        GL11.glEnable(2929);
        GL11.glDisable(GL_LINE_SMOOTH);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }


    public static void drawImage(int image, float x, float y, float width, float height, int color) {
        enableGL2D();
        glPushMatrix();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);
        glEnable(GL11.GL_TEXTURE_2D);
        glDisable(GL_CULL_FACE);
        glEnable(GL11.GL_ALPHA_TEST);
        GlStateManager.enableBlend();
        GlStateManager.bindTexture(image);

        glColor(color);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); // top left
        GL11.glVertex2f(x, y);

        GL11.glTexCoord2f(0, 1); // bottom left
        GL11.glVertex2f(x, y + height);

        GL11.glTexCoord2f(1, 1); // bottom right
        GL11.glVertex2f(x + width, y + height);

        GL11.glTexCoord2f(1, 0); // top right
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();

        glEnable(GL_CULL_FACE);
        glPopMatrix();
        disableGL2D();
    }

    public static void bindTexture(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public static void renderBPS(final boolean b, final boolean b2) {
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        String s = "";
        int n = -1;
        if (b) {
            final double t = Utils.gbps((Freecam.freeEntity == null) ? mc.thePlayer : Freecam.freeEntity, 2);
            if (t < 10.0) {
                n = Color.green.getRGB();
            }
            else if (t < 30.0) {
                n = Color.yellow.getRGB();
            }
            else if (t < 60.0) {
                n = Color.orange.getRGB();
            }
            else if (t < 160.0) {
                n = Color.red.getRGB();
            }
            else {
                n = Color.black.getRGB();
            }
            s = s + t + "BPS";
        }
        if (b2) {
            final double h = Utils.getHorizontalSpeed();
            if (!s.isEmpty()) {
                s += " ";
            }
            s += Utils.rnd(h, 3);
        }
        mc.fontRendererObj.drawString(s, (float)(scaledResolution.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(s) / 2), (float)(scaledResolution.getScaledHeight() / 2 + 15), n, false);
    }

    public static void renderEntity(Entity e, int type, double expand, double shift, int color, boolean damage) {
        if (e instanceof EntityLivingBase) {
            double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosX;
            double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosY;
            double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosZ;
            float d = (float) expand / 40.0F;
            if (e instanceof EntityPlayer && damage && ((EntityPlayer) e).hurtTime != 0) {
                color = Color.RED.getRGB();
            }

            GlStateManager.pushMatrix();
            if (type == 3) {
                GL11.glTranslated(x, y - 0.2D, z);
                GL11.glRotated((double) (-mc.getRenderManager().playerViewY), 0.0D, 1.0D, 0.0D);
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
                    dGR(-21, 0, -25, 74, st, en);
                    dGR(21, 0, 25, 74, st, en);
                    net.minecraft.client.gui.Gui.drawRect(-21, 0, 21, 4, en);
                    net.minecraft.client.gui.Gui.drawRect(-21, 71, 21, 74, st);
                }

                GlStateManager.enableDepth();
            } else {
                int i;
                if (type == 4) {
                    EntityLivingBase en = (EntityLivingBase) e;
                    double r = en.getHealth() / en.getMaxHealth();
                    int b = (int) (74.0D * r);
                    int hc = r < 0.3D ? Color.red.getRGB() : (r < 0.5D ? Color.orange.getRGB() : (r < 0.7D ? Color.yellow.getRGB() : Color.green.getRGB()));
                    GL11.glTranslated(x, y - 0.2D, z);
                    GL11.glRotated(-mc.getRenderManager().playerViewY, 0.0D, 1.0D, 0.0D);
                    GlStateManager.disableDepth();
                    GL11.glScalef(0.03F + d, 0.03F + d, 0.03F + d);
                    i = (int) (21 + shift * 2);
                    net.minecraft.client.gui.Gui.drawRect(i, -1, i + 4, 75, Color.black.getRGB());
                    net.minecraft.client.gui.Gui.drawRect(i + 1, b, i + 3, 74, Color.darkGray.getRGB());
                    net.minecraft.client.gui.Gui.drawRect(i + 1, 0, i + 3, b, hc);
                    GlStateManager.enableDepth();
                } else if (type == 6) {
                    d3p(x, y, z, 0.699999988079071D, 45, 1.5F, color, color == 0);
                } else {
                    if (color == 0) {
                        color = Utils.getChroma(2L, 0L);
                    }

                    float a = (float) (color >> 24 & 255) / 255.0F;
                    float r = (float) (color >> 16 & 255) / 255.0F;
                    float g = (float) (color >> 8 & 255) / 255.0F;
                    float b = (float) (color & 255) / 255.0F;
                    AxisAlignedBB bbox = e.getEntityBoundingBox().expand(0.1D + expand, 0.1D + expand, 0.1D + expand);
                    AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - e.posX + x, bbox.minY - e.posY + y, bbox.minZ - e.posZ + z, bbox.maxX - e.posX + x, bbox.maxY - e.posY + y, bbox.maxZ - e.posZ + z);
                    GL11.glBlendFunc(770, 771);
                    GL11.glEnable(3042);
                    GL11.glDisable(3553);
                    GL11.glDisable(2929);
                    GL11.glDepthMask(false);
                    GL11.glLineWidth(2.0F);
                    GL11.glColor4f(r, g, b, a);
                    if (type == 1) {
                        RenderGlobal.drawSelectionBoundingBox(axis);
                    } else if (type == 2) {
                        drawBoundingBox(axis, r, g, b);
                    }
                    GL11.glEnable(3553);
                    GL11.glEnable(2929);
                    GL11.glDepthMask(true);
                    GL11.glDisable(3042);
                }
            }
            GlStateManager.popMatrix();
        }
    }

    public static void drawRoundedGradientOutlinedRectangleU(float n, float n2, float n3, float n4, final float n5, final int n6, final int n7, final int n8) { // credit to the creator of raven b4
        n *= 2.0;
        n2 *= 2.0;
        n3 *= 2.0;
        n4 *= 2.0;
        GL11.glPushAttrib(1);
        GL11.glScaled(0.5, 0.5, 0.5);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBegin(9);
        glColor(n6);
        for (int i = 0; i <= 90; i += 3) {
            final double n9 = (double) (i * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n9) * n5 * -1.0, (double) (n2 + n5) + Math.cos(n9) * n5 * -1.0);
        }
        for (int j = 90; j <= 180; j += 3) {
            final double n10 = (double) (j * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n10) * n5 * -1.0, (double) (n4 - n5) + Math.cos(n10) * n5 * -1.0);
        }
        for (int k = 0; k <= 90; k += 3) {
            final double n11 = (double) (k * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n11) * n5, (double) (n4 - n5) + Math.cos(n11) * n5);
        }
        for (int l = 90; l <= 180; l += 3) {
            final double n12 = (double) (l * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n12) * n5, (double) (n2 + n5) + Math.cos(n12) * n5);
        }
        GL11.glEnd();
        GL11.glPushMatrix();
        GL11.glShadeModel(7425);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(2);
        if (n7 != 0L) {
            glColor(n7);
        }
        for (int n13 = 0; n13 <= 90; n13 += 3) {
            final double n14 = (double) (n13 * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n14) * n5 * -1.0, (double) (n2 + n5) + Math.cos(n14) * n5 * -1.0);
        }
        for (int n15 = 90; n15 <= 180; n15 += 3) {
            final double n16 = (double) (n15 * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n16) * n5 * -1.0, (double) (n4 - n5) + Math.cos(n16) * n5 * -1.0);
        }
        if (n8 != 0) {
            glColor(n8);
        }
        for (int n17 = 0; n17 <= 90; n17 += 3) {
            final double n18 = (double) (n17 * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n18) * n5, (double) (n4 - n5) + Math.cos(n18) * n5);
        }
        for (int n19 = 90; n19 <= 180; n19 += 3) {
            final double n20 = (double) (n19 * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n20) * n5, (double) (n2 + n5) + Math.cos(n20) * n5);
        }
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glPopAttrib();
        GL11.glLineWidth(1.0f);
        GL11.glShadeModel(7424);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }


    public static void drawBoundingBox(AxisAlignedBB abb, float r, float g, float b) {
        drawBoundingBox(abb, r, g, b, 0.25f);
    }

    public static void drawBoundingBox(AxisAlignedBB abb, float r, float g, float b, float a) {
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer vb = ts.getWorldRenderer();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
    }

    public static void dtl(Entity e, int color, float lw) {
        if (e != null) {
            double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosX;
            double y = (double) e.getEyeHeight() + e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosY;
            double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosZ;
            float a = (float) (color >> 24 & 255) / 255.0F;
            float r = (float) (color >> 16 & 255) / 255.0F;
            float g = (float) (color >> 8 & 255) / 255.0F;
            float b = (float) (color & 255) / 255.0F;
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glEnable(2848);
            GL11.glDisable(2929);
            GL11.glDisable(3553);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glLineWidth(lw);
            GL11.glColor4f(r, g, b, a);
            GL11.glBegin(2);
            GL11.glVertex3d(0.0D, (double) mc.thePlayer.getEyeHeight(), 0.0D);
            GL11.glVertex3d(x, y, z);
            GL11.glEnd();
            GL11.glDisable(3042);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDisable(2848);
            GL11.glDisable(3042);
            GL11.glPopMatrix();
        }
    }

    public static void dGR(int left, int top, int right, int bottom, int startColor, int endColor) {
        int j;
        if (left < right) {
            j = left;
            left = right;
            right = j;
        }

        if (top < bottom) {
            j = top;
            top = bottom;
            bottom = j;
        }

        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double) right, (double) top, 0.0D).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double) left, (double) top, 0.0D).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double) left, (double) bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos((double) right, (double) bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void db(int w, int h, int r) {
        int c = r == -1 ? -1089466352 : r;
        net.minecraft.client.gui.Gui.drawRect(0, 0, w, h, c);
    }

    public static void dct(String text, char lineSplit, int x, int y, long s, long shift, boolean rect, FontRenderer fontRenderer) {
        int bX = x;
        int l = 0;
        long r = 0L;

        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == lineSplit) {
                ++l;
                x = bX;
                y += fontRenderer.FONT_HEIGHT + 5;
                r = shift * (long) l;
            } else {
                fontRenderer.drawString(String.valueOf(c), (float) x, (float) y, Utils.getChroma(s, r), rect);
                x += fontRenderer.getCharWidth(c);
                if (c != ' ') {
                    r -= 90L;
                }
            }
        }

    }

    public static void d2p(double x, double y, int radius, int sides, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);
        worldrenderer.begin(6, DefaultVertexFormats.POSITION);

        for (int i = 0; i < sides; ++i) {
            double angle = 6.283185307179586D * (double) i / (double) sides + Math.toRadians(180.0D);
            worldrenderer.pos(x + Math.sin(angle) * (double) radius, y + Math.cos(angle) * (double) radius, 0.0D).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void d3p(double x, double y, double z, double radius, int sides, float lineWidth, int color, boolean chroma) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        mc.entityRenderer.disableLightmap();
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2929);
        GL11.glEnable(2848);
        GL11.glDepthMask(false);
        GL11.glLineWidth(lineWidth);
        if (!chroma) {
            GL11.glColor4f(r, g, b, a);
        }

        GL11.glBegin(1);
        long d = 0L;
        long ed = 15000L / (long) sides;
        long hed = ed / 2L;

        for (int i = 0; i < sides * 2; ++i) {
            if (chroma) {
                if (i % 2 != 0) {
                    if (i == 47) {
                        d = hed;
                    }

                    d += ed;
                }

                int c = Utils.getChroma(2L, d);
                float r2 = (float) (c >> 16 & 255) / 255.0F;
                float g2 = (float) (c >> 8 & 255) / 255.0F;
                float b2 = (float) (c & 255) / 255.0F;
                GL11.glColor3f(r2, g2, b2);
            }

            double angle = 6.283185307179586D * (double) i / (double) sides + Math.toRadians(180.0D);
            GL11.glVertex3d(x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius);
        }

        GL11.glEnd();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDepthMask(true);
        GL11.glDisable(2848);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        mc.entityRenderer.enableLightmap();
    }

    public static void drawArrow(float x, float y, int color, double width, double length) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        RenderUtils.glColor(color);
        GL11.glLineWidth((float) width);
        float halfWidth = (float) (width / 2.0);
        float xOffset = halfWidth / 2.0f;
        float yOffset = halfWidth / 2.0f;
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x - xOffset, y + yOffset);
        GL11.glVertex2d(x + length - xOffset, y - length + yOffset);
        GL11.glVertex2d(x + length - xOffset, y - length + yOffset);
        GL11.glVertex2d(x + 2 * length - xOffset, y + yOffset);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }

    public static void glColor(final int n) { // credit to the creator of raven b4
        GL11.glColor4f((float) (n >> 16 & 0xFF) / 255.0f, (float) (n >> 8 & 0xFF) / 255.0f, (float) (n & 0xFF) / 255.0f, (float) (n >> 24 & 0xFF) / 255.0f);
    }

    public static void drawRoundedGradientOutlinedRectangle(float n, float n2, float n3, float n4, final float n5, final int n6, final int n7, final int n8) { // credit to the creator of raven b4
        n *= 2.0;
        n2 *= 2.0;
        n3 *= 2.0;
        n4 *= 2.0;
        GL11.glPushAttrib(1);
        GL11.glScaled(0.5, 0.5, 0.5);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBegin(9);
        glColor(n6);
        for (int i = 0; i <= 90; i += 3) {
            final double n9 = (double) (i * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n9) * n5 * -1.0, (double) (n2 + n5) + Math.cos(n9) * n5 * -1.0);
        }
        for (int j = 90; j <= 180; j += 3) {
            final double n10 = (double) (j * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n10) * n5 * -1.0, (double) (n4 - n5) + Math.cos(n10) * n5 * -1.0);
        }
        for (int k = 0; k <= 90; k += 3) {
            final double n11 = (double) (k * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n11) * n5, (double) (n4 - n5) + Math.cos(n11) * n5);
        }
        for (int l = 90; l <= 180; l += 3) {
            final double n12 = (double) (l * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n12) * n5, (double) (n2 + n5) + Math.cos(n12) * n5);
        }
        GL11.glEnd();
        GL11.glPushMatrix();
        GL11.glShadeModel(7425);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(2);
        if (n7 != 0L) {
            glColor(n7);
        }
        for (int n13 = 0; n13 <= 90; n13 += 3) {
            final double n14 = (double) (n13 * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n14) * n5 * -1.0, (double) (n2 + n5) + Math.cos(n14) * n5 * -1.0);
        }
        for (int n15 = 90; n15 <= 180; n15 += 3) {
            final double n16 = (double) (n15 * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n16) * n5 * -1.0, (double) (n4 - n5) + Math.cos(n16) * n5 * -1.0);
        }
        if (n8 != 0) {
            glColor(n8);
        }
        for (int n17 = 0; n17 <= 90; n17 += 3) {
            final double n18 = (double) (n17 * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n18) * n5, (double) (n4 - n5) + Math.cos(n18) * n5);
        }
        for (int n19 = 90; n19 <= 180; n19 += 3) {
            final double n20 = (double) (n19 * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n20) * n5, (double) (n2 + n5) + Math.cos(n20) * n5);
        }
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glPopAttrib();
        GL11.glLineWidth(1.0f);
        GL11.glShadeModel(7424);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawPolygon(final double n, final double n2, final double n3, final int n4, final int n5) {
        if (n4 < 3) {
            return;
        }
        final float n6 = (n5 >> 24 & 0xFF) / 255.0f;
        final float n7 = (n5 >> 16 & 0xFF) / 255.0f;
        final float n8 = (n5 >> 8 & 0xFF) / 255.0f;
        final float n9 = (n5 & 0xFF) / 255.0f;
        final Tessellator getInstance = Tessellator.getInstance();
        final WorldRenderer getWorldRenderer = getInstance.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glColor4f(n7, n8, n9, n6);
        getWorldRenderer.begin(6, DefaultVertexFormats.POSITION);
        for (int i = 0; i < n4; ++i) {
            final double n10 = 6.283185307179586 * i / n4 + Math.toRadians(180.0);
            getWorldRenderer.pos(n + Math.sin(n10) * n3, n2 + Math.cos(n10) * n3, 0.0).endVertex();
        }
        getInstance.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


    public static void drawRoundedOutline(float n, float n2, float n3, float n4, final float n5, final float n6) {
        n *= 2.0;
        n2 *= 2.0;
        n3 *= 2.0;
        n4 *= 2.0;
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glLineWidth(2.0f);

        GL11.glBegin(1);
        glColor((int) n6);

        for (int i = 0; i <= 90; i += 3) {
            final double n7 = (double) (i * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n7) * n5 * -1.0, (double) (n2 + n5) + Math.cos(n7) * n5 * -1.0);
        }

        for (int j = 90; j <= 180; j += 3) {
            final double n8 = (double) (j * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n8) * n5 * -1.0, (double) (n4 - n5) + Math.cos(n8) * n5 * -1.0);
        }

        for (int k = 0; k <= 90; k += 3) {
            final double n9 = (double) (k * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n9) * n5, (double) (n4 - n5) + Math.cos(n9) * n5);
        }

        for (int l = 90; l <= 180; l += 3) {
            final double n10 = (double) (l * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n10) * n5, (double) (n2 + n5) + Math.cos(n10) * n5);
        }

        GL11.glVertex2d(n, n2 + n5);
        GL11.glVertex2d(n, n4 - n5);
        GL11.glVertex2d(n3, n2 + n5);
        GL11.glVertex2d(n3, n4 - n5);
        GL11.glVertex2d(n + n5, n2);
        GL11.glVertex2d(n3 - n5, n2);
        GL11.glVertex2d(n + n5, n4);
        GL11.glVertex2d(n3 - n5, n4);

        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glPopAttrib();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f); // Reset color to white
    }

    public static void drawRoundedOutline(float startX, float startY, float endX, float endY, final float cornerRadius, final float outlineThickness, int color) {
        // Extract ARGB components from the color
        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        startX *= 2.0;
        startY *= 2.0;
        endX *= 2.0;
        endY *= 2.0;

        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);
        GL11.glEnable(3042); // GL_BLEND
        GL11.glDisable(3553); // GL_TEXTURE_2D
        GL11.glEnable(2848); // GL_LINE_SMOOTH
        GL11.glLineWidth(2.0f);

        GL11.glBegin(1); // GL_LINES

        // Set the color using the extracted RGBA components
        GL11.glColor4f(red, green, blue, alpha);

        // Draw rounded rectangle outline (same logic as before)
        for (int i = 0; i <= 90; i += 3) {
            final double angle = (i * 0.017453292f);
            GL11.glVertex2d((startX + cornerRadius) + Math.sin(angle) * cornerRadius * -1.0,
                    (startY + cornerRadius) + Math.cos(angle) * cornerRadius * -1.0);
        }

        for (int j = 90; j <= 180; j += 3) {
            final double angle = (j * 0.017453292f);
            GL11.glVertex2d((startX + cornerRadius) + Math.sin(angle) * cornerRadius * -1.0,
                    (endY - cornerRadius) + Math.cos(angle) * cornerRadius * -1.0);
        }

        for (int k = 0; k <= 90; k += 3) {
            final double angle = (k * 0.017453292f);
            GL11.glVertex2d((endX - cornerRadius) + Math.sin(angle) * cornerRadius,
                    (endY - cornerRadius) + Math.cos(angle) * cornerRadius);
        }

        for (int l = 90; l <= 180; l += 3) {
            final double angle = (l * 0.017453292f);
            GL11.glVertex2d((endX - cornerRadius) + Math.sin(angle) * cornerRadius,
                    (startY + cornerRadius) + Math.cos(angle) * cornerRadius);
        }

        GL11.glVertex2d(startX, startY + cornerRadius);
        GL11.glVertex2d(startX, endY - cornerRadius);
        GL11.glVertex2d(endX, startY + cornerRadius);
        GL11.glVertex2d(endX, endY - cornerRadius);
        GL11.glVertex2d(startX + cornerRadius, startY);
        GL11.glVertex2d(endX - cornerRadius, startY);
        GL11.glVertex2d(startX + cornerRadius, endY);
        GL11.glVertex2d(endX - cornerRadius, endY);

        GL11.glEnd();

        GL11.glEnable(3553); // GL_TEXTURE_2D
        GL11.glDisable(3042); // GL_BLEND
        GL11.glDisable(2848); // GL_LINE_SMOOTH
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glPopAttrib();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f); // Reset color to white
    }


    public static void drawCircleOutline(double x, double y, double width, double height, float lineWidth, Color color) {
        drawArcOutline(x + width / 2f, y + height / 2f, width / 2f, 0, 360f, lineWidth, color);
    }


    private static float renderDeltaTime = 1000;


    public static double fpsMultiplier() {
        return renderDeltaTime / 60.0 * 3;
    }

    public static void add(double x, double y, Color color) {
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        GL11.glVertex2d(x, y);
    }

    public static void drawArcOutline(double x, double y, double radius, double startAngle, double endAngle, float lineWidth, Color color) {
        GlStateManager.pushMatrix();
        RenderUtils.preRenderShade();
        GL11.glLineWidth(lineWidth);

        GL11.glBegin(GL11.GL_LINE_STRIP);

        for (double i = (startAngle / 360.0 * 100); i <= (endAngle / 360.0 * 100); i++) {
            double angle = (Math.PI * 2 * i / 100) + Math.toRadians(180);
            add(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, color);
        }

        GL11.glEnd();

        RenderUtils.postRenderShade();
        GlStateManager.popMatrix();
    }


    public static void preRenderShade() {
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GlStateManager.disableCull();
    }

    public static void postRenderShade() {
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }


    public static void drawRoundedRectangle(float n, float n2, float n3, float n4, final float n5, final int n6) {
        n *= 2.0;
        n2 *= 2.0;
        n3 *= 2.0;
        n4 *= 2.0;
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBegin(9);
        glColor(n6);
        for (int i = 0; i <= 90; i += 3) {
            final double n7 = (double) (i * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n7) * n5 * -1.0, (double) (n2 + n5) + Math.cos(n7) * n5 * -1.0);
        }
        for (int j = 90; j <= 180; j += 3) {
            final double n8 = (double) (j * 0.017453292f);
            GL11.glVertex2d((double) (n + n5) + Math.sin(n8) * n5 * -1.0, (double) (n4 - n5) + Math.cos(n8) * n5 * -1.0);
        }
        for (int k = 0; k <= 90; k += 3) {
            final double n9 = (double) (k * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n9) * n5, (double) (n4 - n5) + Math.cos(n9) * n5);
        }
        for (int l = 90; l <= 180; l += 3) {
            final double n10 = (double) (l * 0.017453292f);
            GL11.glVertex2d((double) (n3 - n5) + Math.sin(n10) * n5, (double) (n2 + n5) + Math.cos(n10) * n5);
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glPopAttrib();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawRoundedOutlineB(float startX, float startY, float endX, float endY, final float cornerRadius, final float outlineThickness, int color) {
        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        startX *= 2.0;
        startY *= 2.0;
        endX *= 2.0;
        endY *= 2.0;

        float z = 0.1f; // Z-coordinate (set lower value for behind)

        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glLineWidth(2.0f);

        GL11.glBegin(1);
        GL11.glColor4f(red, green, blue, alpha);

        for (int i = 0; i <= 90; i += 3) {
            final double angle = (i * 0.017453292f);
            GL11.glVertex3d((startX + cornerRadius) + Math.sin(angle) * cornerRadius * -1.0,
                    (startY + cornerRadius) + Math.cos(angle) * cornerRadius * -1.0,
                    z);
        }

        // Similar changes for other parts of the rectangle

        GL11.glEnd();

        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glPopAttrib();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }




    public static void drawRoundedGradientRect(float x, float y, float x2, float y2, final float n5, final int n6, final int n7, final int n8, final int n9) {
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);
        x *= 2.0;
        y *= 2.0;
        x2 *= 2.0;
        y2 *= 2.0;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        glColor(n6);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glBegin(9);
        for (int i = 0; i <= 90; i += 3) {
            final double n10 = i * 0.017453292f;
            GL11.glVertex2d((double) (x + n5) + Math.sin(n10) * n5 * -1.0, (double) (y + n5) + Math.cos(n10) * n5 * -1.0);
        }
        glColor(n7);
        for (int j = 90; j <= 180; j += 3) {
            final double n11 = j * 0.017453292f;
            GL11.glVertex2d((double) (x + n5) + Math.sin(n11) * n5 * -1.0, (double) (y2 - n5) + Math.cos(n11) * n5 * -1.0);
        }
        glColor(n8);
        for (int k = 0; k <= 90; k += 3) {
            final double n12 = k * 0.017453292f;
            GL11.glVertex2d((double) (x2 - n5) + Math.sin(n12) * n5, (double) (y2 - n5) + Math.cos(n12) * n5);
        }
        glColor(n9);
        for (int l = 90; l <= 180; l += 3) {
            final double n13 = l * 0.017453292f;
            GL11.glVertex2d((double) (x2 - n5) + Math.sin(n13) * n5, (double) (y + n5) + Math.cos(n13) * n5);
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glPopAttrib();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
    }

    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0f / textureWidth;
        float f1 = 1.0f / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0).tex(u * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0).tex((u + width) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static void drawImage(ResourceLocation res, float x, float y, float width, float height) {
        RenderUtils.drawImage(res, x, y, width, height, 0xFFFFF);
    }

    public static void drawImage(ResourceLocation res, float x, float y, float width, float height, int color) {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        GL14.glBlendFuncSeparate(770, 771, 1, 0);
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        int alpha = color >> 24 & 0xFF;
        GL11.glColor4f((float) red / 255.0f, (float) green / 255.0f, (float) blue / 255.0f, (float) alpha / 255.0f);
        mc.getTextureManager().bindTexture(res);
        RenderUtils.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }



    public static int setAlpha(int rgb, double alpha) {
        if (alpha < 0 || alpha > 1) {
            alpha = 0.5;
        }

        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        int alphaInt = (int) (alpha * 255);

        int rgba = (alphaInt << 24) | (red << 16) | (green << 8) | blue;

        return rgba;
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }


    public void enable(final int glTarget) {
        GL11.glEnable(glTarget);
    }

    public void disable(final int glTarget) {
        GL11.glDisable(glTarget);
    }


    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }

    public void startSmooth() {
        enable(GL11.GL_POLYGON_SMOOTH);
        enable(GL11.GL_LINE_SMOOTH);
        enable(GL11.GL_POINT_SMOOTH);
    }

    public void endSmooth() {
        disable(GL11.GL_POINT_SMOOTH);
        disable(GL11.GL_LINE_SMOOTH);
        disable(GL11.GL_POLYGON_SMOOTH);
    }

    public void push() {
        GL11.glPushMatrix();
    }

    public void pop() {
        GL11.glPopMatrix();
    }

    public static void color(final double red, final double green, final double blue, final double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }

    public void color(final double red, final double green, final double blue) {
        color(red, green, blue, 1);
    }

    public static void color(Color color) {
        if (color == null)
            color = Color.white;
        color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }

    public void color(Color color, final int alpha) {
        if (color == null)
            color = Color.white;
        color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 0.5);
    }

    public static void renderItemIcon(final double x, final double y, final ItemStack itemStack) {
        if (itemStack != null) {
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();

            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, (int) x, (int) y);

            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

    public static float animate(float target, float current, float speed) {
        boolean larger = (target > current);
        if (speed < 0.0f) speed = 0.0f;
        else if (speed > 1.0f) speed = 1.0f;
        float dif = Math.abs(current - target);
        float factor = dif * speed;
        if (larger) current += factor;
        else current -= factor;
        return current;
    }

    public static void drawPlayerHead(int x, int y, int size, EntityLivingBase player) {
        NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(player.getUniqueID());
        if (playerInfo != null) {
            mc.getTextureManager().bindTexture(playerInfo.getLocationSkin());
            GL11.glColor4f(1F, 1F, 1F, 1F);


            int textureSize = 8;
            int offset = size / 8;
            Gui.drawScaledCustomSizeModalRect(
                    x, y, 8F, 8F, textureSize, textureSize, size, size, 64F, 64F
            );
        }
    }





    /**
     * Better to use gl state manager to avoid bugs
     */
    public void start() {
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
    public void stop() {
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }


}
