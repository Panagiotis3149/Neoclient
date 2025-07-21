package neo.mixins.impl.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import neo.gui.menu.MainMenu;
import neo.gui.screen.DisconnectedGui;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.stream.IStream;
import net.minecraft.util.IChatComponent;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import static neo.util.render.RenderUtils.drawImage;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    private static final ResourceLocation CUSTOM_IMAGE = new ResourceLocation("neo", "textures/gui/image.png");

    @Shadow public WorldClient theWorld;

    @Shadow public EntityRenderer entityRenderer;

    @Shadow public GuiScreen currentScreen;
    @Shadow private boolean skipRenderWorld;
    @Shadow public EntityPlayerSP thePlayer;
    @Shadow public GameSettings gameSettings;
    @Shadow public GuiIngame ingameGUI;
    @Shadow public SoundHandler mcSoundHandler;

    @Shadow public abstract void setIngameNotInFocus();
    @Shadow public abstract void setIngameFocus();


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


    @Overwrite
    public void displayGuiScreen(GuiScreen guiScreenIn) {
        if (guiScreenIn == null && this.theWorld == null) {
            guiScreenIn = new GuiMainMenu();
        } else if (guiScreenIn == null && this.thePlayer.getHealth() <= 0.0F) {
            guiScreenIn = new GuiGameOver();
        }

        GuiScreen old = this.currentScreen;
        net.minecraftforge.client.event.GuiOpenEvent event = new net.minecraftforge.client.event.GuiOpenEvent(guiScreenIn);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return;

        guiScreenIn = event.gui;
        if (old != null && guiScreenIn != old) {
            old.onGuiClosed();
        }

        if (guiScreenIn instanceof GuiMainMenu) {
            this.gameSettings.showDebugInfo = false;
            this.ingameGUI.getChatGUI().clearChatMessages();
        }


        if (guiScreenIn instanceof GuiMainMenu) {
            guiScreenIn = new MainMenu();
        } /* else if (guiScreenIn instanceof GuiDisconnected) {
            GuiDisconnected oldGui = (GuiDisconnected) guiScreenIn;
            GuiDisconnectedAccessor accessor = (GuiDisconnectedAccessor) oldGui;
            GuiScreen screen = accessor.getParentScreen();
            String reasonKey = accessor.getReason();
            IChatComponent chatComp = accessor.getMessage();
            guiScreenIn = new DisconnectedGui(screen, reasonKey, chatComp);
            // goofy ahh context
        } */

        this.currentScreen = guiScreenIn;

        if (guiScreenIn != null) {
            this.setIngameNotInFocus();
            ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            guiScreenIn.setWorldAndResolution(Minecraft.getMinecraft(), i, j);
            this.skipRenderWorld = false;
        } else {
            this.mcSoundHandler.resumeSounds();
            this.setIngameFocus();
        }

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
