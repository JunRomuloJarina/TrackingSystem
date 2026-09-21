package com.ojttracker.ui;

import com.ojttracker.components.Theme;
import org.junit.jupiter.api.Test;

import javax.swing.JTextField;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

class FormFieldsTest {

    @Test
    void textField_shouldUseReadableWhiteBackgroundAndVisibleBlackText() {
        JTextField field = FormFields.textField("example");

        assertEquals(Color.BLACK, field.getForeground());
        assertEquals(Color.BLACK, field.getCaretColor());
        assertEquals(Color.WHITE, field.getBackground());
        assertNotNull(field.getToolTipText());
        assertTrue(field.isOpaque());
        assertEquals(40, field.getPreferredSize().height);
        assertEquals(0, field.getMargin().top);
        assertEquals(6, field.getBorder().getBorderInsets(field).top);
    }

    @Test
    void numberField_shouldRejectLettersAndAllowDecimalHours() {
        JTextField field = FormFields.numberField();

        field.setText("12.5");
        field.replaceSelection("x");

        assertEquals("12.5", field.getText());
    }
}
