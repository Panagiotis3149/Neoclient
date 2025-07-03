package neo.module.impl.render;

import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;
import neo.util.font.FontManager;
import neo.util.font.MinecraftFontRenderer;
import neo.util.render.RenderUtils;
import neo.util.render.Theme;
import neo.util.shader.BlurUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

import static neo.util.shader.RoundedUtils.drawRound;

public class HUD extends Module {
    public static SliderSetting theme;
    public static ButtonSetting dropShadow;
    public static ButtonSetting alphabeticalSort;
    public static ButtonSetting wText;
    public static ButtonSetting wsText;
    public static SliderSetting fonts;
    public static SliderSetting bloomType;
    public static SliderSetting outliness;
    public static SliderSetting blurss;
    public static SliderSetting bgss;
    public static SliderSetting cbgss;
    // public static SliderSetting round;
    public static ButtonSetting alignRight;
    public static ButtonSetting lowercase;
    public static ButtonSetting showInfo;
    public static ButtonSetting wout;
    public static int hudX = 5;
    public static int hudY = 70;
    public static float bh;
    private final ButtonSetting srndr;
    public String[] cbgs = new String[]{"None", "Light", "Normal", "Heavy"};
    public String[] outlines = new String[]{"None", "Sidebar", "NewSidebar"}; // , "AllOutline"};
    public String[] blurs = new String[]{"None", "Light", "Normal", "Heavy"};
    public String[] bgs = new String[]{"None", "Transparent", "Normal", "Opaque"};
    public String[] blooms = new String[]{"None", "Shadow", "Glow", "Both"};
    public String[] fontss = new String[]{"Minecraft", "Helvetica Neue", "Product Sans", "Google", "Apple UI", "Greycliff CF", "Product Sans Light", "Poppins Bold", "Proxima Nova", "Comfortaa"};
    private boolean isAlphabeticalSort;
    private boolean canShowInfo;

    public HUD() {
        super("Arraylist", Module.category.render);
        this.registerSetting(new DescriptionSetting("Right click bind to hide modules."));
        this.registerSetting(fonts = new SliderSetting("Font", fontss, 2));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
        this.registerSetting(new ButtonSetting("Edit position", () -> mc.displayGuiScreen(new EditScreen())));
        this.registerSetting(alignRight = new ButtonSetting("Align right", true));
        this.registerSetting(alphabeticalSort = new ButtonSetting("Alphabetical sort", false));
        this.registerSetting(dropShadow = new ButtonSetting("Drop shadow", false));
        this.registerSetting(lowercase = new ButtonSetting("Lowercase", false));
        this.registerSetting(showInfo = new ButtonSetting("Show module info", false));
        this.registerSetting(wText = new ButtonSetting("White Text", false));
        this.registerSetting(wsText = new ButtonSetting("White Information", true));
        this.registerSetting(wout = new ButtonSetting("White Sidebar", false));
        this.registerSetting(srndr = new ButtonSetting("Hide Render", false));
        this.registerSetting(bloomType = new SliderSetting("Bloom Type", blooms, 0));
        this.registerSetting(blurss = new SliderSetting("Blur Type", blurs, 0));
        this.registerSetting(bgss = new SliderSetting("Background Type", bgs, 0));
        this.registerSetting(cbgss = new SliderSetting("Color Type", cbgs, 0));
        this.registerSetting(outliness = new SliderSetting("Sidebar Type", outlines, 2));
//      this.registerSetting(round = new SliderSetting("Roundness", 0, 0.0, 8, 1));
    }


    @SubscribeEvent
    public void onRenderTick(RenderTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !Utils.isnull()) return;
        if (isAlphabeticalSort != alphabeticalSort.isToggled()) {
            isAlphabeticalSort = alphabeticalSort.isToggled();
            ModuleManager.sort();
        }
        if (canShowInfo != showInfo.isToggled()) {
            canShowInfo = showInfo.isToggled();
            ModuleManager.sort();
        }
        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) return;

        int n = hudY;
        double n2 = 0.0;


        for (Module module : ModuleManager.organizedModules) {
            if (module.isEnabled() && module != this) {
                if (module.isHidden() || (module.moduleCategory().equals(category.render) && srndr.isToggled())) continue;
                String moduleName = module.getName();
                String moduleInfo = "";
                if (module.getInfo() != null && !Objects.equals(module.getInfo(), "")) {
                    moduleInfo = "§7" + " " + module.getInfo();
                    if (wsText.isToggled()) {
                        moduleInfo = "§f" + " " + module.getInfo();
                    }
                }

                if (fonts.getInput() == 0) {
                    MinecraftFontRenderer font = MinecraftFontRenderer.INSTANCE;
                    bh = (float) ((float) font.height() + 0.65);

                    {
                        String text = showInfo.isToggled() ? moduleName + moduleInfo : moduleName;
                        if (lowercase.isToggled()) {
                            text = text.toLowerCase();
                        }
                        int e = Theme.getGradient((int) theme.getInput(), n2);
                        if (theme.getInput() == 0) {
                            n2 -= 120;
                        } else {
                            n2 -= 12;
                        }
                        double n3 = hudX;
                        double width = font.width(text) + 1;
                        if (alignRight.isToggled()) {
                            n3 -= width - MinecraftFontRenderer.INSTANCE.width("ModuleExample");
                        }
                        if (bloomType.getInput() == 3 || bloomType.getInput() == 1) {
                            BlurUtils.prepareBloom();
                            drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, Color.black);
                            BlurUtils.bloomEnd(2, 2F);
                        }
                        switch ((int) blurss.getInput()) {
                            case 0:
                                break;
                            case 1:
                                BlurUtils.prepareBlur();
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, Color.black);
                                BlurUtils.blurEnd(2, 0.25F);
                                break;
                            case 2:
                                BlurUtils.prepareBlur();
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, Color.black);
                                BlurUtils.blurEnd(2, 0.75F);
                                break;
                            case 3:
                                BlurUtils.prepareBlur();
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, Color.black);
                                BlurUtils.blurEnd(2, 1.5F);
                                break;
                        }
                        switch ((int) bgss.getInput()) {
                            case 0:
                                break;
                            case 1:
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, new Color(0, 0, 0, 64).getRGB());
                                break;
                            case 2:
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, new Color(0, 0, 0, 128).getRGB());
                                break;
                            case 3:
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, new Color(32, 32, 36, 255).getRGB());
                                break;
                        }
                        switch ((int) cbgss.getInput()) {
                            case 0:
                                break;
                            case 1:
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 32));
                                break;
                            case 2:
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 92));
                                break;
                            case 3:
                                e = RenderUtils.toArgb(Color.WHITE, 255);
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 255));
                                break;
                        }
                        switch ((int) outliness.getInput()) {
                            case 0:
                                break;
                            case 1:
                                RenderUtils.drawRect(alignRight.isToggled() ? n3 + width : n3 + 1, n - 1, alignRight.isToggled() ? n3 + width + 2 : n3 - 1, n + (float) font.height() + 0.7, wout.isToggled() ? 0xFFFFFFFF : Theme.getGradient((int) theme.getInput(), 0.0));
                                break;
                            case 2:
                                if (bloomType.getInput() == 3 || bloomType.getInput() == 2) BlurUtils.prepareBloom();
                                drawRound((float) (n3 + width + 2.2), n + (bh * 0.1f), 1f, bh * 0.8f, 1f, wout.isToggled() ? 0xFFFFFFFF : Theme.getGradient((int) theme.getInput(), 0.0));
                                if (bloomType.getInput() == 3 || bloomType.getInput() == 2) BlurUtils.bloomEnd(2, 2.25F);
                                drawRound((float) (n3 + width + 2.2), n + (bh * 0.1f), 1f, bh * 0.8f, 1f, wout.isToggled() ? 0xFFFFFFFF : Theme.getGradient((int) theme.getInput(), 0.0));
                                break;
                        }
                        if (wText.isToggled()) {
                            e = RenderUtils.toArgb(Color.WHITE, 255);
                        }
                        font.drawString(text, n3, n, e, dropShadow.isToggled());
                        if (fonts.getInput() != 5) {
                            n += (int) (font.height() + 2);
                        } else {
                            n += (int) (font.height() + 3);
                        }
                    }
                } else {
                    neo.util.font.impl.FontRenderer font = FontManager.helveticaNeue;
                    bh = (float) ((float) font.height() + 0.5);
                    switch ((int) fonts.getInput()) {
                        case 2:
                            font = FontManager.productSans20;
                            bh = (float) ((float) font.height() + 0.65);
                            break;
                        case 3:
                            font = FontManager.googleMedium20;
                            bh = (float) ((float) font.height() + 0.55);
                            break;
                        case 4:
                            font = FontManager.sfRegular;
                            bh = (float) ((float) font.height() + 0.45);
                            break;
                        case 5:
                            font = FontManager.greyCliffCF;
                            bh = (float) ((float) font.height() + 1.5);
                            break;
                        case 6:
                            font = FontManager.productSansLight22;
                            bh = (float) ((float) font.height() + 0.65);
                            break;
                        case 7:
                            font = FontManager.poppinsBold20;
                            bh = (float) ((float) font.height() + 0.65);
                            break;
                        case 8:
                            font = FontManager.proximaNova;
                            bh = (float) ((float) font.height() + 0.65);
                            break;
                        case 9:
                            font = FontManager.comfortaa;
                            bh = (float) ((float) font.height() + 0.7);
                        default:
                            bh = (float) ((float) font.height() + 0.5);
                            break;
                    }
                    {
                        String text = showInfo.isToggled() ? moduleName + moduleInfo : moduleName;
                        if (lowercase.isToggled()) {
                            text = text.toLowerCase();
                        }
                        int e = Theme.getGradient((int) theme.getInput(), n2);
                        if (theme.getInput() == 0) {
                            n2 -= 120;
                        } else {
                            n2 -= 12;
                        }
                        double n3 = hudX;
                        double width = font.width(text) + 1;
                        if (alignRight.isToggled()) {
                            n3 -= width - MinecraftFontRenderer.INSTANCE.width("ModuleExample");
                        }
                        if (bloomType.getInput() == 3 || bloomType.getInput() == 1) {
                            BlurUtils.prepareBloom();
                            drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, Color.black);
                            BlurUtils.bloomEnd(2, 2F);
                        }
                        switch ((int) blurss.getInput()) {
                            case 0:
                                break;
                            case 1:
                                BlurUtils.prepareBlur();
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, Color.black);
                                BlurUtils.blurEnd(2, 0.5F);
                                break;
                            case 2:
                                BlurUtils.prepareBlur();
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, Color.black);
                                BlurUtils.blurEnd(2, 0.1F);
                                break;
                            case 3:
                                BlurUtils.prepareBlur();
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, Color.black);
                                BlurUtils.blurEnd(2, 1.75F);
                                break;
                        }
                        switch ((int) bgss.getInput()) {
                            case 0:
                                break;
                            case 1:
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, new Color(0, 0, 0, 64).getRGB());
                                break;
                            case 2:
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, new Color(0, 0, 0, 128).getRGB());
                                break;
                            case 3:
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, new Color(32, 32, 36, 255).getRGB());
                                break;
                        }
                        switch ((int) cbgss.getInput()) {
                            case 0:
                                break;
                            case 1:
                                e = RenderUtils.toArgb(Color.WHITE, 255);
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 32));
                                break;
                            case 2:
                                e = RenderUtils.toArgb(Color.WHITE, 255);
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 92));
                                break;
                            case 3:
                                e = RenderUtils.toArgb(Color.WHITE, 255);
                                drawRound((float) (n3 - 1.5), n - 1, (float) (width + 3), bh, 0, RenderUtils.colorWithAlpha(Theme.getGradient(theme.getInput(), 0), 255));
                                break;
                        }
                        switch ((int) outliness.getInput()) {
                            case 0:
                                break;
                            case 1:
                                RenderUtils.drawRect(alignRight.isToggled() ? n3 + width : n3 - 2, n - 1, alignRight.isToggled() ? n3 + width + 1 : n3 - 1, n + (float) font.height() + 0.7, wout.isToggled() ? 0xFFFFFFFF : Theme.getGradient((int) theme.getInput(), 0.0));
                                break;
                            case 2:
                                if (bloomType.getInput() == 3 || bloomType.getInput() == 2) BlurUtils.prepareBloom();
                                drawRound((float) (n3 + width + 2.2), n + (bh * 0.1f), 1f, bh * 0.8f, 1f, wout.isToggled() ? 0xFFFFFFFF : Theme.getGradient((int) theme.getInput(), 0.0));
                                if (bloomType.getInput() == 3 || bloomType.getInput() == 2) BlurUtils.bloomEnd(2, 2F);
                                drawRound((float) (n3 + width + 2.2), n + (bh * 0.1f), 1f, bh * 0.8f, 1f, wout.isToggled() ? 0xFFFFFFFF : Theme.getGradient((int) theme.getInput(), 0.0));
                                break;
                        }
                        if (wText.isToggled()) {
                            e = RenderUtils.toArgb(Color.WHITE, 255);
                        }
                        font.drawString(text, n3, n, e, dropShadow.isToggled());
                        if (fonts.getInput() != 5) {
                            n += (int) (font.height() + 2);
                        } else {
                            n += (int) (font.height() + 3);
                        }
                    }
                }
            }
        }
    }

    static class EditScreen extends GuiScreen {
        final String example = "ModuleExample";
        GuiButtonExt resetPosition;
        boolean dragging = false;
        int dragStartX = 0;
        int dragStartY = 0;
        int initialHudX = 0;
        int initialHudY = 0;

        public void initGui() {
            super.initGui();
            this.buttonList.add(this.resetPosition = new GuiButtonExt(1, this.width - 90, 5, 85, 20, "Reset position"));
            this.initialHudX = HUD.hudX;
            this.initialHudY = HUD.hudY;
            Keyboard.enableRepeatEvents(true);
        }

        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            drawRect(0, 0, this.width, this.height, -1308622848);

            drawModulesOrExample(mc.fontRendererObj);

            HUD.hudX = this.initialHudX;
            HUD.hudY = this.initialHudY;
            ScaledResolution res = new ScaledResolution(this.mc);
            int infoX = res.getScaledWidth() / 2 - 84;
            int infoY = res.getScaledHeight() / 2 - 20;
            RenderUtils.dct("Edit the HUD position by dragging.", '-', infoX, infoY, 2L, 0L, true, this.mc.fontRendererObj);

            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        private void drawModulesOrExample(FontRenderer fr) {
            int x = this.initialHudX;
            int y = this.initialHudY;
            String[] parts = example.split("-");
            for (String part : parts) {
                fr.drawString(part, x, y, Color.white.getRGB());
                y += 12;
            }
        }

        public boolean doesGuiPauseGame() {
            return false;
        }

        public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
            super.mouseClicked(mouseX, mouseY, button);
            if (button == 0 && isHovered(this.initialHudX, this.initialHudY, mouseX, mouseY)) {
                this.dragging = true;
                this.dragStartX = mouseX;
                this.dragStartY = mouseY;
            }
        }

        private boolean isHovered(int hudX, int hudY, int mouseX, int mouseY) {
            int hudWidth = 50;
            int hudHeight = 32;
            return mouseX >= hudX && mouseX <= hudX + hudWidth && mouseY >= hudY && mouseY <= hudY + hudHeight;
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            super.mouseReleased(mouseX, mouseY, state);
            this.dragging = false;
        }

        public void handleMouseInput() throws IOException {
            super.handleMouseInput();
            if (this.dragging) {

                int deltaX = Mouse.getEventX() * this.width / mc.displayWidth - this.dragStartX;
                int deltaY = this.height - Mouse.getEventY() * this.height / mc.displayHeight - 1 - this.dragStartY;

                this.initialHudX += deltaX;
                this.initialHudY += deltaY;


                this.dragStartX += deltaX;
                this.dragStartY += deltaY;
            }
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                this.mc.displayGuiScreen(null);
            }
            super.keyTyped(typedChar, keyCode);
        }

        @Override
        public void onGuiClosed() {
            Keyboard.enableRepeatEvents(false);
        }
    }
}