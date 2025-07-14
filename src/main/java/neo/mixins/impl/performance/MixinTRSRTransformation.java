package neo.mixins.impl.performance;


import net.minecraftforge.client.model.TRSRTransformation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TRSRTransformation.class, remap = false)
public class MixinTRSRTransformation {
    @Shadow
    @Final
    private static TRSRTransformation identity;

    @Inject(method = "compose", at = @At("HEAD"), cancellable = true)
    private void earlyExitCompose(TRSRTransformation b, CallbackInfoReturnable<TRSRTransformation> cir) {
        TRSRTransformation thiz = (TRSRTransformation) (Object) this;
        if (thiz == identity) {
            cir.setReturnValue(b);
        } else if (b == identity) {
            cir.setReturnValue(thiz);
        }
    }

    @Inject(method = "blockCenterToCorner", at = @At("HEAD"), cancellable = true)
    private static void earlyExitBlockCenter(TRSRTransformation transform, CallbackInfoReturnable<TRSRTransformation> cir) {
        if (transform == identity) {
            cir.setReturnValue(transform);
        }
    }

    @Inject(method = "blockCornerToCenter", at = @At("HEAD"), cancellable = true)
    private static void earlyExitBlockCorner(TRSRTransformation transform, CallbackInfoReturnable<TRSRTransformation> cir) {
        if (transform == identity) {
            cir.setReturnValue(transform);
        }
    }
}