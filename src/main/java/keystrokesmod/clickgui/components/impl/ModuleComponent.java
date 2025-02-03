package keystrokesmod.clickgui.components.impl;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.Component;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.profile.Manager;
import keystrokesmod.utility.profile.ProfileModule;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static keystrokesmod.utility.RenderUtils.drawRect;
import static keystrokesmod.utility.RenderUtils.drawRoundedGradientRect;

public class ModuleComponent extends Component {
    private final int c2 = (new Color(154, 2, 255)).getRGB();
    private final int hoverColor = (new Color(191, 221, 219, 255)).getRGB();
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

        double selectedTheme = Gui.theme.getInput();
        Color firstGradient = null;
        Color secondGradient = null;


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
            firstGradient = new Color(47, 64, 84);
            secondGradient = new Color(125, 179, 223);
        } else if (selectedTheme == 11) { // Mist
            firstGradient = new Color(94, 228, 154);
            secondGradient = new Color(40, 139, 207);
        }


        int firstColor = firstGradient != null ? 0xFF000000 | firstGradient.getRGB() : Utils.getChroma(2, 0);
        int secondColor = secondGradient != null ? 0xFF000000 | secondGradient.getRGB() : Utils.getChroma(2, 0);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        boolean isLastModule = this.categoryComponent.getModules().indexOf(this.mod) == this.categoryComponent.getModules().size() - 1;
        int roundedness = isLastModule ? 12 : 1;

        drawRoundedGradientRect(
                this.categoryComponent.getX(),
                this.categoryComponent.getY() + o,
                this.categoryComponent.getX() + this.categoryComponent.getWidth(),
                this.categoryComponent.getY() + 16 + this.o,
                roundedness,
                this.mod.isEnabled() ? firstColor : 0xE5141414, // Gradient color if enabled, else solid color
                this.mod.isEnabled() ? firstColor : 0xE5141414,
                this.mod.isEnabled() ? secondColor : 0xE5141414, // Additional gradient color if enabled
                this.mod.isEnabled() ? secondColor : 0xE5141414
        );

        // Draw module name with 5px left padding
        GL11.glPushMatrix();
        double button_rgb;

        // Determine button color based on module state
        if (this.mod.isEnabled()) {
            button_rgb = enabledColor; // Set a specific color for enabled state
        } else {
            button_rgb = disabledColor; // Set a specific color for disabled state
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
