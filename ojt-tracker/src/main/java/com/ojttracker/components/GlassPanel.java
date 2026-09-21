package com.ojttracker.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * A rounded, semi-transparent "glass" card used as the base building
 * block for dashboard stat cards, forms and content sections, giving the
 * app its glassmorphism look instead of a default Swing appearance.
 */
public class GlassPanel extends JPanel {

    private int cornerRadius = Theme.RADIUS;
    private Color surfaceColor = Theme.GLASS_SURFACE;
    private Color borderColor = Theme.GLASS_BORDER;
    private boolean drawShadow = true;

    public GlassPanel() {
        setOpaque(false);
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    public void setSurfaceColor(Color surfaceColor) {
        this.surfaceColor = surfaceColor;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    public void setDrawShadow(boolean drawShadow) {
        this.drawShadow = drawShadow;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int shadowOffset = drawShadow ? 4 : 0;

            if (drawShadow) {
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fill(new RoundRectangle2D.Float(2, shadowOffset, width - 4, height - shadowOffset - 2,
                        cornerRadius, cornerRadius));
            }

            RoundRectangle2D.Float shape = new RoundRectangle2D.Float(
                    0, 0, width - 1, height - 1 - (drawShadow ? 2 : 0), cornerRadius, cornerRadius);
            g2.setColor(surfaceColor);
            g2.fill(shape);

            g2.setColor(borderColor);
            g2.draw(shape);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}