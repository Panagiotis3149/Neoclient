package neo.module.impl.movement;

import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.Utils;

/**
 * @deprecated This class is replaced by {@link BHop}
 */
@Deprecated

public class Speed extends Module {
    public static SliderSetting speed;
    private final ButtonSetting strafeOnly;

    public Speed() {
        super("DEPRECATED", Module.category.movement, 0);
        this.registerSetting(speed = new SliderSetting("Speed", 1.2D, 1.0D, 1.5D, 0.01D));
        this.registerSetting(strafeOnly = new ButtonSetting("Strafe only", false));
    }

    public void onUpdate() {
        double csp = Utils.getHorizontalSpeed();
        if (csp != 0.0D) {
            if (mc.thePlayer.onGround && !mc.thePlayer.capabilities.isFlying) {
                if (!strafeOnly.isToggled() || mc.thePlayer.moveStrafing != 0.0F) {
                    if (mc.thePlayer.hurtTime != mc.thePlayer.maxHurtTime || mc.thePlayer.maxHurtTime <= 0) {
                        if (!Utils.jumpDown()) {
                            double val = speed.getInput() - (speed.getInput() - 1.0D) * 0.5D;
                            Utils.ss(csp * val, true);
                        }
                    }
                }
            }
        }
    }
}
