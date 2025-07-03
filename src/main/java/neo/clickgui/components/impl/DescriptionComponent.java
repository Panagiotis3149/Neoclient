package neo.clickgui.components.impl;

import neo.clickgui.components.Component;
import neo.module.setting.impl.DescriptionSetting;
import neo.util.font.MinecraftFontRenderer;
import neo.util.render.RenderUtils;
import neo.util.render.Theme;
import neo.util.font.FontManager;
import neo.util.font.impl.FontRenderer;
import org.lwjgl.opengl.GL11;

public class DescriptionComponent extends Component {
    public final DescriptionSetting desc;
    private final ModuleComponent p;
    private int o;
    private final int x;
    private final int y;

    public DescriptionComponent(DescriptionSetting desc, ModuleComponent b, int o) {
        this.desc = desc;
        this.p = b;
        this.x = b.categoryComponent.getX() + b.categoryComponent.getWidth();
        this.y = b.categoryComponent.getY() + b.o;
        this.o = o;
    }

    public void render() {
        if (!desc.isVisible()) return;
        /*
        RenderUtils.drawRect(
                this.p.categoryComponent.getX() + 4,
                this.p.categoryComponent.getY() + this.o + 11,
                this.p.categoryComponent.getX() + this.p.categoryComponent.getWidth() - 8,
                this.p.categoryComponent.getY() + this.o + 15,
                0xBF1C1C1C
        );
       */

        // Draw description text
        FontRenderer font = FontManager.productSans20;
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        font.drawString(
                this.desc.getDesc(),
                (float) ((this.p.categoryComponent.getX() + 4) * 2),
                (float) ((this.p.categoryComponent.getY() + this.o + 4) * 2),
                Theme.getGradient(Theme.descriptor[0], Theme.descriptor[1], 0),
                false
        );
        GL11.glPopMatrix();
    }

    public void so(int n) {
        this.o = n;
    }
}
