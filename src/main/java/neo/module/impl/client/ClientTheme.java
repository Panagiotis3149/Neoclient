package neo.module.impl.client;

import neo.module.Module;
import neo.module.impl.player.Scaffold;
import neo.module.impl.render.*;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.render.Theme;


public class ClientTheme extends Module {
    public static SliderSetting theme;

    public ClientTheme() {
        super("ClientTheme", category.client, 0);
        this.registerSetting(new DescriptionSetting("Set all the themes at once."));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
    }


    @Override
    public void onEnable() {
            Gui.theme.setValueRaw(theme.getInput());
            Scaffold.theme.setValueRaw(theme.getInput());
            TargetESP.theme.setValueRaw(theme.getInput());
            TargetHUD.theme.setValueRaw(theme.getInput());
            HUD.theme.setValueRaw(theme.getInput());
            Watermark.theme.setValueRaw(theme.getInput());
            BedESP.theme.setValueRaw(theme.getInput());
            ChestESP.theme.setValueRaw(theme.getInput());
            Notifications.theme.setValueRaw(theme.getInput());
            this.disable();
    }
}
