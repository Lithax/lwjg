package LWJG.rendering;

import LWJG.core.Base;
import LWJG.core.CFrame;
import LWJG.core.Vector2;
import java.awt.Color;
// You'll likely need classes for these too:
// import LWJG.rendering.Color;
// import LWJG.rendering.Texture;
// import LWJG.rendering.Font;
import java.awt.Graphics;

public abstract class Drawable extends Base {
    protected boolean isVisible;
    protected int zIndex;
    protected Color colorTint;
    protected double alpha;
    protected Outline outline;

    public Drawable(CFrame cframe, Vector2 vel, double angularVelocity, Vector2 accel, double angularAcceleration, Vector2 dim,
                    Color initialColor, int zIndex) {
        super(cframe, vel, angularVelocity, accel, angularAcceleration, dim);
        this.isVisible = true;
        this.zIndex = zIndex;
        this.colorTint = (initialColor != null) ? initialColor : Color.WHITE;
        this.alpha = 1.0;
        this.outline = null;
    }

    /**
     * Abstract method that concrete drawable types must implement.
     * This method contains the specific drawing logic (e.g., OpenGL calls,
     * drawing API calls) for this type of drawable object, using its
     * position, rotation, scale, color, texture, text, etc.
     *
     * @param renderer A rendering context object that provides drawing functions
     *                 (e.g., access to OpenGL, a Canvas, Graphics2D, etc.)
     */
    public abstract void drawSelf(Graphics g); // Replace 'Renderer' with your actual rendering context/system

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public Color getColorTint() {
        return colorTint;
    }

    public void setColorTint(Color colorTint) {
        this.colorTint = (colorTint != null) ? colorTint : Color.WHITE;
    }

    public double getAlpha() {
        return alpha;
    }

    /**
     * Sets the alpha transparency.
     * @param alpha Value between 0.0 (fully transparent) and 1.0 (fully opaque). Clamped internally.
     */
    public void setAlpha(double alpha) {
        this.alpha = Math.max(0.0, Math.min(1.0, alpha)); // Clamp between 0 and 1
    }
}