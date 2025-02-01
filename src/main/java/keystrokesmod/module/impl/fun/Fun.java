package keystrokesmod.module.impl.fun;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;

import java.util.Iterator;
import java.util.Random;

public class Fun {
    public static class Spin extends Module {
        public SliderSetting speed;
        private float originalYaw;
        private float originalPitch;
        private Random random;

        public Spin() {
            super("Spin", category.fun, 0);
            this.registerSetting(speed = new SliderSetting("Speed", 25.0D, 1.0D, 60.0D, 1.0D));
            this.random = new Random();
        }

        public void onEnable() {
            this.originalYaw = mc.thePlayer.rotationYaw; // Store the original yaw
            this.originalPitch = mc.thePlayer.rotationPitch; // Store the original pitch
        }

        public void onDisable() {
            // Optionally reset the player's yaw and pitch when disabling
            mc.thePlayer.rotationYaw = this.originalYaw;
            mc.thePlayer.rotationPitch = this.originalPitch;
        }

        public void onUpdate() {
            // Randomly adjust the yaw
            float randomYawChange = (float) ((random.nextFloat() * 2 - 1) * speed.getInput()); // Random value between -speed and +speed
            mc.thePlayer.rotationYaw += randomYawChange;

            // Randomly adjust the pitch
            float randomPitchChange = (float) ((random.nextFloat() * 2 - 1) * (speed.getInput() / 2)); // Random value between -speed/2 and +speed/2
            mc.thePlayer.rotationPitch += randomPitchChange;

            // Optional: Wrap the yaw to keep it within valid ranges
            if (mc.thePlayer.rotationYaw >= 360.0F) {
                mc.thePlayer.rotationYaw -= 360.0F;
            } else if (mc.thePlayer.rotationYaw < 0.0F) {
                mc.thePlayer.rotationYaw += 360.0F;
            }

            // Limit pitch to prevent extreme angles
            if (mc.thePlayer.rotationPitch > 90.0F) {
                mc.thePlayer.rotationPitch = 90.0F; // Limit pitch to 90 degrees
            } else if (mc.thePlayer.rotationPitch < -90.0F) {
                mc.thePlayer.rotationPitch = -90.0F; // Limit pitch to -90 degrees
            }
        }
    }

    public static class SlyPort extends Module {
        public SliderSetting range;
        public ButtonSetting playSound;
        public ButtonSetting playersOnly;
        public ButtonSetting aim;

        public SlyPort() {
            super("SlyPort", category.fun, 0);
            this.registerSetting(new DescriptionSetting("Teleport behind enemies."));
            this.registerSetting(range = new SliderSetting("Range", 6.0D, 2.0D, 15.0D, 1.0D));
            this.registerSetting(aim = new ButtonSetting("Aim", true));
            this.registerSetting(playSound = new ButtonSetting("Play sound", true));
            this.registerSetting(playersOnly = new ButtonSetting("Players only", true));
        }

        public void onEnable() {
            Entity en = this.ge();
            if (en != null) {
                this.tp(en);
            }

            this.disable();
        }

        private void tp(Entity en) {
            if (playSound.isToggled()) {
                mc.thePlayer.playSound("mob.endermen.portal", 1.0F, 1.0F);
            }

            Vec3 vec = en.getLookVec();
            double x = en.posX - vec.xCoord * 2.5D;
            double z = en.posZ - vec.zCoord * 2.5D;
            mc.thePlayer.setPosition(x, mc.thePlayer.posY, z);
            if (aim.isToggled()) {
                Utils.aim(en, 0.0F, false);
            }

        }

        private Entity ge() {
            Entity en = null;
            double r = Math.pow(this.range.getInput(), 2.0D);
            double dist = r + 1.0D;
            Iterator var6 = mc.theWorld.loadedEntityList.iterator();

            while (true) {
                Entity ent;
                do {
                    do {
                        do {
                            do {
                                if (!var6.hasNext()) {
                                    return en;
                                }

                                ent = (Entity) var6.next();
                            } while (ent == mc.thePlayer);
                        } while (!(ent instanceof EntityLivingBase));
                    } while (((EntityLivingBase) ent).deathTime != 0);
                } while (this.playersOnly.isToggled() && !(ent instanceof EntityPlayer));

                if (!AntiBot.isBot(ent)) {
                    double d = mc.thePlayer.getDistanceSqToEntity(ent);
                    if (!(d > r) && !(dist < d)) {
                        dist = d;
                        en = ent;
                    }
                }
            }
        }
    }

    public static class FlameTrail extends Module {
        public SliderSetting a;

        public FlameTrail() {
            super("Flame Trail", category.fun, 0);
        }

        public void onUpdate() {
            Vec3 vec = mc.thePlayer.getLookVec();
            double x = mc.thePlayer.posX - vec.xCoord * 2.0D;
            double y = mc.thePlayer.posY + ((double) mc.thePlayer.getEyeHeight() - 0.2D);
            double z = mc.thePlayer.posZ - vec.zCoord * 2.0D;
            mc.thePlayer.worldObj.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D, new int[]{0});
        }
    }

    public static class ExtraBobbing extends Module {
        public SliderSetting level;
        private boolean b;

        public ExtraBobbing() {
            super("Extra Bobbing", category.fun, 0);
            this.registerSetting(level = new SliderSetting("Level", 1.0D, 0.0D, 8.0D, 0.1D));
        }

        public void onEnable() {
            this.b = mc.gameSettings.viewBobbing;
            if (!this.b) {
                mc.gameSettings.viewBobbing = true;
            }

        }

        public void onDisable() {
            mc.gameSettings.viewBobbing = this.b;
        }

        public void onUpdate() {
            if (!mc.gameSettings.viewBobbing) {
                mc.gameSettings.viewBobbing = true;
            }

            if (mc.thePlayer.movementInput.moveForward != 0.0F || mc.thePlayer.movementInput.moveStrafe != 0.0F) {
                EntityPlayerSP var10000 = mc.thePlayer;
                var10000.cameraYaw = (float) ((double) var10000.cameraYaw + level.getInput() / 2.0D);
            }
        }
    }
}
