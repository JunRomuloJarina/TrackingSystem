package com.ojttracker.components;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Reusable dashboard statistic card: a label, a large value, and an
 * optional caption line (e.g. "64.3% of required hours").
 */
public class StatCard extends GlassPanel {

    private final JLabel valueLabel;
    private final JLabel captionLabel;

    public StatCard(String title, String value, String caption, Color accent) {
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        setPreferredSize(new Dimension(220, 120));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.FONT_LABEL);
        titleLabel.setForeground(Theme.TEXT_SECONDARY);

        valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.FONT_STAT_VALUE);
        valueLabel.setForeground(accent != null ? accent : Theme.TEXT_PRIMARY);

        captionLabel = new JLabel(caption == null ? " " : caption);
        captionLabel.setFont(Theme.FONT_SMALL);
        captionLabel.setForeground(Theme.TEXT_SECONDARY);
        captionLabel.setHorizontalAlignment(SwingConstants.LEFT);

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        add(captionLabel, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setCaption(String caption) {
        captionLabel.setText(caption);
    }
}