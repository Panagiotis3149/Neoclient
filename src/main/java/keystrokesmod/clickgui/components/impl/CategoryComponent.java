package keystrokesmod.clickgui.components.impl;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.Component;
import keystrokesmod.module.Module;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Timer;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.profile.Manager;
import keystrokesmod.utility.profile.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static keystrokesmod.utility.Utils.cFL;


public class CategoryComponent {
    public List<ModuleComponent> modules = new CopyOnWriteArrayList<>();
    public Module.category categoryName;
    public boolean opened;
    private int width;
    private int y;
    private int x;
    private int titleHeight;
    public boolean dragging;
    public int xx;
    public int yy;
    public boolean n4m = false;
    public String pvp;
    public boolean pin = false;
    public boolean hovering = false;
    public boolean hoveringOverCategory = false;
    public Timer smoothTimer;
    private Timer textTimer;
    public Timer moduleSmoothTimer;
    public ScaledResolution scale;
    private float big;
    private float bigSettings;
    private final int categoryNameColor = new Color(220, 220, 220).getRGB();
    private float lastHeight;
    public int moduleY;
    private int lastModuleY;
    private int screenHeight;



    public CategoryComponent(Module.category category) {
        this.categoryName = category;
        this.width = 92;
        this.x = 5;
        this.moduleY = this.y = 5;
        this.titleHeight = 13;
        this.smoothTimer = null;
        this.textTimer = null;
        this.xx = 0;
        this.opened = false;
        this.dragging = false;
        int moduleRenderX = this.titleHeight + 3;
        this.scale = new ScaledResolution(Minecraft.getMinecraft());

        for (Iterator var3 = Raven.getModuleManager().inCategory(this.categoryName).iterator(); var3.hasNext(); moduleRenderX += 16) {
            Module mod = (Module) var3.next();
            ModuleComponent b = new ModuleComponent(mod, this, moduleRenderX);
            this.modules.add(b);
        }
    }

    public List<ModuleComponent> getModules() {
        return this.modules;
    }

    public void reloadModules(boolean isProfile) {
        this.modules.clear();
        this.titleHeight = 13;
        int moduleRenderY = this.titleHeight + 3;

        if ((this.categoryName == Module.category.profiles && isProfile) || (this.categoryName == Module.category.scripts && !isProfile)) {
            ModuleComponent manager = new ModuleComponent(isProfile ? new Manager() : new keystrokesmod.script.Manager(), this, moduleRenderY);
            this.modules.add(manager);

            if ((Raven.profileManager == null && isProfile) || (Raven.scriptManager == null && !isProfile)) {
                return;
            }

            if (isProfile) {
                for (Profile profile : Raven.profileManager.profiles) {
                    moduleRenderY += 16;
                    ModuleComponent b = new ModuleComponent(profile.getModule(), this, moduleRenderY);
                    this.modules.add(b);
                }
            } else {
                for (Module module : Raven.scriptManager.scripts.values()) {
                    moduleRenderY += 16;
                    ModuleComponent b = new ModuleComponent(module, this, moduleRenderY);
                    this.modules.add(b);
                }
            }
        }
    }

    public void setX(int n) {
        this.x = n;
    }

    public void setY(int y) {
        this.moduleY = this.y = y;
    }

    public void overTitle(boolean d) {
        this.dragging = d;
    }

    public boolean p() {
        return this.pin;
    }

    public void cv(boolean on) {
        this.pin = on;
    }

    public boolean isOpened() {
        return this.opened;
    }

    public void mouseClicked(boolean on) {
        this.opened = on;
        (this.smoothTimer = new Timer(300)).start();
        (this.textTimer = new Timer(200)).start();
    }

    public void openModule(ModuleComponent component) {
        (this.smoothTimer = new Timer(300)).start();
    }

    public void onScroll(int mouseScrollInput) {
        if (!hoveringOverCategory || !this.opened) {
            return;
        }

        if (mouseScrollInput > 0) {
            this.moduleY += 18;
        } else if (mouseScrollInput < 0) {
            this.moduleY -= 18;
        }

        (moduleSmoothTimer = new Timer(200)).start();
    }

    public void render(FontRenderer renderer) {
        keystrokesmod.utility.font.impl.FontRenderer font = FontManager.googleSansMedium;
        this.moduleY = Math.min(this.moduleY, this.y);
        if (this.moduleY + this.bigSettings < this.y + this.big + this.titleHeight) {
            this.moduleY = (int) (this.y + this.big - this.bigSettings);
        }
        this.width = 92;
        int modulesHeight = 0;
        int settingsHeight = 0;

        if (!this.modules.isEmpty() && this.opened) {
            for (ModuleComponent c : this.modules) {
                settingsHeight += c.getHeight();
                if (modulesHeight > this.screenHeight - 80) {
                    continue;
                }
                if (this.y + this.titleHeight + c.getHeight() + modulesHeight > this.y + this.titleHeight + (16 * this.modules.size())) {
                    modulesHeight += c.getHeight();
                    continue;
                }
                modulesHeight += 16;
            }
            big = modulesHeight;
            bigSettings = settingsHeight;
        }

        float middlePos = (float) (this.x + this.width / 2 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.categoryName.name()) / 2);
        float xPos = opened ? middlePos : this.x + 12;
        float extra = this.y + this.titleHeight + modulesHeight + 4;

        if (smoothTimer != null && System.currentTimeMillis() - smoothTimer.last >= 400) {
            smoothTimer = null;
        }
        if (extra != lastHeight && smoothTimer != null) {
            double diff = lastHeight - extra;
            if (diff < 0) {
                extra = smoothTimer.getValueFloat(lastHeight, this.y + this.titleHeight + modulesHeight + 4, 1);
            } else if (diff > 0) {
                extra = (this.y + this.titleHeight + 4 + big) - smoothTimer.getValueFloat(0, big, 1);
            }
        }
        float namePos = textTimer == null ? xPos : textTimer.getValueFloat(this.x + 12, middlePos, 1);
        if (!this.opened) {
            namePos = textTimer == null ? xPos : middlePos - textTimer.getValueFloat(0, (float) this.width / 2 - (float) font.getStringWidth(cFL(this.categoryName.name())) / 2 - 12, 1);
        }
        lastHeight = extra;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtils.scissor(0, this.y - 2, this.x + this.width + 4, extra - this.y + 4);

        RenderUtils.drawRoundedRectangle(this.x, this.y, this.x + this.width, extra, 12, 0x33202024);

        font.drawString(this.n4m ? this.pvp : cFL(this.categoryName.name()), this.x + this.width / 2 - (font.getStringWidth(categoryName.name()) / 2), (float) (this.y + 4), categoryNameColor, false);
        
        RenderUtils.scissor(0, this.y + this.titleHeight + 3, this.x + this.width + 4, extra - this.y - 4 - this.titleHeight);

        if (!this.n4m) {
            int prevY = this.y;
            this.y = this.moduleY;
            if ((this.opened || smoothTimer != null) && !this.modules.isEmpty()) {
                for (Component c2 : this.modules) {
                    c2.render();
                }
            }
            this.y = prevY;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }



    public void render() {
        int o = this.titleHeight + 3;

        Component component;
        for (Iterator var2 = this.modules.iterator(); var2.hasNext(); o += component.getHeight()) {
            component = (Component) var2.next();
            component.so(o);
        }

    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getModuleY() {
        return this.moduleY;
    }

    public int getWidth() {
        return this.width;
    }

    public void mousePosition(int x, int y) {
        if (this.dragging) {
            this.setX(x - this.xx);
            this.setY(y - this.yy);
        }
        hoveringOverCategory = overCategory(x, y);
        hovering = overTitle(x, y);
    }

    public boolean i(int x, int y) {
        return x >= this.x + 92 - 13 && x <= this.x + this.width && (float) y >= (float) this.y + 2.0F && y <= this.y + this.titleHeight + 1;
    }

    public boolean overTitle(int x, int y) {
        return x >= this.x && x <= this.x + this.width && (float) y >= (float) this.y + 2.0F && y <= this.y + this.titleHeight + 1;
    }

    public boolean overCategory(int x, int y) {
        return x >= this.x - 2 && x <= this.x + this.width + 2 && (float) y >= (float) this.y + 2.0F && y <= this.y + this.titleHeight + big + 1;
    }

    public boolean v(int x, int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.titleHeight;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }
}
