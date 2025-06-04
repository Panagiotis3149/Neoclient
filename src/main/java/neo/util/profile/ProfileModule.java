package neo.util.profile;

import neo.Neo;
import neo.clickgui.ClickGui;
import neo.module.Module;
import neo.module.impl.client.Settings;
import neo.module.setting.impl.ButtonSetting;
import neo.util.Utils;

public class ProfileModule extends Module {
    private final Profile profile;
    public boolean saved = true;

    public ProfileModule(Profile profile, String name, int bind) {
        super(name, category.profiles, bind);
        this.profile = profile;
        this.registerSetting(new ButtonSetting("Save profile", () -> {
            Utils.sendMessage("&7Saved profile: &b" + getName());
            Neo.profileManager.saveProfile(this.profile);
            saved = true;
        }));
        this.registerSetting(new ButtonSetting("Remove profile", () -> {
            Utils.sendMessage("&7Removed profile: &b" + getName());
            Neo.profileManager.deleteProfile(getName());
        }));
    }

    @Override
    public void toggle() {
        if (mc.currentScreen instanceof ClickGui || mc.currentScreen == null) {
            if (this.profile == Neo.currentProfile) {
                return;
            }
            Neo.profileManager.loadProfile(this.getName());

            Neo.currentProfile = profile;

            if (Settings.sendMessage.isToggled()) {
                Utils.sendMessage("&7Enabled profile: &b" + this.getName());
            }
            saved = true;
        }
    }

    @Override
    public boolean isEnabled() {
        if (Neo.currentProfile == null) {
            return false;
        }
        return Neo.currentProfile.getModule() == this;
    }
}
