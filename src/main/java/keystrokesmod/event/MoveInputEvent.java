package keystrokesmod.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class MoveInputEvent extends Event {
    private float forward;
    private float strafe;
    private boolean jump;
    private boolean sneak;
    private double sneakSlowDown;
    public double x;
    private double y;
    public double z;

    public MoveInputEvent(float forward, float strafe, boolean jump, boolean sneak, double sneakSlowDown) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneak = sneak;
        this.sneakSlowDown = sneakSlowDown;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getForward() {
        return forward;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public float getStrafe() {
        return strafe;
    }

    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    public boolean isJump() {
        return jump;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public boolean isSneak() {
        return sneak;
    }

    public void setSneak(boolean sneak) {
        this.sneak = sneak;
    }

    public double getSneakSlowDown() {
        return sneakSlowDown;
    }

    public void setSneakSlowDown(double sneakSlowDown) {
        this.sneakSlowDown = sneakSlowDown;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void zero() {
        x = 0.0;
        y = 0.0;
        z = 0.0;
    }

    public void zeroXZ() {
        x = 0.0;
        z = 0.0;
    }

    @Override
    public void setCanceled(boolean cancel) {
        if (cancel) {
            setForward(0);
            setStrafe(0);
            setJump(false);
            setSneak(false);
        }
    }
}
