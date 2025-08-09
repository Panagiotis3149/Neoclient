package neo.module;

import neo.Neo;
import neo.module.impl.client.Settings;
import neo.module.impl.client.Gui;
import neo.module.impl.client.Notifications;
import neo.module.setting.Setting;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.script.Script;
import neo.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Module {
    protected ArrayList<Setting> settings;
    public String moduleName;
    private final Module.category moduleCategory;
    public String rawModuleName;
    private boolean enabled;
    private int keycode;
    protected static Minecraft mc;
    private boolean isToggled = false;
    public boolean canBeEnabled = true;
    public boolean ignoreOnSave = false;
    public boolean hidden = false;
    public Script script = null;

    public Module(String moduleName, Module.category moduleCategory, int keycode) {
        this.rawModuleName = moduleName;
        this.moduleName = moduleName;
        this.moduleCategory = moduleCategory;
        this.keycode = keycode;
        this.enabled = false;
        mc = Minecraft.getMinecraft();
        this.settings = new ArrayList();
    }

    public static Module getModule(Class<? extends Module> a) {
        Iterator var1 = ModuleManager.modules.iterator();

        Module module;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            module = (Module) var1.next();
        } while (module.getClass() != a);

        return module;
    }

    public Module(String name, Module.category moduleCategory) {
        this.rawModuleName = name;
        this.moduleName = name;
        this.moduleCategory = moduleCategory;
        this.keycode = 0;
        this.enabled = false;
        mc = Minecraft.getMinecraft();
        this.settings = new ArrayList();

    }

    public Module(Script script) {
        super();
        this.enabled = false;
        this.rawModuleName = script.name;
        this.moduleName = script.name;
        this.script = script;
        this.keycode = 0;
        this.moduleCategory = category.scripts;
        this.settings = new ArrayList<>();
    }

    public void keybind() {
        if (this.keycode != 0) {
            try {
                boolean isPressed = false;
                if (this.keycode >= 1000) {
                    int mouseButton = this.keycode - 1000;
                    if (mouseButton >= 0 && mouseButton < 10) {
                        isPressed = Mouse.isButtonDown(mouseButton);
                    } else {
                        this.keycode = 0;
                        return;
                    }
                } else if (this.keycode >= 0 && this.keycode <= 255) {
                    isPressed = Keyboard.isKeyDown(this.keycode);
                } else {
                    this.keycode = 0;
                    return;
                }

                if (!this.isToggled && isPressed) {
                    this.toggle();
                    this.isToggled = true;
                } else if (!isPressed) {
                    this.isToggled = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Utils.sendMessage("&cFailed to check keybinding. Setting to none");
                Utils.sendMessage("Debug: " + e.getMessage());
               // Utils.sendRawMessage("DEBUG: " + new java.io.StringWriter() {{ e.printStackTrace(new java.io.PrintWriter(this)); }}.toString());
                this.keycode = 0;
            }
        }
    }


    public boolean canBeEnabled() {
        if (this.script != null && script.error) {
            return false;
        }
        return this.canBeEnabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void enable() {
        if (!this.canBeEnabled() || this.isEnabled()) {
            return;
        }
        this.setEnabled(true);
            ModuleManager.organizedModules.add(this);
        if (ModuleManager.hud.isEnabled()) {
            ModuleManager.sort();
        }
        //
        if (this.script != null) {
            Neo.scriptManager.onEnable(script);
        }
        else {
            FMLCommonHandler.instance().bus().register(this);
            this.onEnable();
        }
    }

    public void disable() {
        if (!this.isEnabled()) {
            return;
        }
        this.setEnabled(false);
      //
        ModuleManager.organizedModules.remove(this);
        if (this.script != null) {
            Neo.scriptManager.onDisable(script);
        }
        else {
            FMLCommonHandler.instance().bus().unregister(this);
            this.onDisable();
        }
    }

    // Very Fucking Smart™
    public String getInfo() {
        try {
            SliderSetting modeSlider = null;
            String[] modeStrings = null;

            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName().toLowerCase();

                if (name.contains("mode")) {
                    if (SliderSetting.class.isAssignableFrom(field.getType())) {
                        modeSlider = (SliderSetting) field.get(this);
                    } else if (field.getType().isArray() && field.getType().getComponentType() == String.class) {
                        modeStrings = (String[]) field.get(this);
                    }
                }
            }

            if (modeSlider != null && modeStrings != null) {
                int index = (int) modeSlider.getInput();
                if (index >= 0 && index < modeStrings.length) {
                    return modeStrings[index];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        if (this.script != null || this.moduleCategory == category.config) {
            return this.moduleName;
        }
        return Neo.i18n.get(this.moduleName);
    }

    public void updateName() {
        final String last = moduleName;
        if (!(this.script != null || this.moduleCategory == category.config)) {
            moduleName = Neo.i18n.get(last);
        }
    }

    public ArrayList<Setting> getSettings() {
        return this.settings;
    }

    public void registerSetting(Setting Setting) {
        this.settings.add(Setting);
    }

    public void registerSetting(Setting... settings) {
        Collections.addAll(this.settings, settings);
    }

    public Module.category moduleCategory() {
        return this.moduleCategory;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void toggle() {
        if (this.isEnabled()) {
            this.disable();
            if (Settings.toggleSound.getInput() != 0) mc.thePlayer.playSound(Settings.getToggleSound(false), 1, 1);
            if (Notifications.moduleToggled.isToggled() && !(this instanceof Gui))
                Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Disabled " + this.moduleName);
        } else {
            this.enable();
            if (Settings.toggleSound.getInput() != 0) mc.thePlayer.playSound(Settings.getToggleSound(true), 1, 1);
            if (Notifications.moduleToggled.isToggled() && !(this instanceof Gui))
                Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Enabled " + this.moduleName);
        }

    }

    public void onUpdate() {
    }

    public void guiUpdate() {
    }

    public void onToggleSetting(ButtonSetting b) {
    }

    public int getKeycode() {
        return this.keycode;
    }

    public void setBind(int keybind) {
        this.keycode = keybind;
    }

    public enum category {
        combat,
        movement,
        player,
        render,
        minigames,
        other,
        client,
        config,
        scripts
    }
}
