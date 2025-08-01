package neo.module.impl.render;

import neo.module.Module;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import neo.script.ScriptDefaults;
import neo.util.render.RenderUtils;
import neo.util.render.Theme;
import neo.util.Utils;
import neo.util.font.FontManager;
import neo.util.font.MinecraftFontRenderer;
import neo.util.font.impl.FontRenderer;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static neo.Variables.*;
import static neo.util.render.RenderUtils.drawRoundedRectangle;

public class Watermark extends Module {
    public static SliderSetting theme;
    public static SliderSetting posx;
    public static SliderSetting posy;
    public static SliderSetting mode;
    private final String[] modes = new String[]{"Basic", "NeoOld", "Neo", "NeoText", "NeoText2", "Rise", "Rise2", "NeoNew", "NeoNewer", "RiseCopy", "Legacy"};

    public Watermark() {
        super("Watermark", Module.category.render);
        this.registerSetting(new DescriptionSetting("Right click bind to hide modules."));
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !Utils.isntnull()) {
            return;
        }

        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) {
            return;
        }

        render();
    }

    public void render() {


        String text = clientName;

        if (mode.getInput() == 0) {
            FontRenderer font = FontManager.helveticaNeue;
            int textColor = Theme.getGradient((int) theme.getInput(), 0.0);
            font.drawString(text, 4, 4, textColor, false);
        } else if (mode.getInput() == 1) {
            String textmodern = clientName + " " + clientVersion + " " + ScriptDefaults.client.getFPS() + " FPS";
            FontRenderer font = FontManager.helveticaNeue;
            int rectX = 5, rectY = 5, rectWidth = (int) (font.getStringWidth(textmodern) + 9), rectHeight = 20;
            drawRoundedRectangle(rectX, rectY, rectX + rectWidth, rectY + rectHeight, 7, 0x51161616);
            font.drawString(textmodern, rectX + 5, rectY + 7, Theme.getGradient((int) theme.getInput(), 0.0), false);
        } else if (mode.getInput() == 2) {
            String textmodern = clientName + " " + clientVersion;
            FontRenderer font = FontManager.googleRegular20;
            int rectX = 5, rectY = 5, rectWidth = (int) (font.getStringWidth(textmodern) + 9), rectHeight = 20;
            BlurUtils.prepareBlur();
            RoundedUtils.drawRound((float) rectX, (float) rectY, rectWidth, rectHeight, 4.0f, Color.black);
            BlurUtils.blurEnd(2, 2F);
            GL11.glPushMatrix();
            font.drawString(textmodern, rectX + 5, rectY + 7, Theme.getGradient((int) theme.getInput(), 0.0), false);
            GL11.glPopMatrix();
        } else if (mode.getInput() == 3) {
            FontRenderer font = FontManager.logoa;
            font.drawString(Theme.mCCC("Neo", (int) Watermark.theme.getInput()), 4 * 2, 4 * 2, 0xFFFFFFFF, false);
        } else if (mode.getInput() == 4) {
            FontRenderer font = FontManager.googleSansBold;
            font.drawString(Theme.mCCC("Neoclient", (int) Watermark.theme.getInput()), 4 * 2, 4 * 2, 0xFFFFFFFF, false);
        } else if (mode.getInput() == 5) {
            FontRenderer font = FontManager.productSansMedium36;
            font.drawString(Theme.mCCC("Neo", (int) Watermark.theme.getInput()), 6, 6, 0xFFFFFFFF, false);
        } else if (mode.getInput() == 6) {
            FontRenderer font = FontManager.productSansMedium36;
            font.drawString(Theme.mCCC("Neoclient", (int) Watermark.theme.getInput()), 6, 6, 0xFFFFFFFF, false);
        } else if (mode.getInput() == 7) {
            String textmodern = clientName + " §7|§r " + mc.thePlayer.getName() + " §7|§r " + ScriptDefaults.client.getFPS();
            FontRenderer font = FontManager.productSansLight22;
            int rectX = 5, rectY = 5, rectWidth = (int) (font.getStringWidth(textmodern) + 9), rectHeight = 20;
            BlurUtils.prepareBlur();
            RoundedUtils.drawRound((float) rectX, (float) rectY, rectWidth, rectHeight, 4.0f, Color.black);
            BlurUtils.blurEnd(2, 2F);
            GL11.glPushMatrix();
            font.drawString(textmodern, rectX + 5, rectY + 7, Theme.getGradient((int) theme.getInput(), 0.0), false);
            GL11.glPopMatrix();
        } else if (mode.getInput() == 8) {

            if (mc.gameSettings.keyBindPlayerList.isKeyDown()) {
                return;
            }

            String part1 = shortName;
            String sep1 = " | ";
            String part2 = buildType;
            String sep2 = " | ";
            String part3 = mc.getCurrentServerData().serverIP;
            String det1 = shortClientVersion;
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            String det2 = (info != null ? info.getResponseTime() : -1) + "ms";
            FontRenderer fontname = FontManager.productSansMedium36;
            FontRenderer fontbar = FontManager.productSansLight40;
            FontRenderer fontdefault = FontManager.googleSansRegular20;
            FontRenderer fontdetail = FontManager.productSansLight16;

            int screenWidth = new ScaledResolution(mc).getScaledWidth();
            int baseWidth = 0;
            baseWidth += (int) FontManager.productSansMedium36.getStringWidth(shortName);
            baseWidth += (int) FontManager.productSansLight40.getStringWidth(" | ");
            baseWidth += (int) FontManager.googleSansRegular20.getStringWidth(buildType);
            baseWidth += (int) FontManager.productSansLight40.getStringWidth(" | ");
            baseWidth += (int) FontManager.googleSansRegular20.getStringWidth(mc.getCurrentServerData().serverIP);

            int rectWidth = baseWidth + 8;
            int rectX = (screenWidth - rectWidth) / 2;
            int rectY = 25, rectHeight = 32;


            BlurUtils.prepareBlur();
            BlurUtils.prepareBloom();
            RoundedUtils.drawRound(rectX, rectY, rectWidth, rectHeight, 6.0f, Theme.getGradient((int) theme.getInput(), 0.0));
            BlurUtils.bloomEnd(3, 6F);
            BlurUtils.blurEnd(2, 2F);

            BlurUtils.prepareBlur();
            RoundedUtils.drawRound(rectX, rectY, rectWidth, rectHeight, 6.0f, Theme.getGradient((int) theme.getInput(), 0.0));
            BlurUtils.blurEnd(2, 2F);

            RenderUtils.drawRoundedRectangle(rectX, rectY, rectX + rectWidth, rectY + rectHeight, 6.0f, 0x44212121);

            int x = rectX + 4;
            int y = rectY + 7;

            fontname.drawString(part1, x, y, Theme.getGradient((int) theme.getInput(), 0.0), false);
            x += (int) fontname.getStringWidth(part1);

            fontbar.drawString(sep1, x, y, RenderUtils.toARGBInt(Color.lightGray), false);
            x += (int) fontbar.getStringWidth(sep1);

            fontdefault.drawString(part2, x, y, 0xFFFFFFFF, false);
            fontdetail.drawString(det1, x, y + 11, 0xFFFFFFFF, false);
            x += (int) fontdefault.getStringWidth(part2);

            fontbar.drawString(sep2, x, y, RenderUtils.toARGBInt(Color.lightGray), false);
            x += (int) fontbar.getStringWidth(sep2);

            fontdefault.drawString(part3, x, y, 0xFFFFFFFF, false);
            fontdetail.drawString(det2, x, y + 11, 0xFFFFFFFF, false);
        } if (mode.getInput() == 9) {
            FontRenderer fontneo = FontManager.productSansMedium36;
            FontRenderer fontver = FontManager.productSansMedium18;
            BlurUtils.prepareBloom();
            fontneo.drawString("Neo", 6, 6, Theme.getGradient((int) theme.getInput(), 0.0));
            BlurUtils.bloomEnd(2, 4F);
            fontneo.drawString("Neo", 6, 6, Theme.getGradient((int) theme.getInput(), 0.0));
            fontver.drawString(shortClientVersionNV, 6 + fontneo.getStringWidth("Neo") + 2, 6, 0xFFFFFFFF);
        } if (mode.getInput() == 10) {
            MinecraftFontRenderer font = MinecraftFontRenderer.INSTANCE;
            font.drawString("N", 4, 4, Theme.getGradient((int) theme.getInput(), 0.0), true);
            font.drawString("eoclient" + " [" + ScriptDefaults.client.getFPS() + " FPS]", 4 + font.width("N"), 4, 0xFFAAAAAA, true);
        }
    }
}