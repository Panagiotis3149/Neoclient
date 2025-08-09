package neo.module.impl.client;

import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import org.jetbrains.annotations.NotNull;


public class Settings extends Module {
    public static ButtonSetting weaponAxe;
    public static ButtonSetting weaponRod;
    public static ButtonSetting weaponStick;
    public static ButtonSetting middleClickFriends;
    public static ButtonSetting rotateBody;
    public static ButtonSetting fullBody;
    public static ButtonSetting movementFix;
    public static SliderSetting randomYawFactor;
    public static SliderSetting offset;
    public static SliderSetting timeMultiplier;
    public static SliderSetting toggleSound;
    public static ButtonSetting sendMessage;
    public static ButtonSetting spacednames;
    public static ButtonSetting specialnames;

    public Settings() {
        super("Settings", category.client, 0);
        this.registerSetting(new DescriptionSetting("General"));
        this.registerSetting(toggleSound = new SliderSetting("Toggle sound", new String[]{"None", "Rise", "Sigma", "QuickMacro", "Shein"}, 1));
        this.registerSetting(weaponAxe = new ButtonSetting("Set axe as weapon", false));
        this.registerSetting(weaponRod = new ButtonSetting("Set rod as weapon", false));
        this.registerSetting(weaponStick = new ButtonSetting("Set stick as weapon", false));
        this.registerSetting(middleClickFriends = new ButtonSetting("Middle click friends", false));
        this.registerSetting(new DescriptionSetting("Rotations"));
        this.registerSetting(rotateBody = new ButtonSetting("Rotate body", true));
        this.registerSetting(fullBody = new ButtonSetting("Full body", false));
        this.registerSetting(movementFix = new ButtonSetting("Movement fix", false));
        this.registerSetting(randomYawFactor = new SliderSetting("Random yaw factor", 1.0, 0.0, 10.0, 1.0));
        this.registerSetting(new DescriptionSetting("Profiles"));
        this.registerSetting(sendMessage = new ButtonSetting("Send message on enable", true));
        this.registerSetting(new DescriptionSetting("Theme"));
        this.registerSetting(offset = new SliderSetting("Offset", 0.5, -3.0, 3.0, 0.1));
        this.registerSetting(timeMultiplier = new SliderSetting("Time multiplier", 0.5, 0.1, 4.0, 0.1));
        this.registerSetting(new DescriptionSetting("Module Naming"));
        this.registerSetting(spacednames = new ButtonSetting("Spaced Names", false));
        this.registerSetting(specialnames = new ButtonSetting("Special Naming", false));
        this.canBeEnabled = false;

        }

    public static @NotNull String getToggleSound(boolean enable) {
        final String startSuffix = "neo:toggle.";
        final String endSuffix = enable ? ".enable" : ".disable";

        final String middleSuffix;
        switch ((int) toggleSound.getInput()) {
            default:
            case 0:
                return "";
            case 1:
                middleSuffix = "rise";
                break;
            case 2:
                middleSuffix = "sigma";
                break;
            case 3:
                middleSuffix = "quickmacro";
                break;
            case 4:
                middleSuffix = "shein";
                break;
        }
        return startSuffix + middleSuffix + endSuffix;
    }
}
