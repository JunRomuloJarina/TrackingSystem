package com.ojttracker.components;

import java.awt.Color;
import java.awt.Font;

/**
 * Central palette and typography for the glassmorphism UI, per the
 * design spec. Keeping this in one place means every panel/component
 * stays visually consistent and the theme can be changed in one spot.
 */
public final class Theme {

    private Theme() {
    }

    public static final Color BACKGROUND = new Color(0x0F, 0x17, 0x2A);
    public static final Color SECONDARY = new Color(0x1E, 0x29, 0x3B);
    public static final Color GLASS_SURFACE = new Color(255, 255, 255, 18);
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 35);
    public static final Color PRIMARY_ACCENT = new Color(0x38, 0xBD, 0xF8);
    public static final Color SUCCESS = new Color(0x22, 0xC5, 0x5E);
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);
    public static final Color DANGER = new Color(0xEF, 0x44, 0x44);
    public static final Color TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);
    public static final Color TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);
    public static final Color SIDEBAR = new Color(0x0B, 0x12, 0x22);
    public static final Color SIDEBAR_ACTIVE = new Color(0x1E, 0x40, 0x5A);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_SUBHEADING = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_STAT_VALUE = new Font("SansSerif", Font.BOLD, 30);
    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);

    public static final int RADIUS = 18;
}