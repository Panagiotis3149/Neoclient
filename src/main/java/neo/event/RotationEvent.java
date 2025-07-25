package neo.event;

import neo.module.impl.other.RotationHandler;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RotationEvent extends Event {
    private float yaw;
    private float pitch;
    private RotationHandler.MoveFix moveFix;
    private boolean smoothBack = true;
    private boolean isSet;

    public RotationEvent(float yaw, float pitch, RotationHandler.MoveFix moveFix) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.moveFix = moveFix;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        isSet = true;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        isSet = true;
    }

    public RotationHandler.MoveFix getMoveFix() {
        return moveFix;
    }

    public void setMoveFix(RotationHandler.MoveFix moveFix) {
        this.moveFix = moveFix;
    }

    public boolean isSmoothBack() {
        return smoothBack;
    }

    public void noSmoothBack() {
        smoothBack = false;
    }

    public boolean isSet() {
        return isSet;
    }
}
