package neo.script;

import neo.Neo;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.util.Utils;
import org.lwjgl.Sys;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class Manager extends Module {
    private long lastLoad;
    public final String documentationURL = "https://blowsy.gitbook.io/raven";
    public Manager() {
        super("Manager", category.scripts);
        this.registerSetting(new ButtonSetting("Load scripts", () -> {
            if (Neo.scriptManager.compiler == null) {
                Utils.sendMessage("&cCompiler ewwor, JDK not found");
            }
            else {
                final long currentTimeMillis = System.currentTimeMillis();
                if (Utils.getDifference(this.lastLoad, currentTimeMillis) > 1500) {
                    this.lastLoad = currentTimeMillis;
                    Neo.scriptManager.loadScripts();
                    if (Neo.scriptManager.scripts.isEmpty()) {
                        Utils.sendMessage("&7No scripts found.");
                    }
                    else {
                        Utils.sendMessage("&7Loaded &b" + Neo.scriptManager.scripts.size() + " &7script" + ((Neo.scriptManager.scripts.size() == 1) ? "." : "s."));
                    }
                }
                else {
                    Utils.sendMessage("&cYou are on cooldown.");
                }
            }
        }));
        this.registerSetting(new ButtonSetting("Open folder", () -> {
            try {
                Desktop.getDesktop().open(Neo.scriptManager.directory);
            }
            catch (IOException ex) {
                Neo.scriptManager.directory.mkdirs();
                Utils.sendMessage("&cError locating folder, recreated.");
            }
        }));
        this.registerSetting(new ButtonSetting("View documentation", () -> {
            try {
                Desktop.getDesktop().browse(new URI(documentationURL));
            } catch (Throwable t) {
                Sys.openURL(documentationURL);
            }
        }));
        this.canBeEnabled = false;
        this.ignoreOnSave = true;
    }
}
