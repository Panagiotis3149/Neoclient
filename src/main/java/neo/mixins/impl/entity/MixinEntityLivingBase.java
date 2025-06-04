package neo.mixins.impl.entity;

import com.google.common.collect.Maps;
import neo.event.JumpEvent;
import neo.event.PreMotionEvent;
import neo.event.SwingAnimationEvent;
import neo.module.impl.client.Settings;
import neo.util.player.move.RotationUtils;
import neo.util.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    private final Map<Integer, PotionEffect> activePotionsMap = Maps.newHashMap();

    @Shadow
    public PotionEffect getActivePotionEffect(Potion potionIn) {
        return this.activePotionsMap.get(Integer.valueOf(potionIn.id));
    }

    @Shadow
    public boolean isPotionActive(Potion potionIn) {
        return this.activePotionsMap.containsKey(Integer.valueOf(potionIn.id));
    }

    @Shadow
    public float rotationYawHead;

    @Shadow
    public float renderYawOffset;

    @Shadow
    public float swingProgress;

    @Overwrite
    protected float func_110146_f(float p_1101461, float p_1101462) {
        float rotationYaw = this.rotationYaw;
        if (Settings.fullBody != null && Settings.rotateBody != null && !Settings.fullBody.isToggled() && Settings.rotateBody.isToggled() && (EntityLivingBase) (Object) this instanceof EntityPlayerSP) {
            if (this.swingProgress > 0F) {
                p_1101461 = RotationUtils.renderYaw;
            }
            rotationYaw = RotationUtils.renderYaw;
            rotationYawHead = RotationUtils.renderYaw;
        }
        float f = MathHelper.wrapAngleTo180_float(p_1101461 - this.renderYawOffset);
        this.renderYawOffset += f * 0.3F;
        float f1 = MathHelper.wrapAngleTo180_float(rotationYaw - this.renderYawOffset);
        boolean flag = f1 < 90.0F || f1 >= 90.0F;

        if (f1 < -75.0F) {
            f1 = -75.0F;
        }

        if (f1 >= 75.0F) {
            f1 = 75.0F;
        }

        this.renderYawOffset = rotationYaw - f1;

        if (f1 * f1 > 2500.0F) {
            this.renderYawOffset += f1 * 0.2F;
        }

        if (flag) {
            p_1101462 *= -1.0F;
        }

        return p_1101462;
    }

    @Shadow
    protected float getJumpUpwardsMotion() {
        return 0.42F;
    }

    @Overwrite
    protected void jump() {
        JumpEvent jumpEvent = new JumpEvent(this.getJumpUpwardsMotion(), this.rotationYaw);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(jumpEvent);
        if (jumpEvent.isCanceled()) {
            return;
        }

        if (Settings.movementFix != null && Settings.movementFix.isToggled() && PreMotionEvent.setRenderYaw()) {
            jumpEvent.setYaw(RotationUtils.renderYaw);
        }

        this.motionY = jumpEvent.getMotionY();

        if (this.isPotionActive(Potion.jump)) {
            this.motionY += (float) (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
        }

        if (this.isSprinting()) {
            float f = jumpEvent.getYaw() * 0.017453292F;
            this.motionX -= MathHelper.sin(f) * 0.2F;
            this.motionZ += MathHelper.cos(f) * 0.2F;
        }

        this.isAirBorne = true;
        ForgeHooks.onLivingJump(((EntityLivingBase) (Object) this));
    }


    /**
     * @author xia__mc
     * @reason for Animations module
     */
    @Inject(method = "getArmSwingAnimationEnd", at = @At("RETURN"), cancellable = true)
    private void onGetArmSwingAnimationEnd(@NotNull CallbackInfoReturnable<Integer> cir) {
        SwingAnimationEvent swingAnimationEvent = new SwingAnimationEvent(cir.getReturnValue());
        MinecraftForge.EVENT_BUS.post(swingAnimationEvent);

        cir.setReturnValue((int) (swingAnimationEvent.getAnimationEnd() * Utils.getTimer().timerSpeed));
    }

}