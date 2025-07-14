package neo.mixins.impl.performance;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(EnumFacing.class)
public class MixinEnumFacing {

    @Shadow
    @Final
    public static EnumFacing[] VALUES;
    @Shadow
    @Final
    private int opposite;
    private int frontOffsetX;
    private int frontOffsetY;
    private int frontOffsetZ;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cacheOffsets(String string, int i, int indexIn, int oppositeIn, int horizontalIndexIn, String nameIn, EnumFacing.AxisDirection axisDirectionIn, EnumFacing.Axis axisIn, Vec3i directionVecIn, CallbackInfo ci) {
        frontOffsetX = axisIn == EnumFacing.Axis.X ? axisDirectionIn.getOffset() : 0;
        frontOffsetY = axisIn == EnumFacing.Axis.Y ? axisDirectionIn.getOffset() : 0;
        frontOffsetZ = axisIn == EnumFacing.Axis.Z ? axisDirectionIn.getOffset() : 0;
    }


    @Overwrite
    public int getFrontOffsetX() {
        return frontOffsetX;
    }


    @Overwrite
    public int getFrontOffsetY() {
        return frontOffsetY;
    }


    @Overwrite
    public int getFrontOffsetZ() {
        return frontOffsetZ;
    }


    @Overwrite
    public EnumFacing getOpposite() {
        return VALUES[opposite];
    }


    @Overwrite
    public static EnumFacing random(Random rand) {
        return VALUES[rand.nextInt(VALUES.length)];
    }
}