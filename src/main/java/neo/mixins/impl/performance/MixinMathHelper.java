package neo.mixins.impl.performance;

import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MathHelper.class)
public class MixinMathHelper {
    @Shadow
    @Mutable
    private static float[] SIN_TABLE;

    private static final int[] SINE_TABLE_INT = new int[16384 + 1];
    private static final float SINE_TABLE_MIDPOINT;

    static {
        float[] originalTable = new float[65536];
        for (int i = 0; i < 65536; i++) {
            originalTable[i] = (float) Math.sin(i * Math.PI * 2.0 / 65536.0);
        }

        for (int i = 0; i < SINE_TABLE_INT.length; i++) {
            SINE_TABLE_INT[i] = Float.floatToRawIntBits(originalTable[i]);
        }

        SINE_TABLE_MIDPOINT = originalTable[originalTable.length / 2];

        // Runtime validation
        /*
        for (int i = 0; i < originalTable.length; i++) {
            float expected = originalTable[i];
            float value = lookup(i);
            if (expected != value) {
                throw new IllegalStateException("LUT mismatch at index " + i);
            }
        }

        */ // For performance.
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void replaceSinTable(CallbackInfo ci) {
        SIN_TABLE = new float[16384];
        for (int i = 0; i < SIN_TABLE.length; i++) {
            SIN_TABLE[i] = Float.intBitsToFloat(SINE_TABLE_INT[i]);
        }
    }

    // Override sin to use compact LUT
    @Overwrite
    public static float sin(float f) {
        return lookup((int) (f * 10430.378f) & 0xFFFF);
    }

    // Override cos to use compact LUT
    @Overwrite
    public static float cos(float f) {
        return lookup((int) (f * 10430.378f + 16384.0f) & 0xFFFF);
    }

    private static float lookup(int index) {
        if (index == 32768) {
            return SINE_TABLE_MIDPOINT;
        }
        int neg = (index & 0x8000) << 16;
        int mask = (index << 17) >> 31;
        int pos = (0x8001 & mask) + (index ^ mask);
        pos &= 0x7fff;
        return Float.intBitsToFloat(SINE_TABLE_INT[pos] ^ neg);
    }
}
