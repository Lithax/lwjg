package LWJG.physics;

import LWJG.core.Vector2; // Import necessary class

public class Gravity {
    private final double strength; // Magnitude (e.g., 9.81 or pixels/sec^2)
    private final Vector2 acceleration;

    public Gravity(double g) {
        // Ensure g is positive, direction is handled by the vector
        this.strength = Math.abs(g); 
        // Assuming positive Y is UP, gravity pulls DOWN (negative Y)
        // If positive Y is DOWN, use new Vector2(0, this.strength)
        this.acceleration = new Vector2(0, -this.strength); 
    }

    /**
     * Gets the gravitational acceleration as a Vector2.
     * @return The acceleration vector due to gravity.
     */
    public Vector2 getAcceleration() {
        // Return a copy to prevent external modification if desired
        // return acceleration.copy(); 
        return acceleration; 
    }

    /**
     * Gets the magnitude of gravitational acceleration.
     * @return The strength 'g'.
     */
    public double getStrength() {
        return strength;
    }

    @Override
    public String toString() {
        return "Gravity[strength=" + strength + ", acceleration=" + acceleration + "]";
    }
}