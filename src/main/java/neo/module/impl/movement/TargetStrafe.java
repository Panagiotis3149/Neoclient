package neo.module.impl.movement;

import neo.event.JumpEvent;
import neo.event.PrePlayerInputEvent;
import neo.module.Module;
import neo.module.ModuleManager;
import neo.module.impl.combat.KillAura;
import neo.module.setting.impl.ButtonSetting;
import neo.module.setting.impl.SliderSetting;
import neo.script.classes.Vec3;
import neo.util.player.move.MoveUtil;
import neo.util.player.move.PlayerRotation;
import neo.util.world.block.BlockUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static neo.module.ModuleManager.*;

public class TargetStrafe extends Module {
    private final SliderSetting range = new SliderSetting("Range", 1, 0.2, 6, 0.1);
    private final ButtonSetting speed = new ButtonSetting("Allow speed", true);
    private final ButtonSetting fly = new ButtonSetting("Allow fly", false);
    private final ButtonSetting manual = new ButtonSetting("Allow manual", false);
    private final ButtonSetting strafe = new ButtonSetting("Strafe around", true);

    private static float yaw;
    private static EntityLivingBase target = null;
    private boolean left, colliding;
    private static boolean active = false;

    public TargetStrafe() {
        super("TargetStrafe", category.movement);
        this.registerSetting(range);
        this.registerSetting(speed);
        this.registerSetting(fly);
        this.registerSetting(manual);
        this.registerSetting(strafe);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static float getMovementYaw() {
        if (active && target != null) return yaw;
        return mc.thePlayer.rotationYaw;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onJump(JumpEvent event) {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreUpdate(PrePlayerInputEvent event) {
        if (scaffold == null || scaffold.isEnabled() || killAura == null || !killAura.isEnabled()) {
            active = false;
            return;
        }

        active = true;

        if (target != null && active) {
            event.setYaw(yaw);
        }

        if ((!speed.isToggled() && bHop.isEnabled())
                || (!fly.isToggled() && ModuleManager.fly.isEnabled())
                || (!manual.isToggled() && !bHop.isEnabled() && !ModuleManager.fly.isEnabled())) {
            target = null;
            return;
        }

        if (KillAura.target == null) {
            target = null;
            return;
        }

        target = KillAura.target;

        if (mc.thePlayer.isCollidedHorizontally || !BlockUtils.isBlockUnder(5)) {
            if (!colliding) {
                if (strafe.isToggled()) {
                    MoveUtil.strafe5(MoveUtil.getSpeed()); // Always call strafe if module is enabled
                }
                left = !left;
            }
            colliding = true;
        } else {
            colliding = false;
        }

        if (target != null) {
            float yaw = PlayerRotation.getYaw(new Vec3(target)) + (90 + 45) * (left ? -1 : 1);

            final double range = this.range.getInput() + Math.random() / 100f;
            final double posX = -MathHelper.sin((float) Math.toRadians(yaw)) * range + target.posX;
            final double posZ = MathHelper.cos((float) Math.toRadians(yaw)) * range + target.posZ;

            yaw = PlayerRotation.getYaw(new Vec3(posX, target.posY, posZ));

            TargetStrafe.yaw = yaw;
        }
    }


    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        active = false;
    }
}