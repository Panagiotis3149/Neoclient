package neo.gui.click.components.impl;

import neo.Neo;
import neo.gui.click.components.Component;
import neo.module.ModuleManager;
import neo.module.impl.client.Gui;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.config.ConfigModule;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static neo.util.render.RenderUtils.drawRoundedGradientRect;
import static neo.util.render.Theme.getColors;

public class SliderComponent extends Component {
    public final SliderSetting sliderSetting;
    private final ModuleComponent moduleComponent;
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
        if (!sliderSetting.isVisible()) return;
        FontRenderer font = FontManager.productSans20;
        /*
        RenderUtils.drawRect(
                this.moduleComponent.categoryComponent.getX() + 4,
                this.moduleComponent.categoryComponent.getY() + this.o + 11,
                this.moduleComponent.categoryComponent.getX() + 4 + this.moduleComponent.categoryComponent.getWidth() - 8,
                this.moduleComponent.categoryComponent.getY() + this.o + 15,
                0xBF1C1C1C
        );
        */

        int[] colors = getColors((int) Gui.theme.getInput());
        int firstColor = colors[0];
        int secondColor = colors[1];

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
                (this.moduleComponent.categoryComponent.getX() + 4) * 2.0F,
                (this.moduleComponent.categoryComponent.getY() + this.o + 3) * 2.0F,
                -1,
                false
        );
        GL11.glPopMatrix();
    }


    public void so(int n) {
        this.o = n;
    }

    public void drawScreen(int x, int y) {
        if (!sliderSetting.isVisible()) return;
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
            if (Neo.currentConfig != null) {
                ((ConfigModule) Neo.currentConfig.getModule()).saved = false;
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
        if (!sliderSetting.isVisible()) return false;
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
