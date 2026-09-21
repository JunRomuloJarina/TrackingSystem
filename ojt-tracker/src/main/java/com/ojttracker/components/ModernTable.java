package com.ojttracker.components;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * A {@link JTable} styled to match the glassmorphism theme: dark rows
 * with subtle striping, a bold translucent header, and an accent-colored
 * selection highlight, used for both the dashboard "recent records" list
 * and the full OJT Records page.
 */
public class ModernTable extends JTable {

    public ModernTable(TableModel model) {
        super(model);
        applyStyle();
    }

    private void applyStyle() {
        setRowHeight(34);
        setShowGrid(false);
        setIntercellSpacing(new java.awt.Dimension(0, 0));
        setBackground(Theme.SECONDARY);
        setForeground(Theme.TEXT_PRIMARY);
        setSelectionBackground(new Color(Theme.PRIMARY_ACCENT.getRed(), Theme.PRIMARY_ACCENT.getGreen(),
                Theme.PRIMARY_ACCENT.getBlue(), 60));
        setSelectionForeground(Theme.TEXT_PRIMARY);
        setFont(Theme.FONT_BODY);
        setFillsViewportHeight(true);

        JTableHeader header = getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new java.awt.Dimension(0, 36));
        header.setReorderingAllowed(false);

        setDefaultRenderer(Object.class, new StripedCellRenderer());
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setOpaque(true);
            label.setBackground(Theme.SIDEBAR);
            label.setForeground(Theme.TEXT_SECONDARY);
            label.setFont(Theme.FONT_LABEL);
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));
            return label;
        }
    }

    private static class StripedCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12));
            label.setFont(Theme.FONT_BODY.deriveFont(Font.PLAIN, 13f));
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            } else {
                label.setOpaque(true);
                label.setBackground(row % 2 == 0 ? Theme.SECONDARY : new Color(255, 255, 255, 6));
                label.setForeground(Theme.TEXT_PRIMARY);
            }
            return label;
        }
    }

    static {
        UIManager.put("Table.foreground", Theme.TEXT_PRIMARY);
    }
}