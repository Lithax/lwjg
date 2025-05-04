package LWJG.rendering;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import LWJG.core.CFrame;
import LWJG.core.Vector2;

public class Rectangle extends Drawable {
    private boolean filled;

    public Rectangle(CFrame cframe, Vector2 vel, double angularVelocity, Vector2 accel, double angularAcceleration, Vector2 dim, Color initialColor, int zIndex, boolean filled) {
        super(cframe, vel, angularVelocity, accel, angularAcceleration, dim, initialColor, zIndex);
        this.filled = filled;
    }

    @Override
    public void drawSelf(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.translate(cframe.pos.x, cframe.pos.y);
        g2.rotate(cframe.a);

        g2.setColor(colorTint);
        
        double halfWidth = dimension.x / 2.0;
        double halfHeight = dimension.y / 2.0;
        Rectangle2D.Double rectShape = new Rectangle2D.Double(-halfWidth, -halfHeight, dimension.x, dimension.y);

        if(filled) g2.fill(rectShape); else g2.draw(rectShape);

        g2.dispose();
    }
}