package LWJG.core;

public final class CFrame {
    public Vector2 pos;
    public double a; // Stored in radians

    public CFrame(Vector2 pos, double rotation) {
        this.pos = pos;
        setRotation(rotation);
    }

    public void setX(double x) {
        pos.x = x;
    }

    public void setY(double y) {
        pos.y = y;
    }

    public double getX() {
        return pos.x;
    }

    public double getY() {
        return pos.y;
    }

    public void addX(double x) {
        pos.x += x;
    }

    public void addY(double y) {
        pos.y += y;
    }

    /**
     * Sets rotation angle in radians.
     */
    public void setRadians(double a) {
        this.a = normalizeRadians(a);
    }

    /**
     * Sets rotation angle in degrees.
     */
    public void setRotation(double degrees) {
        if (degrees < 0 || degrees >= 360) {
            degrees = degrees % 360;
            if (degrees < 0) degrees += 360;
        }
        this.a = Math.toRadians(degrees);
    }

    /**
     * Adds to current rotation angle in radians.
     */
    public void addRadians(double delta) {
        this.a = normalizeRadians(this.a + delta);
    }

    /**
     * Adds to current rotation angle in degrees.
     */
    public void addRotation(double deltaDegrees) {
        this.a = normalizeRadians(this.a + Math.toRadians(deltaDegrees));
    }

    /**
     * Returns the angle in radians.
     */
    public double getRadians() {
        return this.a;
    }

    /**
     * Returns the angle in degrees.
     */
    public double getRotation() {
        return Math.toDegrees(this.a);
    }

    public void moveForward(double distance) {
        pos.x += Math.cos(a) * distance;
        pos.y += Math.sin(a) * distance;
    }    

    public void lookAt(double targetX, double targetY) {
        this.a = Math.atan2(targetY - pos.y, targetX - pos.x);
    }    

    /**
     * Normalizes any radian angle to the range [0, 2π).
     */
    private double normalizeRadians(double a) {
        a = a % (2 * Math.PI);
        if (a < 0) a += 2 * Math.PI;
        return a;
    }

    public double distanceTo(CFrame other) {
        double dx = this.pos.x - other.pos.x; // Use this.pos.x or just pos.x
        double dy = this.pos.y - other.pos.y; // Use this.pos.y or just pos.y
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public CFrame copy() {
        return new CFrame(new Vector2(pos.x, pos.y), a);
    }    
}
