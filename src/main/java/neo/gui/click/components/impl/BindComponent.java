package neo.gui.click.components.impl;

import neo.Neo;
import neo.gui.click.components.Component;
import neo.module.Module;
import neo.module.impl.client.Gui;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.render.Theme;
import neo.util.config.ConfigModule;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class BindComponent extends Component {
    public boolean isBinding;
    private final ModuleComponent moduleComponent;
    private int bind;
    private int x;
    private int y;

    public BindComponent(ModuleComponent moduleComponent, int bind) {
        this.moduleComponent = moduleComponent;
        this.x = moduleComponent.categoryComponent.getX() + moduleComponent.categoryComponent.getWidth();
        this.y = moduleComponent.categoryComponent.getY() + moduleComponent.o;
        this.bind = bind;
    }

    public void so(int n) {
        this.bind = n;
    }

    public void render() {
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        this.drawString(!this.moduleComponent.mod.canBeEnabled() && this.moduleComponent.mod.script == null ? "This module cannot be bound!" : this.isBinding ? "Press a key to bind." : "Bound to: '§e" + (this.moduleComponent.mod.getKeycode() >= 1000 ? "M" + (this.moduleComponent.mod.getKeycode() - 1000) : Keyboard.getKeyName(this.moduleComponent.mod.getKeycode())) + "§r'");
        GL11.glPopMatrix();
    }

    public void drawScreen(int x, int y) {
        this.y = this.moduleComponent.categoryComponent.getModuleY() + this.bind;
        this.x = this.moduleComponent.categoryComponent.getX();
    }

    public boolean onClick(int x, int y, int b) {
        if (this.i(x, y) && this.moduleComponent.isOpened && this.moduleComponent.mod.canBeEnabled()) {
            if (b == 0) {
                this.isBinding = !this.isBinding;
            }
            else if (b == 1 && this.moduleComponent.mod.moduleCategory() != Module.category.config) {
                this.moduleComponent.mod.setHidden(!this.moduleComponent.mod.isHidden());
                if (Neo.currentConfig != null) {
                    ((ConfigModule) Neo.currentConfig.getModule()).saved = false;
                }
            }
            else if (b > 1) {
                if (this.isBinding) {
                    this.moduleComponent.mod.setBind(b + 1000);
                    if (Neo.currentConfig != null) {
                        ((ConfigModule) Neo.currentConfig.getModule()).saved = false;
                    }
                    this.isBinding = false;
                }
            }
        }
        return false;
    }

    public void keyTyped(char t, int keybind) {
        if (this.isBinding) {
            if (keybind == Keyboard.KEY_0 || keybind == Keyboard.KEY_ESCAPE) {
                if (this.moduleComponent.mod instanceof Gui) {
                    this.moduleComponent.mod.setBind(54);
                } else {
                    this.moduleComponent.mod.setBind(0);
                }
                if (Neo.currentConfig != null) {
                    ((ConfigModule) Neo.currentConfig.getModule()).saved = false;
                }
            }
            else {
                if (Neo.currentConfig != null) {
                    ((ConfigModule) Neo.currentConfig.getModule()).saved = false;
                }
                this.moduleComponent.mod.setBind(keybind);
            }

            this.isBinding = false;
        }
    }

    public boolean i(int x, int y) {
        return x > this.x && x < this.x + this.moduleComponent.categoryComponent.getWidth() && y > this.y - 1 && y < this.y + 12;
    }

    public int getHeight() {
        return 16;
    }

    private void drawString(String s) {
        FontRenderer font = FontManager.productSans20;
        font.drawString(s, (float) ((this.moduleComponent.categoryComponent.getX() + 4) * 2), (float) ((this.moduleComponent.categoryComponent.getY() + this.bind + 3) * 2), !this.moduleComponent.mod.hidden ? Theme.getGradient(Theme.descriptor[0], Theme.descriptor[1], 0) : Theme.getGradient(Theme.hiddenBind[0], Theme.hiddenBind[1], 0), true);
    }

    public void onGuiClosed() {
        this.isBinding = false;
    }
}
