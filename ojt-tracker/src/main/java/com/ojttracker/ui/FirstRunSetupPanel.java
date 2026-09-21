package com.ojttracker.ui;

import com.ojttracker.components.GlassPanel;
import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.Theme;
import com.ojttracker.model.Student;
import com.ojttracker.service.StudentService;
import com.ojttracker.util.ValidationUtils;

import com.ojttracker.util.DateUtils;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Shown once on first launch: collects the student profile and OJT
 * configuration before the app moves on to the Dashboard, per the
 * "First Launch Experience" spec section.
 */
public class FirstRunSetupPanel extends JPanel {

    private final StudentService studentService;
    private final Runnable onComplete;

    private final JTextField fullNameField = FormFields.textField();
    private final JTextField studentIdField = FormFields.textField();
    private final JTextField courseField = FormFields.textField();
    private final JTextField yearLevelField = FormFields.textField();
    private final JTextField schoolField = FormFields.textField();
    private final JTextField companyField = FormFields.textField();
    private final JTextField requiredHoursField = FormFields.numberField();
    private final JComboBox<LocalDate> startDateField = FormFields.dateComboBox(
            DateUtils.buildDateOptions(LocalDate.now().minusDays(30), 90), LocalDate.now());
    private final JComboBox<LocalDate> endDateField = FormFields.dateComboBox(
            DateUtils.buildDateOptions(LocalDate.now(), 180), LocalDate.now().plusDays(30));

    public FirstRunSetupPanel(StudentService studentService, Runnable onComplete) {
        this.studentService = studentService;
        this.onComplete = onComplete;
        setOpaque(false);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome to OJT Tracker");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Let's set up your OJT information.");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 24, 0));

        GlassPanel formCard = new GlassPanel();
        formCard.setLayout(new java.awt.GridBagLayout());
        formCard.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        formCard.setMaximumSize(new Dimension(640, 620));
        formCard.setPreferredSize(new Dimension(640, 600));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(8, 8, 8, 8);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        int row = 0;

        row = FormFields.addRow(formCard, gbc, row, "Full Name *", fullNameField);
        row = FormFields.addRow(formCard, gbc, row, "Student ID", studentIdField);
        row = FormFields.addRow(formCard, gbc, row, "Course", courseField);
        row = FormFields.addRow(formCard, gbc, row, "Year Level", yearLevelField);
        row = FormFields.addRow(formCard, gbc, row, "School", schoolField);
        row = FormFields.addRow(formCard, gbc, row, "OJT Company", companyField);
        row = FormFields.addRow(formCard, gbc, row, "Required OJT Hours *", requiredHoursField);
        row = FormFields.addRow(formCard, gbc, row, "OJT Start Date", startDateField);
        row = FormFields.addRow(formCard, gbc, row, "Expected End Date", endDateField);

        RoundedButton getStarted = new RoundedButton("Get Started →", RoundedButton.Style.PRIMARY);
        getStarted.addActionListener(e -> save());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonRow.setOpaque(false);
        buttonRow.add(getStarted);

        java.awt.GridBagConstraints buttonGbc = (java.awt.GridBagConstraints) gbc.clone();
        buttonGbc.gridx = 0;
        buttonGbc.gridy = row;
        buttonGbc.gridwidth = 2;
        buttonGbc.insets = new java.awt.Insets(20, 8, 8, 8);
        formCard.add(buttonRow, buttonGbc);

        center.add(Box.createVerticalGlue());
        center.add(title);
        center.add(subtitle);
        formCard.setAlignmentX(CENTER_ALIGNMENT);
        center.add(formCard);
        center.add(Box.createVerticalGlue());

        add(center, BorderLayout.CENTER);
    }

    private void save() {
        try {
            Student student = new Student();
            student.setFullName(fullNameField.getText().trim());
            student.setStudentId(studentIdField.getText().trim());
            student.setCourse(courseField.getText().trim());
            student.setYearLevel(yearLevelField.getText().trim());
            student.setSchool(schoolField.getText().trim());
            student.setOjtCompany(companyField.getText().trim());
            student.setRequiredHours(parseHours(requiredHoursField.getText()));
            student.setOjtStartDate(FormFields.readDateValue(startDateField.getSelectedItem()));
            student.setExpectedEndDate(FormFields.readDateValue(endDateField.getSelectedItem()));

            studentService.saveOrUpdate(student);
            onComplete.run();
        } catch (ValidationUtils.ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Please check your input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Required OJT Hours must be a number.", "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Dates must be in yyyy-MM-dd format (e.g. 2026-06-01).",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
        }
    }

    private double parseHours(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationUtils.ValidationException("Required OJT Hours cannot be empty.");
        }
        return Double.parseDouble(text.trim());
    }

}