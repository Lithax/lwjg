package LWJG.rendering;

import java.awt.Graphics;
import java.util.List;
import javax.swing.JPanel;
import LWJG.core.Base;

public class RenderingPanel extends JPanel {
    private List<Drawable> drawables;
    private Base viewport;

    public RenderingPanel(Base viewport) {
        super();
        this.viewport = viewport;
    }

    public void render(List<Drawable> drawables) {
        this.drawables = drawables;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (drawables == null) return;

        for (Drawable drawable : drawables) {
            if (drawable.isVisible() && viewport.intersects(drawable)) {
                drawable.drawSelf(g);
            }
        }
    }
}