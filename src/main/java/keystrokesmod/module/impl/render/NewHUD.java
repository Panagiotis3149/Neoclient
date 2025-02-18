package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.shader.BlurUtils;
import keystrokesmod.utility.shader.RoundedUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import java.awt.*;

public class NewHUD extends Module {
    public static SliderSetting theme;
    public static int hudX = 900;
    public static int hudY = 10;


    public NewHUD() {
        super("HUD2", Module.category.render);
        this.registerSetting(new DescriptionSetting("Right click bind to hide modules."));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
    }


    @SubscribeEvent
    public void onRenderTick(RenderTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !Utils.nullCheck()) return;
            ModuleManager.sort();
        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) return;

        int n = hudY;
        double n2 = 0.0;

        for (Module module : ModuleManager.organizedModules) {
            if (module.isEnabled() && module != this) {
                if (module.isHidden() || module == ModuleManager.commandLine) continue;
                String moduleName = module.getName();

                keystrokesmod.utility.font.impl.FontRenderer font = FontManager.googleRegular20;
                {
                    String text = moduleName;
                    int e = Theme.getGradient((int) theme.getInput(), n2);
                    if (theme.getInput() == 0) {
                        n2 -= 120;
                    } else {
                        n2 -= 12;
                    }
                    double n3 = hudX;
                    double width = font.width(text);
                        n3 -= width - 46;


                    RenderUtils.drawRect(n3 - 3, n - 1, n3 + (width + 2), n + Math.round(font.height() + 1), RenderUtils.toArgb(e, 88));
                    BlurUtils.prepareBlur();
                    RoundedUtils.drawRound((float) (n3 - 3), n - 1, (float) (width + 3), Math.round(font.height() + 1.5), 0, true, Color.black);
                    BlurUtils.blurEnd(2, 1.75F);
                    RenderUtils.drawRoundedRectangle((float)(n3 + width + 2), (float)n - 1, (float) (n3 + width), (float)(n + Math.round(font.height() + 1)), 0, e);
                    font.drawString(text, n3 - 1, n, e, false);
                    n += font.height() + 2.5;
                }
            }
        }

    }
}