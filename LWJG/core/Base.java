package LWJG.core;

public class Base {
    protected CFrame cframe; // CFRAME
    protected Vector2 vel; // Velocity xy
    protected double avel; // Angular Velocity
    protected Vector2 accel; // Acceleration
    protected double aaccel; // Angular accel
    protected Vector2 linearJerk;
    protected double angularJerk;
    protected Vector2 dimension;
    
    public Base(CFrame cframe, Vector2 vel, double angularVelocity, Vector2 accel, double angularAcceleration, Vector2 linearJerk, double angularJerk, Vector2 dim) {
        this.cframe = cframe;
        this.vel = vel != null ? vel : Vector2.zero();
        this.avel = angularVelocity;
        this.accel = accel != null ? accel : Vector2.zero();
        this.aaccel = angularAcceleration;
        this.linearJerk = linearJerk != null ? linearJerk : Vector2.zero();
        this.angularJerk = angularJerk;
        this.dimension = dim;
    }

    public Base(CFrame cframe, Vector2 vel, double angularVelocity, Vector2 accel, double angularAcceleration, Vector2 dim) {
        this(cframe, vel, angularVelocity, accel, angularAcceleration, Vector2.zero(), 0.0, dim); // Call the main constructor with zero jerk
    }

    public void setAcceleration(Vector2 accel) {
        this.accel = accel;
    }

    public void setAngularAcceleration(double angularAcceleration) {
        this.aaccel = angularAcceleration;
    }

    public void setAngularVelocity(double angularVelocity) {
        this.avel = angularVelocity;
    }

    public double getAngularAcceleration() {
        return aaccel;
    }

    public double getAngularVelocity() {
        return avel;
    }

    public void setCFrame(CFrame cframe) {
        this.cframe = cframe;
    }

    public void setVelocity(Vector2 vel) {
        this.vel = vel;
    }

    public void setDimension(Vector2 dimension) {
        this.dimension = dimension;
    }

    public Vector2 getAcceleration() {
        return accel;
    }

    public Vector2 getDimension() {
        return dimension;
    }

    public CFrame getCFrame() {
        return cframe;
    }

    public void update(double deltaTime) {
        // --- Optional: Jerk integration (update acceleration) ---
        if (linearJerk != null) { // Check if jerk is used
             accel.add(linearJerk.multiply(deltaTime));
        }
        aaccel += angularJerk * deltaTime;

        // --- Acceleration integration (update velocity) ---
        // Linear
        Vector2 scaledAccel = accel.multiply(deltaTime);
        vel.add(scaledAccel);
        // Angular
        avel += aaccel * deltaTime;

        // --- Velocity integration (update position/rotation) ---
        // Linear
        cframe.addX(vel.x * deltaTime);
        cframe.addY(vel.y * deltaTime);
        // Angular
        cframe.addRadians(avel * deltaTime);
    }

    public Vector2 getVelocity() {
        return vel;
    }

    @Override
    public String toString() {
        return "Base[cframe=" + cframe +
               ", vel=" + vel + ", angularVel=" + avel +
               ", accel=" + accel + ", angularAccel=" + aaccel +
               ", dim=" + dimension + "]";
    }
}