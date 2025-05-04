package LWJG.core;

public final class Vector2 {
	public double x;
	public double y;
	
	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void add(Vector2 vec) {
		x += vec.x;
		y += vec.y;
	}

	 /**
     * Multiplies this vector by a scalar.
     * @param scalar The scalar value.
     * @return A *new* Vector2 instance representing the scaled vector.
     */
    public Vector2 multiply(double scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }

    /**
     * Creates a copy of this vector.
     * @return A new Vector2 instance with the same x and y values.
     */
    public Vector2 copy() {
        return new Vector2(this.x, this.y);
    }

    /**
     * Returns a new Vector2 representing the sum of this vector and another vector.
     * Does not modify this vector.
     * @param vec The vector to add.
     * @return A new Vector2 instance representing the sum.
     */
    public Vector2 plus(Vector2 vec) {
        return new Vector2(this.x + vec.x, this.y + vec.y);
    }
    
    /**
     * Returns a new Vector2 representing this vector minus another vector.
     * Does not modify this vector.
     * @param vec The vector to subtract.
     * @return A new Vector2 instance representing the difference.
     */
    public Vector2 minus(Vector2 vec) {
        return new Vector2(this.x - vec.x, this.y - vec.y);
    }

    @Override
    public String toString() {
        return "Vector2[" + x + ", " + y + "]";
    }
    
    public static Vector2 zero() {
        return new Vector2(0, 0);
    }
}