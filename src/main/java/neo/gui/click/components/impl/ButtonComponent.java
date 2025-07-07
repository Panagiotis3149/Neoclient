package neo.gui.click.components.impl;

import neo.Neo;
import neo.gui.click.components.Component;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.config.ConfigModule;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ButtonComponent extends Component {
    private final int c = (new Color(20, 255, 0)).getRGB();
    private final Module mod;
    public final ButtonSetting buttonSetting;
    private final ModuleComponent p;
    private int o;
    private int x;
    private int y;

    public ButtonComponent(Module mod, ButtonSetting op, ModuleComponent b, int o) {
        this.mod = mod;
        this.buttonSetting = op;
        this.p = b;
        this.x = b.categoryComponent.getX() + b.categoryComponent.getWidth();
        this.y = b.categoryComponent.getY() + b.o;
        this.o = o;
    }

    public void render() {
        if (!buttonSetting.isVisible()) return;
        /*
        RenderUtils.drawRect(
                this.p.categoryComponent.getX() + 4,
                this.p.categoryComponent.getY() + this.o + 4,
                this.p.categoryComponent.getX() + 4 + this.p.categoryComponent.getWidth() - 8,
                this.p.categoryComponent.getY() + this.o + 14, // Adjust height as needed
                0xBF1C1C1C
        );
         */

        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);

        FontRenderer font = FontManager.productSans20;
        font.drawString(
                (this.buttonSetting.isMethodButton ? "[~]  " : (this.buttonSetting.isToggled() ? "[+]  " : "[-]  ")) + this.buttonSetting.getName(),
                (float) ((this.p.categoryComponent.getX() + 4) * 2),
                (float) ((this.p.categoryComponent.getY() + this.o + 4) * 2),
                this.buttonSetting.isToggled() ? this.c : -1,
                false // No drop shadow
        );
        GL11.glPopMatrix();
    }

    public void so(int n) {
        this.o = n;
    }

    public void drawScreen(int x, int y) {
        if (!buttonSetting.isVisible()) return;
        this.y = this.p.categoryComponent.getModuleY() + this.o;
        this.x = this.p.categoryComponent.getX();
    }

    public boolean onClick(int x, int y, int b) {
        if (!buttonSetting.isVisible()) return false;
        if (this.i(x, y) && b == 0 && this.p.isOpened) {
            if (this.buttonSetting.isMethodButton) {
                this.buttonSetting.runMethod();
                return false;
            }
            this.buttonSetting.toggle();
            this.mod.guiButtonToggled(this.buttonSetting);
            if (Neo.currentConfig != null) {
                ((ConfigModule) Neo.currentConfig.getModule()).saved = false;
            }
        }
        return false;
    }

    public boolean i(int x, int y) {
        return x > this.x && x < this.x + this.p.categoryComponent.getWidth() && y > this.y && y < this.y + 11;
    }
}
