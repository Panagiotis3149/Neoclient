package neo.module.impl.combat;

import neo.module.Module;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.util.player.move.RotationUtils;
import neo.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public class AimAssist extends Module {
    private final SliderSetting speed;
    private final SliderSetting fov;
    private final SliderSetting distance;
    private final ButtonSetting clickAim;
    private final ButtonSetting weaponOnly;
    private final ButtonSetting aimInvis;
    private final ButtonSetting blatantMode;
    private final ButtonSetting ignoreTeammates;


    public AimAssist() {
        super("AimAssist", category.combat, 0);
        this.registerSetting(speed = new SliderSetting("Speed", 45.0D, 1.0D, 100.0D, 1.0D));
        this.registerSetting(fov = new SliderSetting("FOV", 90.0D, 15.0D, 180.0D, 1.0D));
        this.registerSetting(distance = new SliderSetting("Distance", 4.5D, 1.0D, 10.0D, 0.5D));
        this.registerSetting(clickAim = new ButtonSetting("Click aim", true));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(aimInvis = new ButtonSetting("Aim invis", false));
        this.registerSetting(blatantMode = new ButtonSetting("Blatant mode", true));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
    }

    public void onUpdate() {
        if (mc.currentScreen == null && mc.inGameHasFocus) {
            if (!weaponOnly.isToggled() || Utils.holdingWeapon()) {
                if (!clickAim.isToggled() || Utils.isClicking()) {
                    Entity target = this.getEnemy();
                    if (target != null) {
                        if (blatantMode.isToggled()) {
                            Utils.aim(target, 0.0F, false);
                        } else {
                            float[] rotations = RotationUtils.getRotationsPredicted(target, 1);
                            if (rotations == null) return;

                            float currentYaw = mc.thePlayer.rotationYaw;
                            float currentPitch = mc.thePlayer.rotationPitch;

                            float yawDiff = MathHelper.wrapAngleTo180_float(rotations[0] - currentYaw);
                            float pitchDiff = rotations[1] - currentPitch;

                            float accFactor = (float) (speed.getInput() / 400.0);

                            float jitterYaw = (float) (Math.random() * 0.15 - 0.075);
                            float jitterPitch = (float) (Math.random() * 0.15 - 0.075);

                            float randomFactor = 0.9f + (float) (Math.random() * 0.15);

                            float yawStep = Math.abs(yawDiff) * accFactor * randomFactor + 0.26f;
                            float pitchStep = Math.abs(pitchDiff) * accFactor * randomFactor + 0.26f;

                            yawStep = Math.min(yawStep, 19.9f);
                            pitchStep = Math.min(pitchStep, 19.9f);

                            float newYaw = currentYaw + (yawDiff > 0 ? yawStep : -yawStep) + jitterYaw;
                            float newPitch = currentPitch + (pitchDiff > 0 ? pitchStep : -pitchStep) + jitterPitch;

                            newPitch = MathHelper.clamp_float(newPitch, -90, 90);

                            mc.thePlayer.rotationYaw = newYaw;
                            mc.thePlayer.rotationPitch = newPitch;

                        }
                    }
                }
            }
        }
    }


    private Entity getEnemy() {
        final int n = (int) fov.getInput();
        for (final EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            if (entityPlayer != mc.thePlayer && entityPlayer.deathTime == 0) {
                if (Utils.isFriended(entityPlayer)) continue;
                if (ignoreTeammates.isToggled() && Utils.isTeamMate(entityPlayer)) continue;
                if (!aimInvis.isToggled() && entityPlayer.isInvisible()) continue;
                if (mc.thePlayer.getDistanceToEntity(entityPlayer) > distance.getInput()) continue;
                if (AntiBot.isBot(entityPlayer)) continue;
                if (!blatantMode.isToggled() && n != 360 && !Utils.inFov((float) n, entityPlayer)) continue;
                return entityPlayer;
            }
        }
        return null;
    }
}
