package neo.module.impl.movement;

import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.impl.combat.OldAura;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.DescriptionSetting;
import neo.module.setting.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class KeepSprint extends Module {
    public static SliderSetting slow;
    public static ButtonSetting disableWhileJump;
    public static ButtonSetting reduceReachHits;

    public KeepSprint() {
        super("KeepSprint", Module.category.movement, 0);
        this.registerSetting(new DescriptionSetting("Default is 40% motion reduction."));
        this.registerSetting(slow = new SliderSetting("Slow %", 40.0D, 0.0D, 40.0D, 1.0D));
        this.registerSetting(disableWhileJump = new ButtonSetting("Disable while jumping", false));
        this.registerSetting(reduceReachHits = new ButtonSetting("Only reduce reach hits", false));
    }

    public static void keepSprint(Entity en) {
        boolean vanilla = false;
        if (disableWhileJump.isToggled() && !mc.thePlayer.onGround) {
            vanilla = true;
        }
        else if (reduceReachHits.isToggled() && !mc.thePlayer.capabilities.isCreativeMode) {
            double n = -1.0;
            final Vec3 getPositionEyes = mc.thePlayer.getPositionEyes(1.0f);
            if (ModuleManager.killAura != null && ModuleManager.killAura.isEnabled() && OldAura.target != null) {
                n = getPositionEyes.distanceTo(OldAura.target.getPositionEyes(1.0f));
            }
            else if (ModuleManager.reach != null && ModuleManager.reach.isEnabled()) {
                n = getPositionEyes.distanceTo(mc.objectMouseOver.hitVec);
            }
            if (n != -1.0 && n <= 3.0) {
                vanilla = true;
            }
        }
        if (vanilla) {
            mc.thePlayer.motionX *= 0.6;
            mc.thePlayer.motionZ *= 0.6;
        } else {
            float n2 = (100.0f - (float) slow.getInput()) / 100.0f;
            mc.thePlayer.motionX *= n2;
            mc.thePlayer.motionZ *= n2;
        }
    }
}
