package LWJG.rendering;

import java.awt.Color;
import java.awt.Graphics;

import LWJG.core.CFrame;
import LWJG.core.Vector2;

public class Image extends Drawable {
    public Image(CFrame cframe, Vector2 vel, double angularVelocity, Vector2 accel, double angularAcceleration, Vector2 dim, Color initialColor, int zIndex, boolean filled) {
        super(cframe, vel, angularVelocity, accel, angularAcceleration, dim, initialColor, zIndex);
    }

    @Override
    public void drawSelf(Graphics g) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'drawSelf'");
    }
}