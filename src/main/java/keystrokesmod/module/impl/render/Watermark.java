package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.ScriptDefaults;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.shader.BlurUtils;
import keystrokesmod.utility.shader.RoundedUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static keystrokesmod.Variables.clientName;
import static keystrokesmod.Variables.clientVersion;
import static keystrokesmod.utility.RenderUtils.drawRoundedRectangle;

public class Watermark extends Module {
    public static SliderSetting theme;
    public static SliderSetting posx;
    public static SliderSetting posy;
    public static SliderSetting mode;
    private String[] modes = new String[]{"Basic", "Neo", "NeoNew"};

    public Watermark() {
        super("Watermark", Module.category.render);
        this.registerSetting(new DescriptionSetting("Right click bind to hide modules."));
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }

        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) {
            return;
        }

        render();
    }

    public void render() {
        int x = 4;
        int y = 4;

        String text = clientName;

        if (mode.getInput() == 0) {
            FontRenderer font = FontManager.helveticaNeue;
            int textColor = Theme.getGradient((int) theme.getInput(), 0.0);
            font.drawString(text, x, y, textColor, false);
        } else if (mode.getInput() == 1) {
            String textmodern = clientName + " " + clientVersion + " " + ScriptDefaults.client.getFPS() + " FPS";
            FontRenderer font = FontManager.helveticaNeue;
            int rectX = 5, rectY = 5, rectWidth = (int) (font.getStringWidth(textmodern) + 9), rectHeight = 20;
            drawRoundedRectangle(rectX, rectY, rectX + rectWidth, rectY + rectHeight, 7, 0x51161616);
            GL11.glPushMatrix();
            font.drawString(textmodern, rectX + 5, rectY + 7, Theme.getGradient((int) theme.getInput(), 0.0), false);
            GL11.glPopMatrix();
        } else if (mode.getInput() == 2) {
            String textmodern = clientName + " " + clientVersion;
            FontRenderer font = FontManager.googleRegular20;
            int rectX = 5, rectY = 5, rectWidth = (int) (font.getStringWidth(textmodern) + 9), rectHeight = 20;
            BlurUtils.prepareBlur();
            RoundedUtils.drawRound((float) rectX, (float) rectY, rectWidth, rectHeight, 4.0f, true, Color.black);
            BlurUtils.blurEnd(2, 2F);
            GL11.glPushMatrix();
            font.drawString(textmodern, rectX + 5, rectY + 7, Theme.getGradient((int) theme.getInput(), 0.0), false);
            GL11.glPopMatrix();
        }
    }
}