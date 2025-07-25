package neo.util.config;

import com.google.gson.*;
import neo.Neo;
import neo.gui.click.ClickGui;
import neo.gui.click.components.impl.CategoryComponent;
import neo.module.Module;
import neo.module.impl.render.BPSCounter;
import neo.module.impl.render.FPSCounter;
import neo.module.impl.render.HUD;
import neo.module.setting.Setting;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.script.Manager;
import neo.util.Utils;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigManager {
    public static Minecraft mc = Minecraft.getMinecraft();
    public File directory;
    public List<Config> configs = new ArrayList<>();

    public ConfigManager() {
        directory = new File(Neo.NeoDirectory, "conf");
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                System.out.println("There was an issue creating configs directory.");
                return;
            }
        }
        if (directory.listFiles().length == 0) { // if there's no config in the folder upon launch, create new default config
            saveConfig(new Config("default", 0));
        }
    }

    public void saveConfig(Config config) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("keybind", config.getModule().getKeycode());
        JsonArray jsonArray = new JsonArray();
        for (Module module : Neo.moduleManager.getModules()) {
            if (module.ignoreOnSave) {
                continue;
            }
            JsonObject moduleInformation = getJsonObject(module);
            jsonArray.add(moduleInformation);
        }
        if (Neo.scriptManager != null && Neo.scriptManager.scripts != null) {
            for (Module module : Neo.scriptManager.scripts.values()) {
                if (module.ignoreOnSave) {
                    continue;
                }
                JsonObject moduleInformation = getJsonObject(module);
                jsonArray.add(moduleInformation);
            }
        }
        jsonObject.add("modules", jsonArray);
        try (FileWriter fileWriter = new FileWriter(new File(directory, config.getName() + ".json"))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
        } catch (Exception e) {
            failedMessage("save", config.getName());
            e.printStackTrace();
        }
    }

    private static JsonObject getJsonObject(Module module) {
        JsonObject moduleInformation = new JsonObject();
        moduleInformation.addProperty("name", (module.moduleCategory() == Module.category.scripts && !(module instanceof Manager)) ? "sc-" + module.getName() : module.getName());
        if (module.canBeEnabled) {
            moduleInformation.addProperty("enabled", module.isEnabled());
            moduleInformation.addProperty("hidden", module.isHidden());
            moduleInformation.addProperty("keybind", module.getKeycode());
            }
            if (module instanceof HUD) {
            moduleInformation.addProperty("posX", HUD.hudX);
            moduleInformation.addProperty("posY", HUD.hudY);
        }
        if (module instanceof FPSCounter) {
            moduleInformation.addProperty("posX", FPSCounter.X);
            moduleInformation.addProperty("posY", FPSCounter.Y);
        }
        if (module instanceof BPSCounter) {
            moduleInformation.addProperty("posX", BPSCounter.X);
            moduleInformation.addProperty("posY", BPSCounter.Y);
        }
        for (Setting setting : module.getSettings()) {
            if (setting instanceof ButtonSetting && !((ButtonSetting) setting).isMethodButton) {
                moduleInformation.addProperty(setting.getName(), ((ButtonSetting) setting).isToggled());
            } else if (setting instanceof SliderSetting) {
                moduleInformation.addProperty(setting.getName(), ((SliderSetting) setting).getInput());
            }
        }
        return moduleInformation;
    }

    public void loadConfig(String name) {
        for (File file : getConfigFiles()) {
            if (!file.exists()) {
                failedMessage("load", name);
                System.out.println("Failed to load " + name);
                return;
            }
            if (!file.getName().equals(name + ".json")) {
                continue;
            }
            if (Neo.scriptManager != null) {
                for (Module module : Neo.scriptManager.scripts.values()) {
                    if (module.canBeEnabled()) {
                        module.disable();
                        module.setBind(0);
                    }
                }
            }
            for (Module module : Neo.getModuleManager().getModules()) {
                if (module.canBeEnabled()) {
                    module.disable();
                    module.setBind(0);
                }
            }
            try (FileReader fileReader = new FileReader(file)) {
                JsonParser jsonParser = new JsonParser();
                JsonObject configJson = jsonParser.parse(fileReader).getAsJsonObject();
                if (configJson == null) {
                    failedMessage("load", name);
                    return;
                }
                JsonArray modules = configJson.getAsJsonArray("modules");
                if (modules == null) {
                    failedMessage("load", name);
                    return;
                }
                for (JsonElement moduleJson : modules) {
                    JsonObject moduleInformation = moduleJson.getAsJsonObject();
                    String moduleName = moduleInformation.get("name").getAsString();

                    if (moduleName == null || moduleName.isEmpty()) {
                        continue;
                    }

                    Module module = Neo.moduleManager.getModule(moduleName);
                    if (module == null && moduleName.startsWith("sc-") && Neo.scriptManager != null) {
                        for (Module module1 : Neo.scriptManager.scripts.values()) {
                            if (module1.getName().equals(moduleName.substring(3))) {
                                module = module1;
                            }
                        }
                    }

                    if (module == null) {
                        continue;
                    }

                    if (module.canBeEnabled()) {
                        if (moduleInformation.has("enabled")) {
                            boolean enabled = moduleInformation.get("enabled").getAsBoolean();
                            if (enabled) {
                                module.enable();
                            } else {
                                module.disable();
                            }
                        }
                        if (moduleInformation.has("hidden")) {
                            boolean hidden = moduleInformation.get("hidden").getAsBoolean();
                            module.setHidden(hidden);
                        }
                        if (moduleInformation.has("keybind")) {
                            int keybind = moduleInformation.get("keybind").getAsInt();
                            module.setBind(keybind);
                        }
                    }

                    if (module.getName().equals("Arraylist")) { // fixed 28/06/25
                        if (moduleInformation.has("posX")) {
                            int hudX = moduleInformation.get("posX").getAsInt();
                            HUD.hudX = hudX;
                        }
                        if (moduleInformation.has("posY")) {
                            int hudY = moduleInformation.get("posY").getAsInt();
                            HUD.hudY = hudY;
                        }
                    }
                    if (module.getName().equals("FPSCounter")) {
                        if (moduleInformation.has("posX")) {
                            int hudX = moduleInformation.get("posX").getAsInt();
                            FPSCounter.X = hudX;
                        }
                        if (moduleInformation.has("posY")) {
                            int hudY = moduleInformation.get("posY").getAsInt();
                            FPSCounter.Y = hudY;
                        }
                    }
                    if (module.getName().equals("BPSCounter")) {
                        if (moduleInformation.has("posX")) {
                            int hudX = moduleInformation.get("posX").getAsInt();
                            BPSCounter.X = hudX;
                        }
                        if (moduleInformation.has("posY")) {
                            int hudY = moduleInformation.get("posY").getAsInt();
                            BPSCounter.Y = hudY;
                        }
                    }

                    for (Setting setting : module.getSettings()) {
                        setting.loadConfig(moduleInformation);
                    }

                    Neo.currentConfig = getConfig(name);
                }
            } catch (Exception e) {
                failedMessage("load", name);
                e.printStackTrace();
            }
        }
    }

    public void deleteConfig(String name) {
        Iterator<Config> iterator = configs.iterator();
        while (iterator.hasNext()) {
            Config config = iterator.next();
            if (config.getName().equals(name)) {
                iterator.remove();
            }
        }
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.getName().equals(name + ".json")) {
                    file.delete();
                }
            }
        }
    }

    public void loadConfigs() {
        configs.clear();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                try (FileReader fileReader = new FileReader(file)) {
                    JsonParser jsonParser = new JsonParser();
                    JsonObject configJson = jsonParser.parse(fileReader).getAsJsonObject();
                    String configName = file.getName().replace(".json", "");

                    if (configJson == null) {
                        failedMessage("load", configName);
                        return;
                    }

                    int keybind = 0;

                    if (configJson.has("keybind")) {
                        keybind = configJson.get("keybind").getAsInt();
                    }

                    Config config = new Config(configName, keybind);
                    configs.add(config);
                } catch (Exception e) {
                    Utils.sendMessage("&cFailed to load configs.");
                    e.printStackTrace();
                }
            }

            for (CategoryComponent categoryComponent : ClickGui.categories) {
                if (categoryComponent.categoryName == Module.category.config) {
                    categoryComponent.reloadModules(true);
                }
            }
            Utils.sendMessage("&b" + Neo.configManager.getConfigFiles().size() + " &7configs loaded.");
        }
    }

    public List<File> getConfigFiles() {
        List<File> configFiles = new ArrayList<>();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (!file.getName().endsWith(".json")) {
                    continue;
                }
                configFiles.add(file);
            }
        }
        return configFiles;
    }

    public Config getConfig(String name) {
        for (Config config : configs) {
            if (config.getName().equals(name)) {
                return config;
            }
        }
        return null;
    }

    public void failedMessage(String reason, String name) {
        Utils.sendMessage("&cFailed to " + reason + ": &b" + name);
    }
}
