package keystrokesmod.clickgui.components.impl;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.Component;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.profile.ProfileModule;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static keystrokesmod.utility.RenderUtils.drawRoundedGradientRect;

public class SliderComponent extends Component {
    private SliderSetting sliderSetting;
    private ModuleComponent moduleComponent;
    private int o;
    private int x;
    private int y;
    private boolean heldDown = false;
    private double w;

    public SliderComponent(SliderSetting sliderSetting, ModuleComponent moduleComponent, int o) {
        this.sliderSetting = sliderSetting;
        this.moduleComponent = moduleComponent;
        this.x = moduleComponent.categoryComponent.getX() + moduleComponent.categoryComponent.getWidth();
        this.y = moduleComponent.categoryComponent.getY() + moduleComponent.o;
        this.o = o;
    }

    public void render() {
        FontRenderer font = FontManager.helveticaNeue;
        RenderUtils.drawRect(
                this.moduleComponent.categoryComponent.getX() + 4,
                this.moduleComponent.categoryComponent.getY() + this.o + 11,
                this.moduleComponent.categoryComponent.getX() + 4 + this.moduleComponent.categoryComponent.getWidth() - 8,
                this.moduleComponent.categoryComponent.getY() + this.o + 15,
                0xE5141414
        );

        // Get the selected theme as a double
        double selectedTheme = Gui.theme.getInput();
        Color firstGradient = null;
        Color secondGradient = null;

        // Determine the gradient colors based on the selected theme
        if (selectedTheme == 0.0) { // Rainbow
            firstGradient = null;
            secondGradient = null;
        } else if (selectedTheme == 1.0) { // Cherry
            firstGradient = new Color(255, 200, 200);
            secondGradient = new Color(243, 58, 106);
        } else if (selectedTheme == 2.0) { // Cotton Candy
            firstGradient = new Color(99, 249, 255);
            secondGradient = new Color(255, 104, 204);
        } else if (selectedTheme == 3.0) { // Flare
            firstGradient = new Color(231, 39, 24);
            secondGradient = new Color(245, 173, 49);
        } else if (selectedTheme == 4.0) { // Flower
            firstGradient = new Color(215, 166, 231);
            secondGradient = new Color(211, 90, 232);
        } else if (selectedTheme == 5.0) { // Gold
            firstGradient = new Color(255, 215, 0);
            secondGradient = new Color(240, 159, 0);
        } else if (selectedTheme == 6.0) { // Grayscale
            firstGradient = new Color(240, 240, 240);
            secondGradient = new Color(110, 110, 110);
        } else if (selectedTheme == 7.0) { // Royal
            firstGradient = new Color(125, 204, 241);
            secondGradient = new Color(30, 71, 170);
        } else if (selectedTheme == 8.0) { // Sky
            firstGradient = new Color(160, 230, 225);
            secondGradient = new Color(15, 190, 220);
        } else if (selectedTheme == 9.0) { // Vine
            firstGradient = new Color(17, 192, 45);
            secondGradient = new Color(201, 234, 198);
        } else if (selectedTheme == 10.0) { // Steelvoid
            firstGradient = new Color(55, 73, 98);
            secondGradient = new Color(125, 170, 223);
        } else if (selectedTheme == 11) { // Mist
            firstGradient = new Color(94, 228, 154);
            secondGradient = new Color(40, 139, 207);
        }

        // Get the RGB values in RGBA Java Hex format
        int firstColor = firstGradient != null ? 0xFF000000 | firstGradient.getRGB() : Utils.getChroma(2, 0);
        int secondColor = secondGradient != null ? 0xFF000000 | secondGradient.getRGB() : Utils.getChroma(2, 0);



        int l = this.moduleComponent.categoryComponent.getX() + 4;
        int r = this.moduleComponent.categoryComponent.getX() + 4 + (int) this.w;
        if (r - l > 84) {
            r = l + 84;
        }

        drawRoundedGradientRect(
                l,
                this.moduleComponent.categoryComponent.getY() + this.o + 9,
                r,
                this.moduleComponent.categoryComponent.getY() + this.o + 15.3F,
                (float) 2,
                firstColor,
                firstColor,
                secondColor,
                secondColor
        );



        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);

        String value;
        double input = this.sliderSetting.getInput();
        String info = this.sliderSetting.getInfo();
        if (input != 1 && (info.equals(" second") || info.equals(" block"))) {
            info += "s";
        }
        if (this.sliderSetting.isString) {
            value = this.sliderSetting.getOptions()[(int) this.sliderSetting.getInput()];
        } else {
            value = Utils.isWholeNumber(input) ? (int) input + "" : String.valueOf(input);
        }

        font.drawString(
                this.sliderSetting.getName() + ": " + (this.sliderSetting.isString ? "§e" : "§b") + value + info,
                (float) ((this.moduleComponent.categoryComponent.getX() + 4) * 2.0F),
                (float) ((this.moduleComponent.categoryComponent.getY() + this.o + 3) * 2.0F),
                -1,
                false // No drop shadow
        );
        GL11.glPopMatrix();
    }


    public void so(int n) {
        this.o = n;
    }

    public void drawScreen(int x, int y) {
        this.y = this.moduleComponent.categoryComponent.getModuleY() + this.o;
        this.x = this.moduleComponent.categoryComponent.getX();
        double d = Math.min(this.moduleComponent.categoryComponent.getWidth() - 8, Math.max(0, x - this.x));
        this.w = (double) (this.moduleComponent.categoryComponent.getWidth() - 8) * (this.sliderSetting.getInput() - this.sliderSetting.getMin()) / (this.sliderSetting.getMax() - this.sliderSetting.getMin());
        if (this.heldDown) {
            if (d == 0.0D) {
                if (this.sliderSetting.getInput() != this.sliderSetting.getMin() && ModuleManager.hud != null && ModuleManager.hud.isEnabled() && !ModuleManager.organizedModules.isEmpty()) {
                    ModuleManager.sort();
                }
                this.sliderSetting.setValue(this.sliderSetting.getMin());
            } else {
                double n = roundToInterval(d / (double) (this.moduleComponent.categoryComponent.getWidth() - 8) * (this.sliderSetting.getMax() - this.sliderSetting.getMin()) + this.sliderSetting.getMin(), 4);
                if (this.sliderSetting.getInput() != n && ModuleManager.hud != null && ModuleManager.hud.isEnabled() && !ModuleManager.organizedModules.isEmpty()) {
                    ModuleManager.sort();
                }
                this.sliderSetting.setValue(n);
            }
            if (Raven.currentProfile != null) {
                ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
            }
        }

    }

    private static double roundToInterval(double v, int p) {
        if (p < 0) {
            return 0.0D;
        } else {
            BigDecimal bd = new BigDecimal(v);
            bd = bd.setScale(p, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
    }

    public boolean onClick(int x, int y, int b) {
        if ((this.u(x, y) || this.i(x, y)) && b == 0 && this.moduleComponent.isOpened) {
            this.heldDown = true;
        }
        return false;
    }

    public void mouseReleased(int x, int y, int m) {
        this.heldDown = false;
    }

    public boolean u(int x, int y) {
        return x > this.x && x < this.x + this.moduleComponent.categoryComponent.getWidth() / 2 + 1 && y > this.y && y < this.y + 16;
    }

    public boolean i(int x, int y) {
        return x > this.x + this.moduleComponent.categoryComponent.getWidth() / 2 && x < this.x + this.moduleComponent.categoryComponent.getWidth() && y > this.y && y < this.y + 16;
    }

    @Override
    public void onGuiClosed() {
        this.heldDown = false;
    }
}
