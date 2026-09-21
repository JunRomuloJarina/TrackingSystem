package com.ojttracker.ui;

import com.ojttracker.components.Theme;

import com.ojttracker.util.DateUtils;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Small helpers shared by the setup, student and record forms so every
 * label/field pair looks consistent without duplicating styling code.
 */
final class FormFields {

    private FormFields() {
    }

    static JTextField textField() {
        return textField(null);
    }

    static JTextField textField(String placeholderHint) {
        JTextField field = new JTextField();
        field.setFont(Theme.FONT_BODY);
        field.setForeground(Color.BLACK);
        field.setOpaque(true);
        field.setBackground(Color.WHITE);
        field.setCaretColor(Color.BLACK);
        field.setSelectionColor(new Color(56, 189, 248, 180));
        field.setSelectedTextColor(Color.BLACK);
        field.setMargin(new java.awt.Insets(0, 0, 0, 0));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GLASS_BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)));
        field.setMinimumSize(new Dimension(240, 40));
        field.setPreferredSize(new Dimension(240, 40));
        if (placeholderHint != null) {
            field.setToolTipText(placeholderHint);
        }
        return field;
    }

    static JTextField numberField() {
        JTextField field = textField("numbers only");
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass bypass, int offset, String text, AttributeSet attributes)
                    throws BadLocationException {
                if (isNumericInput(bypass.getDocument().getText(0, bypass.getDocument().getLength()), text, offset)) {
                    bypass.insertString(offset, text, attributes);
                }
            }

            @Override
            public void replace(FilterBypass bypass, int offset, int length, String text, AttributeSet attributes)
                    throws BadLocationException {
                String current = bypass.getDocument().getText(0, bypass.getDocument().getLength());
                String before = current.substring(0, offset);
                String after = current.substring(offset + length);
                if (isNumericInput(before + after, text, before.length())) {
                    bypass.replace(offset, length, text, attributes);
                }
            }

            private boolean isNumericInput(String current, String replacement, int offset) {
                String candidate = current.substring(0, offset) + replacement + current.substring(offset);
                return candidate.matches("\\d*(\\.\\d*)?");
            }
        });
        return field;
    }

    static JComboBox<LocalDate> dateComboBox(List<LocalDate> options, LocalDate selected) {
        JComboBox<LocalDate> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        comboBox.setFont(Theme.FONT_BODY);
        comboBox.setBackground(new Color(12, 18, 28, 220));
        comboBox.setForeground(Color.BLACK);
        comboBox.setOpaque(true);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GLASS_BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)));
        comboBox.setMinimumSize(new Dimension(240, 40));
        comboBox.setPreferredSize(new Dimension(240, 40));
        if (comboBox.getEditor().getEditorComponent() instanceof JTextField editor) {
            editor.setFont(Theme.FONT_BODY);
            editor.setForeground(Color.BLACK);
            editor.setCaretColor(Color.BLACK);
            editor.setBackground(Color.WHITE);
            editor.setMargin(new java.awt.Insets(0, 0, 0, 0));
            editor.setBorder(new EmptyBorder(2, 8, 2, 8));
        }
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setFont(Theme.FONT_BODY);
            label.setForeground(Color.BLACK);
            label.setBackground(isSelected ? new Color(230, 240, 255) : new Color(255, 255, 255));
            label.setBorder(new EmptyBorder(4, 8, 4, 8));
            if (value instanceof LocalDate) {
                label.setText(DateUtils.formatDate((LocalDate) value));
            } else {
                label.setText(String.valueOf(value));
            }
            return label;
        });
        for (LocalDate date : options) {
            comboBox.addItem(date);
        }
        if (selected != null) {
            comboBox.setSelectedItem(selected);
        }
        return comboBox;
    }

    static LocalDate readDateValue(Object value) throws DateTimeParseException {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return LocalDate.parse(value.toString().trim());
    }

    static JComboBox<LocalTime> timeComboBox(List<LocalTime> options, LocalTime selected) {
        JComboBox<LocalTime> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        comboBox.setFont(Theme.FONT_BODY);
        comboBox.setBackground(new Color(12, 18, 28, 220));
        comboBox.setForeground(Color.BLACK);
        comboBox.setOpaque(true);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GLASS_BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)));
        comboBox.setMinimumSize(new Dimension(240, 40));
        comboBox.setPreferredSize(new Dimension(240, 40));
        if (comboBox.getEditor().getEditorComponent() instanceof JTextField) {
            JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
            editor.setFont(Theme.FONT_BODY);
            editor.setForeground(Color.BLACK);
            editor.setCaretColor(Color.BLACK);
            editor.setBackground(Color.WHITE);
            editor.setMargin(new java.awt.Insets(0, 0, 0, 0));
            editor.setBorder(new EmptyBorder(2, 8, 2, 8));
        }
        comboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setFont(Theme.FONT_BODY);
            label.setForeground(Color.BLACK);
            label.setBackground(isSelected ? new Color(230, 240, 255) : new Color(255, 255, 255));
            label.setBorder(new EmptyBorder(4, 8, 4, 8));
            if (value instanceof LocalTime) {
                label.setText(DateUtils.formatTime((LocalTime) value));
            } else {
                label.setText(String.valueOf(value));
            }
            return label;
        });
        for (LocalTime time : options) {
            comboBox.addItem(time);
        }
        if (selected != null) {
            comboBox.setSelectedItem(selected);
        }
        return comboBox;
    }

    /** Adds a "Label" / field row to a form using GridBagLayout, returning the next free row index. */
    static int addRow(JPanel form, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_SUBHEADING);
        label.setForeground(Theme.TEXT_SECONDARY);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        form.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        form.add(field, gbc);

        return row + 1;
    }

    static int addRow(JPanel form, GridBagConstraints gbc, int row, String labelText, JComboBox<?> comboBox) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_SUBHEADING);
        label.setForeground(Theme.TEXT_SECONDARY);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        form.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        form.add(comboBox, gbc);

        return row + 1;
    }
}