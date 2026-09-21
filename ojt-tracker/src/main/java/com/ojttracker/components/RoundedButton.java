package com.ojttracker.components;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * A modern rounded button with hover and press visual feedback, replacing
 * the default Swing button chrome. Supports a "primary" (filled accent)
 * and "secondary" (outlined) style.
 */
public class RoundedButton extends JButton {

    public enum Style { PRIMARY, SECONDARY, DANGER }

    private final Style style;
    private boolean hovered = false;
    private boolean pressed = false;

    public RoundedButton(String text, Style style) {
        super(text);
        this.style = style;
        setFont(Theme.FONT_SUBHEADING);
        setForeground(style == Style.SECONDARY ? Theme.TEXT_PRIMARY : Color.WHITE);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new java.awt.Insets(10, 22, 10, 22));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    public RoundedButton(String text) {
        this(text, Style.PRIMARY);
    }

    private Color baseColor() {
        return switch (style) {
            case PRIMARY -> Theme.PRIMARY_ACCENT;
            case DANGER -> Theme.DANGER;
            case SECONDARY -> new Color(255, 255, 255, 20);
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = baseColor();
            Color fill;
            if (!isEnabled()) {
                fill = new Color(base.getRed(), base.getGreen(), base.getBlue(), 90);
            } else if (pressed) {
                fill = base.darker();
            } else if (hovered) {
                fill = brighten(base);
            } else {
                fill = base;
            }

            RoundRectangle2D.Float shape = new RoundRectangle2D.Float(
                    0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g2.setColor(fill);
            g2.fill(shape);

            if (style == Style.SECONDARY) {
                g2.setColor(Theme.GLASS_BORDER);
                g2.draw(shape);
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    private Color brighten(Color c) {
        int r = Math.min(255, c.getRed() + 20);
        int g = Math.min(255, c.getGreen() + 20);
        int b = Math.min(255, c.getBlue() + 20);
        return new Color(r, g, b, c.getAlpha());
    }
}