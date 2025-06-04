package neo.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class MoveEvent extends Event {
    public double x;
    public double y;
    public double z;

    public MoveEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
}
