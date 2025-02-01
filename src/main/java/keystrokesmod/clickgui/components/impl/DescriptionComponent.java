package keystrokesmod.clickgui.components.impl;

import keystrokesmod.clickgui.components.Component;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class DescriptionComponent extends Component {
    private DescriptionSetting desc;
    private ModuleComponent p;
    private int o;
    private int x;
    private int y;

    public DescriptionComponent(DescriptionSetting desc, ModuleComponent b, int o) {
        this.desc = desc;
        this.p = b;
        this.x = b.categoryComponent.getX() + b.categoryComponent.getWidth();
        this.y = b.categoryComponent.getY() + b.o;
        this.o = o;
    }

    public void render() {
        // Draw background for the description
        RenderUtils.drawRect(
                this.p.categoryComponent.getX() + 4,
                this.p.categoryComponent.getY() + this.o + 11,
                this.p.categoryComponent.getX() + this.p.categoryComponent.getWidth() - 8,
                this.p.categoryComponent.getY() + this.o + 15,
                0x00000000
        );

        // Draw description text
        FontRenderer font = FontManager.helveticaNeue;
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
