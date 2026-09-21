package com.ojttracker.ui;

import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.Theme;
import com.ojttracker.model.OJTRecord;
import com.ojttracker.service.OJTRecordService;
import com.ojttracker.util.DateUtils;
import com.ojttracker.util.ValidationUtils;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
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

/** Dialog for recording a historical attendance day, including afternoon absence. */
public class AddEditRecordDialog extends JDialog {
    private final OJTRecordService recordService;
    private final int studentId;
    private final OJTRecord existingRecord;
    private final Runnable onSaved;

    private final JComboBox<LocalDate> dateField = FormFields.dateComboBox(
            DateUtils.buildDateOptions(LocalDate.now().minusYears(10), 3650), LocalDate.now());
    private final JComboBox<LocalTime> morningInField = FormFields.timeComboBox(
            DateUtils.buildTimeOptions(30), LocalTime.of(8, 0));
    private final JComboBox<LocalTime> morningOutField = FormFields.timeComboBox(
            DateUtils.buildTimeOptions(30), LocalTime.of(12, 0));
    private final JComboBox<LocalTime> afternoonInField = FormFields.timeComboBox(
            DateUtils.buildTimeOptions(30), LocalTime.of(13, 0));
    private final JComboBox<LocalTime> afternoonOutField = FormFields.timeComboBox(
            DateUtils.buildTimeOptions(30), LocalTime.of(17, 0));
    private final JCheckBox afternoonAbsentField = new JCheckBox("Absent in the afternoon");
    private final JTextField breakField = FormFields.textField("hours, e.g. 0.0");
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
        row = FormFields.addRow(form, gbc, row, "Morning Time In *", morningInField);
        row = FormFields.addRow(form, gbc, row, "Morning Time Out *", morningOutField);
        row = FormFields.addRow(form, gbc, row, "Afternoon Time In", afternoonInField);
        row = FormFields.addRow(form, gbc, row, "Afternoon Time Out", afternoonOutField);
        row = FormFields.addRow(form, gbc, row, "Afternoon Status", afternoonAbsentField);
        row = FormFields.addRow(form, gbc, row, "Break (hours)", breakField);
        row = FormFields.addRow(form, gbc, row, "Remarks", remarksField);

        afternoonAbsentField.setOpaque(false);
        afternoonAbsentField.setForeground(Theme.TEXT_SECONDARY);
        afternoonAbsentField.addActionListener(e -> updateAfternoonFields());

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
        setMinimumSize(new java.awt.Dimension(540, 500));
    }

    private void prefill() {
        if (existingRecord == null) {
            breakField.setText("0.0");
            afternoonAbsentField.setSelected(true);
        } else {
            dateField.setSelectedItem(existingRecord.getWorkDate());
            setTimeValue(morningInField, existingRecord.getMorningTimeIn());
            setTimeValue(morningOutField, existingRecord.getMorningTimeOut());
            setTimeValue(afternoonInField, existingRecord.getAfternoonTimeIn());
            setTimeValue(afternoonOutField, existingRecord.getAfternoonTimeOut());
            afternoonAbsentField.setSelected(existingRecord.getAfternoonTimeIn() == null
                    && existingRecord.getAfternoonTimeOut() == null);
            breakField.setText(String.valueOf(existingRecord.getBreakHours()));
            remarksField.setText(existingRecord.getRemarks());
        }
        updateAfternoonFields();
    }

    private void updateAfternoonFields() {
        boolean enabled = !afternoonAbsentField.isSelected();
        afternoonInField.setEnabled(enabled);
        afternoonOutField.setEnabled(enabled);
        if (!enabled) {
            setTimeValue(afternoonInField, null);
            setTimeValue(afternoonOutField, null);
        }
    }

    private void save() {
        try {
            LocalDate date = FormFields.readDateValue(dateField.getSelectedItem());
            LocalTime morningIn = readTimeValue(morningInField);
            LocalTime morningOut = readTimeValue(morningOutField);
            LocalTime afternoonIn = afternoonAbsentField.isSelected() ? null : readTimeValue(afternoonInField);
            LocalTime afternoonOut = afternoonAbsentField.isSelected() ? null : readTimeValue(afternoonOutField);
            if (date == null || morningIn == null || morningOut == null) {
                throw new ValidationUtils.ValidationException("Please enter a valid date and both morning times.");
            }
            double breakHours = breakField.getText().isBlank() ? 0.0
                    : Double.parseDouble(breakField.getText().trim());
            String remarks = remarksField.getText().trim();
            if (afternoonAbsentField.isSelected() && remarks.isBlank()) remarks = "Absent in the afternoon";

            if (existingRecord == null) {
                recordService.addRecord(studentId, date, morningIn, morningOut,
                        afternoonIn, afternoonOut, breakHours, remarks);
            } else {
                recordService.updateRecord(existingRecord.getId(), studentId, date, morningIn, morningOut,
                        afternoonIn, afternoonOut, breakHours, remarks);
            }
            onSaved.run();
            dispose();
        } catch (ValidationUtils.ValidationException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Please check your input", JOptionPane.WARNING_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date or time.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void setTimeValue(JComboBox<LocalTime> field, LocalTime value) {
        field.getEditor().setItem(value == null ? "" : DateUtils.formatTime(value));
    }

    private LocalTime readTimeValue(JComboBox<LocalTime> field) {
        Object value = field.getEditor().getItem();
        if (value == null || value.toString().isBlank()) return null;
        return value instanceof LocalTime ? (LocalTime) value : DateUtils.parseTimeFlexible(value.toString());
    }
}
