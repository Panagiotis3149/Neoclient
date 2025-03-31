package keystrokesmod.module.impl.combat;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public class AimAssist extends Module {
    private SliderSetting speed;
    private SliderSetting fov;
    private SliderSetting distance;
    private ButtonSetting clickAim;
    private ButtonSetting weaponOnly;
    private ButtonSetting aimInvis;
    private ButtonSetting blatantMode;
    private ButtonSetting ignoreTeammates;

    private float[] prevRotations = new float[2];
    private boolean startSmoothing = false;
    private float accelerationFactor = 0.02f;
    private float currentSpeed = 0.5f;

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

                            float yawStep = (float) (Math.abs(yawDiff) * accFactor);
                            float pitchStep = (float) (Math.abs(pitchDiff) * accFactor);

                            float newYaw = currentYaw + (yawDiff > 0 ? yawStep : -yawStep);
                            float newPitch = currentPitch + (pitchDiff > 0 ? pitchStep : -pitchStep);
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
