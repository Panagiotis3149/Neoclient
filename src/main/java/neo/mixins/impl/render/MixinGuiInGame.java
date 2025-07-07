package neo.mixins.impl.render;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import neo.module.ModuleManager;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
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
    private void renderScoreboard(ScoreObjective scoreObjective, ScaledResolution scaledResolution, CallbackInfo ci) {
        try {
            if (ModuleManager.interfacemod.isEnabled() && !ModuleManager.interfacemod.ns.isToggled()) {
                /*
                ci.cancel();

                Scoreboard scoreboard = scoreObjective.getScoreboard();

                Collection<Score> collection = scoreboard.getSortedScores(scoreObjective);

                List<Score> list = Lists.newArrayList(
                        Iterables.filter(collection, new ScoreFilter())
                );

                if (list.size() > 15) {
                    collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
                } else {
                    collection = list;
                }

                int i = (int) MinecraftFontRenderer.INSTANCE.width(scoreObjective.getDisplayName());

                for (Score score : collection) {
                    ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
                    String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
                    i = (int) Math.max(i, MinecraftFontRenderer.INSTANCE.width(s));
                }

                int i1 = (int) (collection.size() * MinecraftFontRenderer.INSTANCE.height());
                int j1 = scaledResolution.getScaledHeight() / 2 + i1 / 3;
                int k1 = 3;
                int l1 = ModuleManager.hud.alignRight.isToggled() ? 0 : scaledResolution.getScaledWidth() - i - k1;
                int j = 0;

                for (Score score1 : collection) {
                    ++j;
                    ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
                    String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
                    String s2 = EnumChatFormatting.RED + "" + score1.getScorePoints();
                    int k = (int) (j1 - j * MinecraftFontRenderer.INSTANCE.height());
                    int l = scaledResolution.getScaledWidth() - k1 + 2;
                    RoundedUtils.drawRound((float) (ModuleManager.hud.alignRight.isToggled() ? l1 : l1 - 2)
                            , (float) k, (float) (l - (ModuleManager.hud.alignRight.isToggled() ? l1 : l1 - 2)
                            ), (float) (k + MinecraftFontRenderer.INSTANCE.height()), 4, 1342177280);
                    MinecraftFontRenderer.INSTANCE.drawString(s1, l1, k, 553648127);
                    MinecraftFontRenderer.INSTANCE.drawString(s2, l - MinecraftFontRenderer.INSTANCE.width(s2), k, 553648127);

                    if (j == collection.size()) {
                        String s3 = scoreObjective.getDisplayName();
                        RoundedUtils.drawRound((float) (ModuleManager.hud.alignRight.isToggled() ? l1 : l1 - 2)
                                , (float) (k - MinecraftFontRenderer.INSTANCE.height() - 1), l - (ModuleManager.hud.alignRight.isToggled() ? l1 : l1 - 2)
                                , k - 1, 4, 1610612736);
                        RoundedUtils.drawRound(l1 - 2, k - 1, l - (ModuleManager.hud.alignRight.isToggled() ? l1 : l1 - 2)
                                , k, 4, 1342177280);
                        MinecraftFontRenderer.INSTANCE.drawString(s3, l1 + (double) i / 2 - MinecraftFontRenderer.INSTANCE.width(s3) / 2, k - MinecraftFontRenderer.INSTANCE.height(), 553648127);
                    }
                }
                 */
            } else if (ModuleManager.interfacemod.isEnabled() && ModuleManager.interfacemod.ns.isToggled()) {
                ci.cancel();
            }
        } catch (Throwable t) {
            System.out.println("renderScoreboard ewwor");
            t.printStackTrace();
        }
    }


    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void injectCustomHotbar(ScaledResolution resolution, float delta, CallbackInfo ci) {
        if (mc.getRenderViewEntity() instanceof EntityPlayer && ModuleManager.interfacemod.isEnabled()) {
                ci.cancel();
                int middleScreen = resolution.getScaledWidth() / 2;
                int height = resolution.getScaledHeight() - 1;
                float slot = mc.thePlayer.inventory.currentItem;
                BlurUtils.prepareBloom();
                BlurUtils.prepareBlur();
                RoundedUtils.drawRound(middleScreen - 91, height - 22, 16 * 11 + 4, height, 8, Color.BLACK);
                BlurUtils.bloomEnd(2, 4);
                BlurUtils.blurEnd(2, 1.5f);
                RoundedUtils.drawRound(middleScreen - 91 + slot * 20 + 2, height - 22, 16, height - 23 - 1 + 24, 2, 0x22000000);
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
