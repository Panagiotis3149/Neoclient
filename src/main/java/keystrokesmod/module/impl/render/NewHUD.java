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
import keystrokesmod.utility.shader.KawaseBloom;
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

public class NewHUD extends Module {
    public static SliderSetting theme;
    public static int hudX = 900;
    public static int hudY = 10;


    public NewHUD() {
        super("NewHUD", Module.category.render);
        this.registerSetting(new DescriptionSetting("Right click bind to hide modules."));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
        this.registerSetting(new ButtonSetting("Edit position", () -> {
            mc.displayGuiScreen(new HUD.EditScreen());
        }));
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


        class EditScreen extends GuiScreen {
            final String example = "Example";
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
}