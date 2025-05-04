package LWJG.test;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import LWJG.core.Base;
import LWJG.core.CFrame;
import LWJG.core.Vector2;
import LWJG.rendering.Drawable;
import LWJG.rendering.Rectangle;
import LWJG.rendering.RenderingPanel;

public class Main {
    public static void main(String[] args) {
        final int WIDTH = 500;
        final int HEIGHT = 500;
        final double TARGET_FPS = 60.0;
        final double OPTIMAL_TIME = 1_000_000_000.0 / TARGET_FPS;
        Rectangle rect = new Rectangle(new CFrame(new Vector2(250, 250), 45), new Vector2(0, 0), 1, new Vector2(0, 0), 0, new Vector2(100, 100), Color.RED, 0, true);
        List<Drawable> d = new ArrayList<>();
        d.add(rect);
        JFrame f = new JFrame("Hi");
        f.setSize(new Dimension(500,500));
        RenderingPanel renderingPanel = new RenderingPanel(new Base(new CFrame(null, OPTIMAL_TIME), null, TARGET_FPS, null, OPTIMAL_TIME, null));
        renderingPanel.render(d);
        f.add(renderingPanel);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Thread gameLoopThread = new Thread(() -> {
            long lastLoopTime = System.nanoTime();

            while (true) {
                long now = System.nanoTime();
                long updateLength = now - lastLoopTime;
                lastLoopTime = now;

                // Calculate deltaTime in seconds
                double deltaTime = updateLength / 1_000_000_000.0;

                // --- Update Game State ---
                rect.update(deltaTime);
                // Add other game logic updates here

                // --- Render ---
                // Use invokeLater to ensure rendering happens safely on the EDT
                SwingUtilities.invokeLater(() -> renderingPanel.render(d));

                // --- Frame Rate Control ---
                long sleepTime = (lastLoopTime - System.nanoTime() + (long)OPTIMAL_TIME) / 1_000_000; // Time to sleep in ms
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupt status
                        System.err.println("Game loop interrupted.");
                        break; // Exit loop if interrupted
                    }
                }
                // If sleepTime is negative, the loop took longer than OPTIMAL_TIME (frame drop)
            }
        });

        gameLoopThread.start();
    }
}