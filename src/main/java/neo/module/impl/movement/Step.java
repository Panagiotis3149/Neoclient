package neo.module.impl.movement;


import neo.module.Module;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;

public class Step extends Module {
    private final SliderSetting mode;
    public static String[] modes = new String[]{"Vanilla1.5", "BMC", "Mospixel"};
    private boolean wasCollided;
    private int offGroundTicks;

    public Step() {
        super("Step", Module.category.movement, 0);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
    }

    @Override
    public void onUpdate() {
        mc.thePlayer.stepHeight = 0.5F;

        if(mc.thePlayer.onGround) {
            wasCollided = false;
        }

        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        switch ((int) mode.getInput()) {
            case 0:
            mc.thePlayer.stepHeight = 1.5F;
            break;
            case 1:
                if(mc.thePlayer.isCollidedHorizontally) {
                    if(mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.42F;
                    }
                    wasCollided = true;
                }
                if (offGroundTicks == 4 && wasCollided) {
                    mc.thePlayer.motionY = -0.09800000190734863;
                }
            break;
            case 2:
                if (mc.thePlayer.isCollidedHorizontally) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.4F;
                    }
                    wasCollided = true;
                    Utils.getTimer().timerSpeed = 1.5f;
                } else {
                    Utils.resetTimer();
                }
                if (offGroundTicks == 4 && wasCollided) {
                    mc.thePlayer.motionY = -0.09800000190734863;
                }
                break;
        }

    }

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.5F;
    }


}
