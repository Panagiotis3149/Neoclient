package keystrokesmod.module.impl.client;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;

public class Gui extends Module {
    public static ButtonSetting removePlayerModel;
    public static ButtonSetting translucentBackground;
    public static SliderSetting theme;

    public Gui() {
        super("Gui", category.client, 54);
        this.registerSetting(removePlayerModel = new ButtonSetting("Remove player model", true));
        this.registerSetting(theme = new SliderSetting("Theme", keystrokesmod.utility.Theme.themes, 1));
        this.registerSetting(translucentBackground = new ButtonSetting("test", false));
    }

    public void onEnable() {
        if (Utils.nullCheck() && mc.currentScreen != Raven.clickGui) {
            mc.displayGuiScreen(Raven.clickGui);
            Raven.clickGui.initMain();
        }

        this.disable();
    }
}
