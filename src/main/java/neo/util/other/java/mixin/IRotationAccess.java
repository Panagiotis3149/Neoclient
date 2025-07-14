package neo.util.other.java.mixin;

import net.minecraft.util.Vec3;

// Reason? Workaround.
public interface IRotationAccess {
    Vec3 callGetVectorForRotation(float pitch, float yaw);
}
