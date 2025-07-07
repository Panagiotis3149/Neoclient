package neo.gui.click;

import neo.Neo;
import neo.gui.click.components.Component;
import neo.gui.click.components.impl.BindComponent;
import neo.gui.click.components.impl.CategoryComponent;
import neo.gui.click.components.impl.ModuleComponent;
import neo.module.Module;
import neo.module.impl.client.Gui;
import neo.util.render.Theme;
import neo.util.render.animation.Timer;
import neo.util.render.ClickCircle;
import neo.util.shader.BlurUtils;
import neo.util.shader.RoundedUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ClickGui extends GuiScreen {
    private ScheduledFuture sf;
    private Timer aT;
    private Timer aL;
    private Timer aE;
    private Timer aR;
    private Timer blurSmooth;
    private ScaledResolution sr;
    public static ArrayList<CategoryComponent> categories;
    private static final java.util.List<ClickCircle> circleClicks = new ArrayList<>();

    public ClickGui() {
        categories = new ArrayList();
        int x = 5;
        for (Module.category c : Module.category.values()) {
            CategoryComponent categoryComponent = new CategoryComponent(c);
            categoryComponent.setX(x);
            categoryComponent.setY(5);
            categories.add(categoryComponent);
            x += categoryComponent.getWidth() + 5;
        }


    }

    public void initMain() {
        (this.aT = this.aE = this.aR = this.blurSmooth = new Timer(500.0F)).start();
        this.sf = Neo.getExecutor().schedule(() -> {
            (this.aL = new Timer(650.0F)).start();
        }, 650L, TimeUnit.MILLISECONDS);
        for (CategoryComponent categoryComponent : categories) {
            categoryComponent.setScreenHeight(this.height);
        }
    }

    public void initGui() {
        super.initGui();
        this.sr = new ScaledResolution(this.mc);
    }

    public void drawScreen(int x, int y, float p) {

        BlurUtils.prepareBlur();
        RoundedUtils.drawRound(0, 0, this.width, this.height, 0.0f, Color.black);
        float inputToRange = (float) (3 * (76 + 35) / 100);
        BlurUtils.blurEnd(2, this.blurSmooth.getValueFloat(0, inputToRange, 1));

        drawRect(0, 0, this.width, this.height, (int) (this.aR.getValueFloat(0.0F, 0.7F, 2) * 255.0F) << 24);
        int r;

        int color = Theme.getGradient(Gui.theme.getInput(), 0.0);
        circleClicks.removeIf(ClickCircle::isDone);
        for (ClickCircle clickCircle : circleClicks) {
            clickCircle.drawScreen(color);
        }


        for (CategoryComponent c : categories) {
            c.render(this.fontRendererObj);
            c.mousePosition(x, y);

            for (Component m : c.getModules()) {
                m.drawScreen(x, y);
            }
        }

        GL11.glColor3f(1.0f, 1.0f, 1.0f);
    }

    public void mouseClicked(int x, int y, int m) throws IOException {
        Iterator var4 = categories.iterator();

        circleClicks.removeIf(ClickCircle::isDone);
        ClickCircle clickCircle = new ClickCircle(x, y);
        circleClicks.add(clickCircle);



        while (true) {
            CategoryComponent category;
            do {
                do {
                    if (!var4.hasNext()) {
                        return;
                    }

                    category = (CategoryComponent) var4.next();
                    if (category.v(x, y) && !category.i(x, y) && m == 0) {
                        category.overTitle(true);
                        category.xx = x - category.getX();
                        category.yy = y - category.getY();
                    }

                    if (category.overTitle(x, y) && m == 1) {
                        category.mouseClicked(!category.isOpened());
                    }

                    if (category.i(x, y) && m == 0) {
                        category.cv(!category.p());
                    }
                } while (!category.isOpened());
            } while (category.getModules().isEmpty());

            for (Component c : category.getModules()) {
                if (c.onClick(x, y, m) && c instanceof ModuleComponent) {
                    category.openModule((ModuleComponent) c);
                }
            }

        }
    }

    public void mouseReleased(int x, int y, int s) {
        if (s == 0) {
            Iterator<CategoryComponent> iterator = categories.iterator();
            while (iterator.hasNext()) {
                CategoryComponent category = iterator.next();
                category.overTitle(false);
                if (category.isOpened() && !category.getModules().isEmpty()) {
                    for (Component module : category.getModules()) {
                        module.mouseReleased(x, y, s);
                    }
                }
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheelInput = Mouse.getDWheel();
        if (wheelInput != 0) {
            for (CategoryComponent category : categories) {
                category.onScroll(wheelInput);
            }
        }
    }


    @Override
    public void keyTyped(char t, int k) {
        if (k == Keyboard.KEY_ESCAPE && !binding()) {
            this.mc.displayGuiScreen(null);
        } else {
            Iterator<CategoryComponent> iterator = categories.iterator();
            while (iterator.hasNext()) {
                CategoryComponent category = iterator.next();

                if (category.isOpened() && !category.getModules().isEmpty()) {
                    for (Component module : category.getModules()) {
                        module.keyTyped(t, k);
                    }
                }
            }
        }
    }

    public void actionPerformed(GuiButton b) {

    }

    public void onGuiClosed() {
        this.aL = null;
        if (this.sf != null) {
            this.sf.cancel(true);
            this.sf = null;
        }
        for (CategoryComponent c : categories) {
            c.dragging = false;
            for (Component m : c.getModules()) {
                m.onGuiClosed();
            }
        }
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    private boolean binding() {
        for (CategoryComponent c : categories) {
            for (ModuleComponent m : c.getModules()) {
                for (Component component : m.settings) {
                    if (component instanceof BindComponent && ((BindComponent) component).isBinding) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
