package neo.mixins.impl.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.LWJGLException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    private static final ResourceLocation CUSTOM_IMAGE = new ResourceLocation("neo", "textures/gui/image.png");

    @Inject(method = "drawSplashScreen", at = @At("HEAD"), cancellable = true)
    private void drawSplashScreen(TextureManager textureManagerInstance, CallbackInfo ci) throws LWJGLException {
        Minecraft mc = (Minecraft)(Object)this;
        ScaledResolution scaled = new ScaledResolution(mc);
        int dispW = scaled.getScaledWidth();
        int dispH = scaled.getScaledHeight();

        BufferedImage img;
        try (InputStream in = mc.getResourceManager().getResource(CUSTOM_IMAGE).getInputStream()) {
            img = ImageIO.read(in);
        } catch (IOException e) {
            return;
        }

        int imgW = img.getWidth();
        int imgH = img.getHeight();
        float scaleFactor = Math.min((float)dispW / imgW, (float)dispH / imgH);
        int drawW = Math.round(imgW * scaleFactor);
        int drawH = Math.round(imgH * scaleFactor);
        int x = (dispW - drawW) / 2;
        int y = (dispH - drawH) / 2;

        ResourceLocation logoLoc = textureManagerInstance.getDynamicTextureLocation(
                "neo:splash", new DynamicTexture(img)
        );
        textureManagerInstance.bindTexture(logoLoc);

        Framebuffer fb = new Framebuffer(dispW * scaled.getScaleFactor(), dispH * scaled.getScaleFactor(), true);
        fb.bindFramebuffer(false);
        GlStateManager.clear(16640); // clear color + depth
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, dispW, dispH, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);

        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        wr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        wr.pos(x,        y + drawH, 0).tex(0, 1).color(255,255,255,255).endVertex();
        wr.pos(x + drawW, y + drawH, 0).tex(1, 1).color(255,255,255,255).endVertex();
        wr.pos(x + drawW, y,         0).tex(1, 0).color(255,255,255,255).endVertex();
        wr.pos(x,        y,         0).tex(0, 0).color(255,255,255,255).endVertex();
        t.draw();

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        fb.unbindFramebuffer();
        fb.framebufferRender(dispW * scaled.getScaleFactor(), dispH * scaled.getScaleFactor());
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        mc.updateDisplay();

        ci.cancel();
    }


}
