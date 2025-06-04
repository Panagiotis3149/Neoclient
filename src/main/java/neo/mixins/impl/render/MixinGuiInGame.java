package neo.mixins.impl.render;

import neo.module.ModuleManager;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.glEnable;

@Mixin(GuiIngame.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiInGame {

    @Shadow
    protected abstract void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer player);

    @Shadow
    @Final
    protected Minecraft mc;

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void renderScoreboard(CallbackInfo ci) {
        // ci.cancel();
        // I'll render here. soon...
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void injectCustomHotbar(ScaledResolution resolution, float delta, CallbackInfo ci) {
        if (mc.getRenderViewEntity() instanceof EntityPlayer && ModuleManager.interfacemod.isEnabled()) {
                ci.cancel();
                // I'll render here.
                int middleScreen = resolution.getScaledWidth() / 2;
                int height = resolution.getScaledHeight() - 1;
                float slot = mc.thePlayer.inventory.currentItem;
                BlurUtils.prepareBloom();
                BlurUtils.prepareBlur();
                RoundedUtils.drawRound(middleScreen - 91, height - 22, 16 * 11 + 4, height, 8, Color.BLACK);
                BlurUtils.bloomEnd(2, 4);
                BlurUtils.blurEnd(2, 1.5f);
                RoundedUtils.drawRound(middleScreen - 91 - 1 + slot * 20 + 1, height - 22, 18, height - 23 - 1 + 24, 2, 0x22000000);
                glEnable(GL11.GL_DEPTH_TEST);
                GL11.glPopMatrix();

                enableRescaleNormal();
                glEnable(GL_BLEND);
                tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();

                for (int j = 0; j < 9; ++j) {
                    int l = height - 16 - 3;
                    int k = middleScreen - 90 + j * 20 + 2;
                    renderHotbarItem(j, k, l, delta, mc.thePlayer);
                }

                RenderHelper.disableStandardItemLighting();
                disableRescaleNormal();
                disableBlend();
        }
    }
}
