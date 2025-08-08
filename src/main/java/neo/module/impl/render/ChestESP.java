package neo.module.impl.render;

import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.render.RenderUtils;
import neo.util.Utils;
import neo.util.render.Theme;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChestESP extends Module {

    private final ButtonSetting outline;
    private final ButtonSetting shade;
    private final ButtonSetting disableIfOpened;
    public static SliderSetting theme;

    public ChestESP() {
        super("ChestESP", Module.category.render, 0);
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
        this.registerSetting(outline = new ButtonSetting("Outline", false));
        this.registerSetting(shade = new ButtonSetting("Shade", false));
        this.registerSetting(disableIfOpened = new ButtonSetting("Disable if opened", false));
    }

    @SubscribeEvent
    public void o(RenderWorldLastEvent ev) {
        if (!Utils.isnull()) {
            return;
        }
        for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
            if (tileEntity instanceof TileEntityChest) {
                if (disableIfOpened.isToggled() && ((TileEntityChest) tileEntity).lidAngle > 0.0f) {
                    continue;
                }
                RenderUtils.renderBlock(tileEntity.getPos(), Theme.getGradient(theme.getInput(), 0), outline.isToggled(), shade.isToggled());
            } else {
                if (!(tileEntity instanceof TileEntityEnderChest)) {
                    continue;
                }
                if (disableIfOpened.isToggled() && ((TileEntityEnderChest) tileEntity).lidAngle > 0.0f) {
                    continue;
                }
                RenderUtils.renderBlock(tileEntity.getPos(), Theme.getGradient(theme.getInput(), 0), outline.isToggled(), shade.isToggled());
            }
        }
    }
}
