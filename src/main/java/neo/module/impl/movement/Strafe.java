package neo.module.impl.movement;

import neo.module.Module;
import neo.module.setting.impl.SliderSetting;
import neo.util.player.move.MoveUtil;
import neo.util.Utils;

public class Strafe extends Module {
    public static String[] modes = new String[]{"Default", "Slight", "SFT"};
    public static SliderSetting mode;


    public Strafe() {
        super("Strafe", Module.category.movement, 0);
        registerSetting(mode = new SliderSetting("Mode", modes, 0));
    }

    public void onUpdate() {
        switch ((int) mode.getInput()) {
            case 0:
                MoveUtil.partialStrafePercent(100);
            break;
            case 1:
                MoveUtil.partialStrafePercent(25);
                break;
            case 2:
                if (mc.thePlayer.motionY < 0 && Utils.getFallDistance(mc.thePlayer) < 3) {
                    Utils.getTimer().timerSpeed = 0.9F;
                }
                MoveUtil.partialStrafePercent(100);
                break;
        }

    }

    public void onDisable() {
        Utils.resetTimer();
    }
}
