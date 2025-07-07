package neo.module.impl.movement;

import neo.gui.click.ClickGui;
import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;

public class Timer extends Module {
    private final SliderSetting speed;
    private final ButtonSetting strafeOnly;

    public Timer() {
        super("Timer", Module.category.movement, 0);
        this.registerSetting(speed = new SliderSetting("Speed", 1.0D, 0.5D, 2.5D, 0.01D));
        this.registerSetting(strafeOnly = new ButtonSetting("Strafe only", false));
    }

    public void onUpdate() {
        if (!(mc.currentScreen instanceof ClickGui)) {
            if (strafeOnly.isToggled() && mc.thePlayer.moveStrafing == 0) {
                Utils.resetTimer();
                return;
            }
            Utils.getTimer().timerSpeed = (float) speed.getInput();
        }
        else {
            Utils.resetTimer();
        }

    }

    public void onDisable() {
        Utils.resetTimer();
    }
}
