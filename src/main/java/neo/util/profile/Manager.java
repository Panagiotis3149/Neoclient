package neo.util.profile;

import neo.Neo;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.util.Utils;

import java.awt.*;
import java.io.IOException;

public class Manager extends Module {
    private final ButtonSetting loadProfiles;
    private final ButtonSetting openFolder;
    private final ButtonSetting createProfile;

    public Manager() {
        super("Manager", category.profiles);
        this.registerSetting(createProfile = new ButtonSetting("Create config", () -> {
            if (Utils.nullCheck() && Neo.profileManager != null) {
                String name = "cfg-";
                for (int i = 1; i <= 100; i++) {
                    if (Neo.profileManager.getProfile(name + i) != null) {
                        continue;
                    }
                    name += i;
                    Neo.profileManager.saveProfile(new Profile(name, 0));
                    Utils.sendMessage("&7Created config: &b" + name);
                    Neo.profileManager.loadProfiles();
                    break;
                }
            }
        }));
        this.registerSetting(loadProfiles = new ButtonSetting("Load configs", () -> {
            if (Utils.nullCheck() && Neo.profileManager != null) {
                Neo.profileManager.loadProfiles();
            }
        }));
        this.registerSetting(openFolder = new ButtonSetting("Open folder", () -> {
            try {
                Desktop.getDesktop().open(Neo.profileManager.directory);
            }
            catch (IOException ex) {
                Neo.profileManager.directory.mkdirs();
                Utils.sendMessage("&cError locating folder, recreated.");
            }
        }));
        ignoreOnSave = true;
        canBeEnabled = false;
    }
}