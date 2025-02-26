package keystrokesmod.clickgui.components.impl;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.Component;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.profile.Manager;
import keystrokesmod.utility.profile.ProfileModule;;
import org.lwjgl.opengl.GL11;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import static keystrokesmod.utility.RenderUtils.drawRoundedGradientRect;
import static keystrokesmod.utility.Theme.getColors;

public class ModuleComponent extends Component {
    private final int unsavedColor = new Color(114, 188, 250).getRGB();
    private final int invalidColor = new Color(255, 80, 80).getRGB();
    private final double enabledColor = (new Color(255, 255, 255, 255)).getRGB();
    private final int disabledColor = new Color(192, 192, 192).getRGB();
    public Module mod;
    public CategoryComponent categoryComponent;
    public int o;
    public ArrayList<Component> settings;
    public boolean isOpened;
    private boolean hovering;

    public ModuleComponent(Module mod, CategoryComponent p, int o) {
        this.mod = mod;
        this.categoryComponent = p;
        this.o = o;
        this.settings = new ArrayList();
        this.isOpened = false;
        int y = o + 12;
        if (mod != null && !mod.getSettings().isEmpty()) {
            for (Setting v : mod.getSettings()) {
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
        Iterator var3 = this.settings.iterator();

        while (true) {
            while (var3.hasNext()) {
                Component co = (Component) var3.next();
                co.so(y);
                if (co instanceof SliderComponent) {
                    y += 16;
                } else if (co instanceof ButtonComponent || co instanceof BindComponent || co instanceof DescriptionComponent) {
                    y += 12;
                }
            }

            return;
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
        FontRenderer font = FontManager.helveticaNeue;

        int[] colors = getColors((int) Gui.theme.getInput());
        int firstColor = colors[0];
        int secondColor = colors[1];

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        boolean isLastModule = this.categoryComponent.getModules().indexOf(this.mod) == this.categoryComponent.getModules().size() - 1;
        int roundedness = isLastModule ? 12 : 2;

        drawRoundedGradientRect(
                this.categoryComponent.getX(),
                this.categoryComponent.getY() + o,
                this.categoryComponent.getX() + this.categoryComponent.getWidth(),
                this.categoryComponent.getY() + 16 + this.o,
                roundedness,
                this.mod.isEnabled() ? firstColor : 0xFF202024,
                this.mod.isEnabled() ? firstColor : 0xFF202024,
                this.mod.isEnabled() ? secondColor : 0xFF202024,
                this.mod.isEnabled() ? secondColor : 0xFF202024
        );

        GL11.glPushMatrix();
        double button_rgb;


        if (this.mod.isEnabled()) {
            button_rgb = enabledColor;
        } else {
            button_rgb = disabledColor;
        }

        if (this.mod.script != null && this.mod.script.error) {
            button_rgb = invalidColor;
        }

        if (this.mod.moduleCategory() == Module.category.profiles && !(this.mod instanceof Manager)
                && !((ProfileModule) this.mod).saved && Raven.currentProfile.getModule() == this.mod) {
            button_rgb = unsavedColor;
        }

        if (this.isOpened && !this.settings.isEmpty()) {
            for (Component c : this.settings) {
                c.render();
            }
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();


        float textX = this.categoryComponent.getX() + 5;
        font.drawString(this.mod.getName(), textX, this.categoryComponent.getY() + this.o + 4, (int) button_rgb);

        GL11.glPopMatrix();
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
                        h += 16;
                    } else if (c instanceof ButtonComponent || c instanceof BindComponent || c instanceof DescriptionComponent) {
                        h += 12;
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
        if (overModuleName(x, y) && this.categoryComponent.opened) {
            hovering = true;
        }
        else {
            hovering = false;
        }
    }

    public String getName() {
        return mod.getName();
    }

    public boolean onClick(int x, int y, int b) {
        if (this.overModuleName(x, y) && b == 0 && this.mod.canBeEnabled()) {
            this.mod.toggle();
            if (this.mod.moduleCategory() != Module.category.profiles) {
                if (Raven.currentProfile != null) {
                    ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
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
