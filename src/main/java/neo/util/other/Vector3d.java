package neo.util.other;

public class Vector3d {
    public double x, y, z;

    public Vector3d() {
        this(0, 0, 0);
    }

    public Vector3d(double x, double y, double z) {
        set(x, y, z);
    }

    public Vector3d set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3d set(Vector3d v) {
        return set(v.x, v.y, v.z);
    }

    public Vector3d add(Vector3d v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public Vector3d mul(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        return this;
    }
}
