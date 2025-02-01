package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.shader.BlurUtils;
import keystrokesmod.utility.shader.RoundedUtils;
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

public class HUD extends Module {
    public static SliderSetting theme;
    public static ButtonSetting dropShadow;
    public static ButtonSetting alphabeticalSort;
    public static SliderSetting Mode;
    public static ButtonSetting alignRight;
    private static ButtonSetting lowercase;
    public static ButtonSetting showInfo;
    public static int hudX = 5;
    public static int hudY = 70;
    private boolean isAlphabeticalSort;
    private boolean canShowInfo;
    public String[] modes = new String[]{"1", "2", "3", "4"};

    public HUD() {
        super("HUD", Module.category.render);
        this.registerSetting(new DescriptionSetting("Right click bind to hide modules."));
        this.registerSetting(Mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
        this.registerSetting(new ButtonSetting("Edit position", () -> {
            mc.displayGuiScreen(new EditScreen());
        }));
        this.registerSetting(alignRight = new ButtonSetting("Align right", true));
        this.registerSetting(alphabeticalSort = new ButtonSetting("Alphabetical sort", false));
        this.registerSetting(dropShadow = new ButtonSetting("Drop shadow", false));
        this.registerSetting(lowercase = new ButtonSetting("Lowercase", false));
        this.registerSetting(showInfo = new ButtonSetting("Show module info", false));
    }

    private Color averageColors(Color color1, Color color2) {
        int r = (color1.getRed() + color2.getRed()) / 2;
        int g = (color1.getGreen() + color2.getGreen()) / 2;
        int b = (color1.getBlue() + color2.getBlue()) / 2;
        int a = (color1.getAlpha() + color2.getAlpha()) / 2;
        return new Color(r, g, b, a);
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !Utils.nullCheck()) return;
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

            try {
                switch ((int) Mode.getInput()) {
                    case 0:
                        for (Module module : ModuleManager.organizedModules) {
                            if (module.isEnabled() && module != this) {
                                if (module.isHidden() || module == ModuleManager.commandLine) continue;
                                String moduleName = module.getName();

                                keystrokesmod.utility.font.impl.FontRenderer font = FontManager.helveticaNeue;
                                {
                                    String text = moduleName;
                                    if (lowercase.isToggled()) { text = moduleName.toLowerCase(); }
                                    if (!lowercase.isToggled()) { text = moduleName; }
                                    int e = Theme.getGradient((int) theme.getInput(), n2);
                                    if (theme.getInput() == 0) {
                                        n2 -= 120;
                                    } else {
                                        n2 -= 12;
                                    }
                                    double n3 = hudX;
                                    double width = font.width(text);
                                    if (alignRight.isToggled()) {
                                        n3 -= width - 46;
                                    }
                                        RenderUtils.drawRect(n3 - 1, n - 1, n3 + width, n + Math.round(font.height() + 1), new Color(0, 0, 0, 124).getRGB());
                                        RenderUtils.drawRect(alignRight.isToggled() ? n3 + width : n3 - 2, n - 1, alignRight.isToggled() ? n3 + width + 1 : n3 - 1, n + Math.round(font.height() + 1), e);
                                    font.drawString(text, n3, n, e, dropShadow.isToggled());
                                    n += font.height() + 2.5;
                                }
                            }
                        }
                        break;
                    case 1:
                        for (Module module : ModuleManager.organizedModules) {
                            if (module.isEnabled() && module != this) {
                                if (module.isHidden() || module == ModuleManager.commandLine) continue;
                                String moduleName = module.getName();
                                keystrokesmod.utility.font.impl.FontRenderer font1 = FontManager.helveticaNeue;
                                {
                                    String text = moduleName;
                                    if (lowercase.isToggled()) { text = moduleName.toLowerCase(); }
                                    if (!lowercase.isToggled()) { text = moduleName; }
                                    int e = Theme.getGradient((int) theme.getInput(), n2);
                                    if (theme.getInput() == 0) {
                                        n2 -= 120;
                                    } else {
                                        n2 -= 12;
                                    }
                                    double n4 = hudX;
                                    double width = font1.width(text);
                                    if (alignRight.isToggled()) {
                                        n4 -= width - 46;
                                    }
                                    RenderUtils.drawRect(alignRight.isToggled() ? n4 + width : n4 - 2, n - 1, alignRight.isToggled() ? n4 + width + 1 : n4 - 1, n + Math.round(font1.height() + 1), e);
                                    font1.drawString(text, n4, n, e, dropShadow.isToggled());
                                    n += font1.height() + 2;
                                }
                            }
                        }
                        break;
                    case 2:
                        for (Module module : ModuleManager.organizedModules) {
                            if (module.isEnabled() && module != this) {
                                if (module.isHidden() || module == ModuleManager.commandLine) continue;
                                String moduleName = module.getName();
                                MinecraftFontRenderer font = MinecraftFontRenderer.INSTANCE;
                                {
                                    String text = moduleName;
                                    if (lowercase.isToggled()) { text = moduleName.toLowerCase(); }
                                    if (!lowercase.isToggled()) { text = moduleName; }
                                    int e = Theme.getGradient((int) theme.getInput(), n2);
                                    if (theme.getInput() == 0) {
                                        n2 -= 120;
                                    } else {
                                        n2 -= 12;
                                    }
                                    double n4 = hudX;
                                    double width = font.width(text);
                                    if (alignRight.isToggled()) {
                                        n4 -= width - 46;
                                    }
                                    RenderUtils.drawRect(alignRight.isToggled() ? n4 + width : n4 - 2, n - 1, alignRight.isToggled() ? n4 + width + 1 : n4 - 1, n + Math.round(font.height() + 1), e);
                                    font.drawString(text, n4, n, e, dropShadow.isToggled());
                                    n += font.height() + 1;
                                }
                            }
                        }
                        break;
                    case 3:
                        for (Module module : ModuleManager.organizedModules) {
                            if (module.isEnabled() && module != this) {
                                if (module.isHidden() || module == ModuleManager.commandLine) continue;
                                String moduleName = module.getName();
                                MinecraftFontRenderer font = MinecraftFontRenderer.INSTANCE;
                                {
                                    String text = moduleName;
                                    if (lowercase.isToggled()) { text = moduleName.toLowerCase(); }
                                    if (!lowercase.isToggled()) { text = moduleName; }
                                    int e = Theme.getGradient((int) theme.getInput(), n2);
                                    if (theme.getInput() == 0) {
                                        n2 -= 120;
                                    } else {
                                        n2 -= 12;
                                    }
                                    double n4 = hudX;
                                    double width = font.width(text);
                                    if (alignRight.isToggled()) {
                                        n4 -= width - 46;
                                    }
                                    RenderUtils.drawRect(n4 - 1, n - 1, n4 + width, n + Math.round(font.height() + 1), new Color(0, 0, 0, 124).getRGB());
                                    RenderUtils.drawRect(alignRight.isToggled() ? n4 + width : n4 - 2, n - 1, alignRight.isToggled() ? n4 + width + 1 : n4 - 1, n + Math.round(font.height() + 1), e);
                                    font.drawString(text, n4, n, e, dropShadow.isToggled());
                                    n += font.height() + 1;
                                }
                            }
                        }
                        break;
            default:
                break;
                }
            } catch (Exception e) {
                Utils.sendMessage("&cAn error occurred rendering HUD. Check your logs");
                e.printStackTrace();
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