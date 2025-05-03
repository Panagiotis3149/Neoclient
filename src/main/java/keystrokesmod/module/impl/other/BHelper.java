package keystrokesmod.module.impl.other;


import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.Module;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BHelper extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final float DELTA = (float) 1 / 30;
    private int groundTicks = 0;
    private int jumpfunny = 0;
    private final long jumpticks = 0;


    public BHelper() {
        super("BloxD Helper", Module.category.other);
    }


        private Vec3 impulseVector = new Vec3(0, 0, 0);
        private Vec3 forceVector = new Vec3(0, 0, 0);
        private Vec3 velocityVector = new Vec3(0, 0, 0);
        private final Vec3 gravityVector = new Vec3(0, -10, 0);
        private final double gravityMul = 2;
        private final double mass = 1;

    public Vec3 getMotionForTick(double DELTA) {
        double massDiv = 1 / this.mass;

        forceVector = multiply(forceVector, massDiv);
        forceVector = forceVector.add(gravityVector);
        forceVector = multiply(forceVector, gravityMul);

        impulseVector = multiply(impulseVector, massDiv);
        forceVector = multiply(forceVector, DELTA);
        impulseVector = impulseVector.add(forceVector);

        velocityVector = velocityVector.add(impulseVector);

        set(forceVector, 0, 0, 0);
        set(impulseVector, 0, 0, 0);

        return velocityVector;
    }


    public static Vec3 multiply(Vec3 vec, double scalar) {
        return new Vec3(vec.xCoord * scalar, vec.yCoord * scalar, vec.zCoord * scalar);
    }

    private Vec3 set(Vec3 vec, double x, double y, double z) {
        vec.subtract(vec.xCoord, vec.yCoord, vec.zCoord);
        vec.addVector(x, y, z);
        return vec;
    }

    @SubscribeEvent
    public void OnPreMotion(PreMotionEvent e) {
        if (mc.thePlayer.onGround && mc.thePlayer.motionY < 0) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionZ = 0;
        }

        if (mc.thePlayer.onGround && mc.thePlayer.motionY == 0.41999998688697815) {
            jumpfunny = Math.min(jumpfunny + 1, 3);
            mc.thePlayer.addVelocity(0, 8, 0);
        }

        groundTicks = mc.thePlayer.onGround ? groundTicks + 1 : 0;
        if (groundTicks > 5) {
            jumpfunny = 0;
        }

        if (mc.thePlayer.isCollidedHorizontally) {
            mc.thePlayer.addVelocity(0, 8, 0);
        }


        MoveUtil.stop();
        e.setFriction(0);
        e.setSpeed(jumpticks > System.currentTimeMillis() ? 1 : (mc.thePlayer.isUsingItem() ? 0.06 : 0.26 + 0.025 * jumpfunny));
        mc.thePlayer.motionY = getMotionForTick(DELTA).yCoord * DELTA;
    }

}
