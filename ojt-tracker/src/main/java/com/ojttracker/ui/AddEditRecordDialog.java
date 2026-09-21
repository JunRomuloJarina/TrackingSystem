package com.ojttracker.ui;

import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.Theme;
import com.ojttracker.model.OJTRecord;
import com.ojttracker.service.OJTRecordService;
import com.ojttracker.util.DateUtils;
import com.ojttracker.util.ValidationUtils;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Modal dialog for adding or editing a single OJT attendance record.
 * Total hours are always computed automatically; the user never types
 * them in directly (spec section 11).
 */
public class AddEditRecordDialog extends JDialog {

    private final OJTRecordService recordService;
    private final int studentId;
    private final OJTRecord existingRecord; // null when adding
    private final Runnable onSaved;

    private final JComboBox<LocalDate> dateField = FormFields.dateComboBox(
            DateUtils.buildDateOptions(LocalDate.now().minusDays(30), 90), LocalDate.now());
    private final JComboBox<LocalTime> timeInField = FormFields.timeComboBox(
            DateUtils.buildTimeOptions(30), LocalTime.of(8, 0));
    private final JComboBox<LocalTime> timeOutField = FormFields.timeComboBox(
            DateUtils.buildTimeOptions(30), LocalTime.of(17, 0));
    private final JTextField breakField = FormFields.textField("hours, e.g. 1.0");
    private final JTextField remarksField = FormFields.textField();

    public AddEditRecordDialog(Frame owner, OJTRecordService recordService, int studentId,
                                OJTRecord existingRecord, Runnable onSaved) {
        super(owner, existingRecord == null ? "Add OJT Record" : "Edit OJT Record", true);
        this.recordService = recordService;
        this.studentId = studentId;
        this.existingRecord = existingRecord;
        this.onSaved = onSaved;

        getContentPane().setBackground(Theme.SECONDARY);
        setLayout(new java.awt.BorderLayout());
        build();
        prefill();
        pack();
        setLocationRelativeTo(owner);
    }

    private void build() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SECONDARY);
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        row = FormFields.addRow(form, gbc, row, "Date *", dateField);
        row = FormFields.addRow(form, gbc, row, "Time In *", timeInField);
        row = FormFields.addRow(form, gbc, row, "Time Out *", timeOutField);
        row = FormFields.addRow(form, gbc, row, "Break (hours)", breakField);
        row = FormFields.addRow(form, gbc, row, "Remarks", remarksField);

        RoundedButton cancel = new RoundedButton("Cancel", RoundedButton.Style.SECONDARY);
        cancel.addActionListener(e -> dispose());
        RoundedButton save = new RoundedButton("Save Record", RoundedButton.Style.PRIMARY);
        save.addActionListener(e -> save());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonRow.setBackground(Theme.SECONDARY);
        buttonRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 24));
        buttonRow.add(cancel);
        buttonRow.add(save);

        add(form, java.awt.BorderLayout.CENTER);
        add(buttonRow, java.awt.BorderLayout.SOUTH);
        setMinimumSize(new java.awt.Dimension(460, 360));
    }

    private void prefill() {
        if (existingRecord != null) {
            dateField.setSelectedItem(existingRecord.getWorkDate());
            setTimeValue(timeInField, existingRecord.getTimeIn());
            setTimeValue(timeOutField, existingRecord.getTimeOut());
            breakField.setText(String.valueOf(existingRecord.getBreakHours()));
            remarksField.setText(existingRecord.getRemarks());
        } else {
            dateField.setSelectedItem(LocalDate.now());
            breakField.setText("1.0");
        }
    }

    private void save() {
        try {
            LocalDate date = FormFields.readDateValue(dateField.getSelectedItem());
            LocalTime timeIn = readTimeValue(timeInField);
            LocalTime timeOut = readTimeValue(timeOutField);
            if (date == null || timeIn == null || timeOut == null) {
                throw new ValidationUtils.ValidationException(
                        "Please select valid date and time values.");
            }
            double breakHours = breakField.getText().isBlank() ? 0.0 : Double.parseDouble(breakField.getText().trim());
            String remarks = remarksField.getText().trim();

            if (existingRecord == null) {
                recordService.addRecord(studentId, date, timeIn, timeOut, breakHours, remarks);
            } else {
                recordService.updateRecord(existingRecord.getId(), studentId, date, timeIn, timeOut,
                        breakHours, remarks);
            }
            onSaved.run();
            dispose();
        } catch (ValidationUtils.ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Please check your input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please enter the date as yyyy-MM-dd (e.g. 2026-08-26).",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Break duration must be a number.", "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void setTimeValue(JComboBox<LocalTime> field, LocalTime value) {
        field.getEditor().setItem(DateUtils.formatTime(value));
    }

    private LocalTime readTimeValue(JComboBox<LocalTime> field) {
        Object value = field.getEditor().getItem();
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        }
        return DateUtils.parseTimeFlexible(String.valueOf(value));
    }
}