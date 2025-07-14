package neo.mixins.impl.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.stream.IStream;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import static neo.util.render.RenderUtils.drawImage;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    private static final ResourceLocation CUSTOM_IMAGE = new ResourceLocation("neo", "textures/gui/image.png");

    @Shadow public WorldClient theWorld;

    @Shadow public EntityRenderer entityRenderer;

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void clearLoadedMaps(WorldClient worldClientIn, String loadingMessage, CallbackInfo ci) {
        if (worldClientIn != this.theWorld) {
            this.entityRenderer.getMapItemRenderer().clearLoadedMaps();
        }
    }

    @Redirect(
            method = "runGameLoop",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/stream/IStream;func_152935_j()V")
    )
    private void skipTwitchCode1(IStream instance) {
        // No-op
    }

    @Redirect(
            method = "runGameLoop",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/stream/IStream;func_152922_k()V")
    )
    private void skipTwitchCode2(IStream instance) {
        // No-op
    }


    @Inject(method = "drawSplashScreen", at = @At("HEAD"), cancellable = true)
    private void drawSplashScreen(TextureManager texMgr, CallbackInfo ci) throws LWJGLException {
        ci.cancel();
        Minecraft mc = (Minecraft)(Object)this;
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();

        BufferedImage img;
        try (InputStream in = mc.getResourceManager().getResource(CUSTOM_IMAGE).getInputStream()) {
            img = ImageIO.read(in);
        } catch (IOException e) {
            return;
        }

        float sf = Math.min((float)w / img.getWidth(), (float)h / img.getHeight());
        int dw = Math.round(img.getWidth() * sf), dh = Math.round(img.getHeight() * sf);
        int x = (w - dw) / 2, y = (h - dh) / 2;

        ResourceLocation dynLoc = texMgr.getDynamicTextureLocation("neo:splash", new DynamicTexture(img));

        Framebuffer fb = new Framebuffer(w * sr.getScaleFactor(), h * sr.getScaleFactor(), true);
        fb.bindFramebuffer(true);
        GlStateManager.clearColor(0f, 0f, 0f, 1f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0, w, h, 0, 1000, 3000);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0, 0, -2000);

        drawImage(dynLoc, x, y, dw, dh, 0xFFFFFFFF);

        fb.unbindFramebuffer();
        fb.framebufferRender(w * sr.getScaleFactor(), h * sr.getScaleFactor());

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        mc.updateDisplay();
    }


}
