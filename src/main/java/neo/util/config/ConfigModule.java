package neo.util.config;

import neo.Neo;
import neo.gui.click.ClickGui;
import neo.module.Module;
import neo.module.impl.client.Settings;
import neo.module.setting.impl.ButtonSetting;
import neo.util.Utils;

public class ConfigModule extends Module {
    private final Config config;
    public boolean saved = true;

    public ConfigModule(Config config, String name, int bind) {
        super(name, category.config, bind);
        this.config = config;
        this.registerSetting(new ButtonSetting("Save config", () -> {
            Utils.sendMessage("&7Saved config: &b" + rawModuleName);
            Neo.configManager.saveConfig(this.config);
            saved = true;
        }));
        this.registerSetting(new ButtonSetting("Remove config", () -> {
            Utils.sendMessage("&7Removed config: &b" + rawModuleName);
            Neo.configManager.deleteConfig(rawModuleName);
        }));
    }

    @Override
    public void toggle() {
        if (mc.currentScreen instanceof ClickGui || mc.currentScreen == null) {
            if (this.config == Neo.currentConfig) {
                return;
            }
            Neo.configManager.loadConfig(rawModuleName);

            Neo.currentConfig = config;

            if (Settings.sendMessage.isToggled()) {
                Utils.sendMessage("&7Enabled config: &b" + rawModuleName);
            }
            saved = true;
        }
    }

    @Override
    public boolean isEnabled() {
        if (Neo.currentConfig == null) {
            return false;
        }
        return Neo.currentConfig.getModule() == this;
    }
}
