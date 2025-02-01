package keystrokesmod.mixins.impl.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinScoreboard {
    private final String targetText = "Vanilla";
    private final String replacementText = "Vulcan";


    @Inject(method = "renderScoreboard", at = @At("HEAD"))
    protected void onRenderScoreboard(CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        Scoreboard scoreboard = mc.theWorld.getScoreboard();

        if (scoreboard == null) return;

        for (ScoreObjective scoreObjective : scoreboard.getScoreObjectives()) {
            if (scoreObjective == null || scoreObjective.getDisplayName() == null) continue;

            String displayName = scoreObjective.getDisplayName();

            if (displayName.contains(targetText)) {
                int targetIndex = displayName.indexOf(targetText);
                String beforePart = displayName.substring(0, targetIndex);
                String afterPart = displayName.substring(targetIndex + targetText.length());
                String updatedDisplayName = beforePart + replacementText + afterPart;
                scoreObjective.setDisplayName(updatedDisplayName);
            }
        }
    }
}