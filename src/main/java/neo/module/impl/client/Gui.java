package neo.module.impl.client;

import neo.Neo;
import neo.module.Module;
import neo.module.setting.impl.SliderSetting;
import neo.util.render.Theme;
import neo.util.Utils;

public class Gui extends Module {
    public static SliderSetting theme;

    public Gui() {
        super("Gui", category.client, 54);
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 1));
    }

    public void onEnable() {
        if (Utils.isnull() && mc.currentScreen != Neo.clickGui) {
            mc.displayGuiScreen(Neo.clickGui);
            Neo.clickGui.initMain();
        }

        this.disable();
    }
}
