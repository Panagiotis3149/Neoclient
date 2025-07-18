package neo.event;

import neo.script.classes.PlayerState;
import net.minecraftforge.fml.common.eventhandler.Event;
import neo.util.player.move.MoveUtil;

import static neo.util.Utils.mc;

public class PreMotionEvent extends Event {
    public double posX;
    public double posY;
    public double posZ;
    private float yaw;
    private float pitch;
    private boolean onGround;
    private static boolean setRenderYaw;
    private boolean isSprinting;
    private boolean isSneaking;
    private float friction;

    public PreMotionEvent(double posX, double posY, double posZ, float yaw, float pitch, boolean onGround, boolean isSprinting, boolean isSneaking) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.isSprinting = isSprinting;
        this.isSneaking = isSneaking;
    }



    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        setRenderYaw = true;
    }

    public void setSpeed(final double speed, final double motionMultiplier) {
        setFriction((float) (mc.thePlayer.moveForward != 0 && mc.thePlayer.moveStrafing != 0 ? speed * 0.98F : speed));
        mc.thePlayer.motionX *= motionMultiplier;
        mc.thePlayer.motionZ *= motionMultiplier;
    }

    public void setSpeed(final double speed) {
        setFriction((float) (mc.thePlayer.moveForward != 0 && mc.thePlayer.moveStrafing != 0 ? speed * 0.98F : speed));
        MoveUtil.stop();
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public static boolean setRenderYaw() {
        return setRenderYaw;
    }

    public void setRenderYaw(boolean setRenderYaw) {
        PreMotionEvent.setRenderYaw = setRenderYaw;
    }
    public boolean isSprinting() {
        return isSprinting;
    }

    public void setSprinting(boolean sprinting) {
        this.isSprinting = sprinting;
    }

    public boolean isSneaking() {
        return isSneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.isSneaking = sneaking;
    }

    public boolean isEquals(PlayerState e) {
        return e.x == this.posX && e.y == this.posY && e.z == this.posZ && e.yaw == this.yaw && e.pitch == this.pitch && e.onGround == this.onGround && e.isSprinting == this.isSprinting && e.isSneaking == this.isSneaking;
    }
}
