package keystrokesmod.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class MoveInputEvent extends Event {
    private float forward;
    private float strafe;
    private boolean jump;
    private boolean sneak;
    private double sneakSlowDown;

    public MoveInputEvent(float forward, float strafe, boolean jump, boolean sneak, double sneakSlowDown) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneak = sneak;
        this.sneakSlowDown = sneakSlowDown;
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
