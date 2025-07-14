package neo.gui.click.components.impl;

import neo.Neo;
import neo.gui.click.components.Component;
import neo.module.Module;
import neo.module.impl.client.Gui;
import neo.module.setting.Setting;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import neo.util.other.MathUtil;
import neo.util.config.ConfigModule;
import neo.util.render.animation.Timer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;

import static neo.util.render.RenderUtils.drawRoundedGradientRect;
import static neo.util.render.Theme.getColors;

public class ModuleComponent extends Component {
    public Module mod;
    public CategoryComponent categoryComponent;
    public int o;
    public ArrayList<Component> settings;
    public boolean isOpened;
    private boolean hovering;
    private Timer opacityTimer = new Timer(500F);
    private Timer opacityOutTimer = new Timer(500F);


    public ModuleComponent(Module mod, CategoryComponent p, int o) {
        this.mod = mod;
        this.categoryComponent = p;
        this.o = o;
        this.settings = new ArrayList();
        this.isOpened = false;
        int y = o + 12;
        if (mod != null && !mod.getSettings().isEmpty()) {
            for (Setting v : mod.getSettings()) {
                if (!v.isVisible()) continue;
                if (v instanceof SliderSetting) {
                    SliderSetting n = (SliderSetting) v;
                    SliderComponent s = new SliderComponent(n, this, y);
                    this.settings.add(s);
                    y += 12;
                } else if (v instanceof ButtonSetting) {
                    ButtonSetting b = (ButtonSetting) v;
                    ButtonComponent c = new ButtonComponent(mod, b, this, y);
                    this.settings.add(c);
                    y += 12;
                } else if (v instanceof DescriptionSetting) {
                    DescriptionSetting d = (DescriptionSetting) v;
                    DescriptionComponent m = new DescriptionComponent(d, this, y);
                    this.settings.add(m);
                    y += 12;
                }
            }
        }
        this.settings.add(new BindComponent(this, y));
    }

    public void so(int n) {
        this.o = n;
        int y = this.o + 16;

        for (Component co : this.settings) {
            boolean vis;

            if (co instanceof SliderComponent) {
                vis = ((SliderComponent) co).sliderSetting.isVisible();
            } else if (co instanceof ButtonComponent) {
                vis = ((ButtonComponent) co).buttonSetting.isVisible();
            } else if (co instanceof DescriptionComponent) {
                vis = ((DescriptionComponent) co).desc.isVisible();
            } else {
                vis = true;
            }

            if (!vis) continue;

            co.so(y);
            if (co instanceof SliderComponent) {
                y += 16;
            } else {
                y += 12;
            }
        }
    }

    public static void e() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void f() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
        GL11.glEdgeFlag(true);
    }

    public static void g(int h) {
        float a = 0.0F;
        float r = 0.0F;
        float g = 0.0F;
        float b = 0.0F;
        GL11.glColor4f(r, g, b, a);
    }

    public static void v(float x, float y, float x1, float y1, int t, int b) {
        e();
        GL11.glShadeModel(7425);
        GL11.glBegin(7);
        g(t);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        g(b);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GL11.glShadeModel(7424);
        f();
    }

    public void render() {
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        FontRenderer font = FontManager.productSans20;

        int[] baseColors = getColors((int) Gui.theme.getInput());
        int baseFirstColor = baseColors[0];
        int baseSecondColor = baseColors[1];

        int alpha;

        if (this.mod.isEnabled()) {
            if (opacityTimer != null) {
                alpha = (int) opacityTimer.getValueFloat(0f, 255f, 2);
                if (alpha >= 255) opacityTimer = null;
            } else {
                alpha = 255;
            }
        } else {
            if (opacityOutTimer != null) {
                alpha = (int) MathUtil.inverseFloat(opacityOutTimer.getValueFloat(0f, 255f, 2), 0, 255f);
                if (alpha <= 0) opacityOutTimer = null;
            } else {
                alpha = 0;
            }
        }

        if (alpha > 255) alpha = 255;
        if (alpha < 0) alpha = 0;

        int firstColor = (alpha << 24) | (baseFirstColor & 0x00FFFFFF);
        int secondColor = (alpha << 24) | (baseSecondColor & 0x00FFFFFF);


        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        boolean isLastModule = this.categoryComponent.getModules().indexOf(this.mod) == this.categoryComponent.getModules().size() - 1;
        int roundness = isLastModule ? 12 : 2;

        drawRoundedGradientRect(
                this.categoryComponent.getX(),
                this.categoryComponent.getY() + o,
                this.categoryComponent.getX() + this.categoryComponent.getWidth(),
                this.categoryComponent.getY() + 16 + this.o,
                roundness,
                firstColor,
                firstColor,
                secondColor,
                secondColor
        );

        GL11.glPushMatrix();


        if (this.isOpened && !this.settings.isEmpty()) {
            for (Component c : this.settings) {
                c.render();
            }
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();
        float textX = (float) (this.categoryComponent.getX() + (this.categoryComponent.getWidth() / 2f) - (font.width(this.mod.getName()) / 2f));

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.resetColor();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableLighting();
        font.drawString(this.mod.getName(), textX, this.categoryComponent.getY() + this.o + 4,  0xFFFFFFFF);
        GlStateManager.resetColor();
        GL11.glPopMatrix();
        GL11.glPopMatrix(); // FIXED I THINK
    }


    public int getHeight() {
        if (!this.isOpened) {
            return 16;
        } else {
            int h = 16;
            Iterator var2 = this.settings.iterator();

            while (true) {
                while (var2.hasNext()) {
                    Component c = (Component) var2.next();
                    if (c instanceof SliderComponent) {
                        if (((SliderComponent) c).sliderSetting.isVisible()) {
                            h += 16;
                        }
                    } else if (c instanceof ButtonComponent || c instanceof BindComponent || c instanceof DescriptionComponent) {
                        if (c instanceof ButtonComponent) {
                            if (((ButtonComponent) c).buttonSetting.isVisible()) {
                                h += 16;
                            }
                        } else if (c instanceof DescriptionComponent) {
                            if (((DescriptionComponent) c).desc.isVisible()) {
                                h += 16;
                            }
                        } else if (c instanceof BindComponent) {
                            h += 16;
                        }
                    }
                }

                return h;
            }
        }
    }

    public void drawScreen(int x, int y) {
        if (!this.settings.isEmpty()) {
            for (Component c : this.settings) {
                c.drawScreen(x, y);
            }
        }
        hovering = overModuleName(x, y) && this.categoryComponent.opened;
    }

    public String getName() {
        return mod.getName();
    }

    public boolean onClick(int x, int y, int b) {
        if (this.overModuleName(x, y) && b == 0 && this.mod.canBeEnabled()) {
            boolean wasEnabled = this.mod.isEnabled();

            this.mod.toggle();

            if (this.mod.isEnabled()) {
                this.opacityOutTimer = null;
                opacityTimer = new Timer(500F);
                this.opacityTimer.start();
            } else if (wasEnabled) {
                this.opacityTimer = null;
                opacityOutTimer = new Timer(500F);
                this.opacityOutTimer.start();
            }

            if (this.mod.moduleCategory() != Module.category.config) {
                if (Neo.currentConfig != null) {
                    ((ConfigModule) Neo.currentConfig.getModule()).saved = false;
                }
            }
        }

        if (this.overModuleName(x, y) && b == 1) {
            this.isOpened = !this.isOpened;
            this.categoryComponent.render();
            return true;
        }

        for (Component c : this.settings) {
            c.onClick(x, y, b);
        }
        return false;
    }

    public void mouseReleased(int x, int y, int m) {
        for (Component c : this.settings) {
            c.mouseReleased(x, y, m);
        }

    }

    public void keyTyped(char t, int k) {
        for (Component c : this.settings) {
            c.keyTyped(t, k);
        }
    }

    public void onGuiClosed() {
        for (Component c : this.settings) {
            c.onGuiClosed();
        }
    }

    public boolean overModuleName(int x, int y) {
        return x > this.categoryComponent.getX() && x < this.categoryComponent.getX() + this.categoryComponent.getWidth() && y > this.categoryComponent.getModuleY() + this.o && y < this.categoryComponent.getModuleY() + 16 + this.o;
    }
}
