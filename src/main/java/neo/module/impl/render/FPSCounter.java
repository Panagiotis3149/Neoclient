package neo.module.impl.render;

import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.script.ScriptDefaults;
import neo.util.Utils;

import neo.util.font.FontManager;
import neo.util.font.MinecraftFontRenderer;
import neo.util.font.impl.FontRenderer;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.gui.GuiScreen;  
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;


public class FPSCounter extends Module {
    private final ButtonSetting xt;
    private final ButtonSetting blr;
    private final ButtonSetting gl;
    public static int Y = 4;
    public static int X = 4;
    private final ButtonSetting mf;
    private final ButtonSetting bg;

    public FPSCounter() {
        super("FPSCounter", Module.category.render, 0);
        this.registerSetting(mf = new ButtonSetting("Minecraft Font", false));
        this.registerSetting(xt = new ButtonSetting("FPS: Prefix", true));
        this.registerSetting(bg = new ButtonSetting("Background", true));
        this.registerSetting(blr = new ButtonSetting("Blur", false));
        this.registerSetting(gl = new ButtonSetting("Shadow", true));
        this.registerSetting(new ButtonSetting("Edit position", () -> mc.displayGuiScreen(new  EditScreen())));
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !Utils.isnull()) return;
        if ((mc.currentScreen != null || mc.gameSettings.showDebugInfo) && !(mc.currentScreen instanceof FPSCounter.EditScreen)) return;
        int FPS = ScriptDefaults.client.getFPS();
        String text = xt.isToggled() ? "FPS: " + FPS : String.valueOf(FPS);
        if (mf.isToggled()) {
            MinecraftFontRenderer font = MinecraftFontRenderer.INSTANCE;
            if (bg.isToggled() && !blr.isToggled()) { RoundedUtils.drawRound((float) X, (float) Y, 15.0f + (float) font.width(text), 15.0f, 4, new Color(0, 0, 0, 128)); } if (bg.isToggled() && blr.isToggled()) {
                BlurUtils.prepareBlur();
                RoundedUtils.drawRound((float) X, (float) Y, 15.0f + (float) font.width(text), 15.0f, 4, Color.BLACK);
                BlurUtils.blurEnd(2, 1.05f);
            }
            if (gl.isToggled()) {
                BlurUtils.prepareBloom();
                if (bg.isToggled()) RoundedUtils.drawRound((float) X, (float) Y, 15.0f + (float) font.width(text), 15.0f, 4, Color.BLACK);
                BlurUtils.bloomEnd(2, 1);
            }
            font.drawString(text, X + (15.0f + (float) font.width(text)) / 2 - (font.width(text) / 2) , Y + (15.0f / 2) - (font.height() / 2), 0xFFFFFFFF);
        } else {
            FontRenderer font = FontManager.productSans20;
            if (bg.isToggled() && !blr.isToggled()) { RoundedUtils.drawRound((float) X, (float) Y, 15.0f + (float) font.width(text), 15.0f, 4, new Color(0, 0, 0, 128)); } if (bg.isToggled() && blr.isToggled()) {
                BlurUtils.prepareBlur();
                RoundedUtils.drawRound((float) X, (float) Y, 15.0f + (float) font.width(text), 15.0f, 4, Color.BLACK);
                BlurUtils.blurEnd(2, 1.05f);
            }
            if (gl.isToggled()) {
                BlurUtils.prepareBloom();
                if (bg.isToggled()) RoundedUtils.drawRound((float) X, (float) Y, 15.0f + (float) font.width(text), 15.0f, 4, Color.BLACK);
                BlurUtils.bloomEnd(2, 1);
            }
            font.drawString(text, X + (15.0f + (float) font.width(text)) / 2 - (font.width(text) / 2) , Y + (15.0f / 2) - (font.height() / 2), 0xFFFFFFFF);
        }
    }

    static class EditScreen extends GuiScreen {
        GuiButtonExt resetPosition;
        boolean dragging = false;
        int dragStartX = 0;
        int dragStartY = 0;
        int initialX = 0;
        int initialY = 0;

        public void initGui() {
            super.initGui();
            this.buttonList.add(this.resetPosition = new GuiButtonExt(1, this.width - 90, 5, 85, 20, "Reset position"));
            this.initialX = FPSCounter.X;
            this.initialY = FPSCounter.Y;
            Keyboard.enableRepeatEvents(true);
        }

        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            drawRect(0, 0, this.width, this.height, -1308622848);


            FPSCounter.X = this.initialX;
            FPSCounter.Y = this.initialY;
            super.drawScreen(mouseX, mouseY, partialTicks);
        }


        public boolean doesGuiPauseGame() {
            return false;
        }

        public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
            super.mouseClicked(mouseX, mouseY, button);
            if (button == 0 && isHovered(this.initialX, this.initialY, mouseX, mouseY)) {
                this.dragging = true;
                this.dragStartX = mouseX;
                this.dragStartY = mouseY;
            }
        }

        private boolean isHovered(int X, int Y, int mouseX, int mouseY) {
            int hudWidth = 50;
            int hudHeight = 32;
            return mouseX >= X && mouseX <= X + hudWidth && mouseY >= Y && mouseY <= Y + hudHeight;
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

                this.initialX += deltaX;
                this.initialY += deltaY;


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