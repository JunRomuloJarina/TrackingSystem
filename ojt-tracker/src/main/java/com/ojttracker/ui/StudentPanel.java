package com.ojttracker.ui;

import com.ojttracker.components.GlassPanel;
import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.Theme;
import com.ojttracker.model.Student;
import com.ojttracker.service.StudentService;
import com.ojttracker.util.ValidationUtils;

import com.ojttracker.util.DateUtils;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Student / OJT placement information screen. Also acts as the "edit
 * profile" screen after first-run setup.
 */
public class StudentPanel extends JPanel {

    private final StudentService studentService;
    private final Runnable onSaved;

    private final JTextField fullNameField = FormFields.textField();
    private final JTextField studentIdField = FormFields.textField();
    private final JTextField courseField = FormFields.textField();
    private final JTextField yearLevelField = FormFields.textField();
    private final JTextField schoolField = FormFields.textField();
    private final JTextField companyField = FormFields.textField();
    private final JTextField companyAddressField = FormFields.textField();
    private final JTextField supervisorField = FormFields.textField();
    private final JTextField supervisorContactField = FormFields.textField();
    private final JTextField requiredHoursField = FormFields.numberField();
    private final JComboBox<LocalDate> startDateField = FormFields.dateComboBox(
            DateUtils.buildDateOptions(LocalDate.now().minusDays(30), 90), LocalDate.now());
    private final JComboBox<LocalDate> endDateField = FormFields.dateComboBox(
            DateUtils.buildDateOptions(LocalDate.now(), 180), LocalDate.now().plusDays(30));
    private final JComboBox<Student> studentSelector = new JComboBox<>();

    public StudentPanel(StudentService studentService, Runnable onSaved) {
        this.studentService = studentService;
        this.onSaved = onSaved;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        build();
    }

    private void build() {
        JLabel title = new JLabel("Student Information");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(title, BorderLayout.WEST);

        JPanel selectorActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        selectorActions.setOpaque(false);

        RoundedButton addStudent = new RoundedButton("Add New Student", RoundedButton.Style.SECONDARY);
        addStudent.addActionListener(e -> createNewStudentProfile());
        RoundedButton deleteStudent = new RoundedButton("Delete Student", RoundedButton.Style.DANGER);
        deleteStudent.addActionListener(e -> deleteSelectedStudent());

        selectorActions.add(studentSelector);
        selectorActions.add(addStudent);
        selectorActions.add(deleteStudent);
        titleRow.add(selectorActions, BorderLayout.EAST);

        GlassPanel formCard = new GlassPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        row = FormFields.addRow(formCard, gbc, row, "Full Name *", fullNameField);
        row = FormFields.addRow(formCard, gbc, row, "Student ID", studentIdField);
        row = FormFields.addRow(formCard, gbc, row, "Course", courseField);
        row = FormFields.addRow(formCard, gbc, row, "Year Level", yearLevelField);
        row = FormFields.addRow(formCard, gbc, row, "School", schoolField);
        row = FormFields.addRow(formCard, gbc, row, "OJT Company", companyField);
        row = FormFields.addRow(formCard, gbc, row, "Company Address", companyAddressField);
        row = FormFields.addRow(formCard, gbc, row, "Supervisor", supervisorField);
        row = FormFields.addRow(formCard, gbc, row, "Supervisor Contact", supervisorContactField);
        row = FormFields.addRow(formCard, gbc, row, "Required OJT Hours *", requiredHoursField);
        row = FormFields.addRow(formCard, gbc, row, "OJT Start Date", startDateField);
        row = FormFields.addRow(formCard, gbc, row, "Expected End Date", endDateField);

        RoundedButton save = new RoundedButton("Save Changes", RoundedButton.Style.PRIMARY);
        save.addActionListener(e -> save());
        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.setOpaque(false);
        buttonRow.add(save, BorderLayout.EAST);

        GridBagConstraints buttonGbc = (GridBagConstraints) gbc.clone();
        buttonGbc.gridx = 0;
        buttonGbc.gridy = row;
        buttonGbc.gridwidth = 2;
        buttonGbc.weightx = 1.0;
        buttonGbc.fill = GridBagConstraints.HORIZONTAL;
        buttonGbc.insets = new Insets(20, 8, 8, 8);
        formCard.add(buttonRow, buttonGbc);

        studentSelector.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list,
                                                                    Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student student) {
                    setText(student.getFullName() == null || student.getFullName().isBlank()
                            ? "New Student" : student.getFullName());
                }
                return this;
            }
        });
        studentSelector.addActionListener(e -> {
            Student selected = (Student) studentSelector.getSelectedItem();
            if (selected != null) {
                studentService.setActiveStudent(selected.getId());
                loadStudent(selected);
            }
        });

        add(titleRow, BorderLayout.NORTH);
        add(formCard, BorderLayout.CENTER);
    }

    /** Reloads the current student profile (if any) into the form. */
    public void refresh() {
        List<Student> students = studentService.getStudents();
        Student selected = (Student) studentSelector.getSelectedItem();
        int selectedId = selected == null ? -1 : selected.getId();

        DefaultComboBoxModel<Student> model = new DefaultComboBoxModel<>();
        for (Student student : students) {
            model.addElement(student);
        }
        studentSelector.setModel(model);

        if (students.isEmpty()) {
            clearForm();
            return;
        }

        if (selectedId > 0) {
            for (int i = 0; i < students.size(); i++) {
                if (students.get(i).getId() == selectedId) {
                    studentSelector.setSelectedIndex(i);
                    loadStudent(students.get(i));
                    return;
                }
            }
        }

        Student activeStudent = studentService.getStudent().orElse(students.get(0));
        studentService.setActiveStudent(activeStudent.getId());
        studentSelector.setSelectedItem(activeStudent);
        loadStudent(activeStudent);
    }

    private void createNewStudentProfile() {
        studentSelector.setSelectedItem(null);
        clearForm();
        studentService.setActiveStudent(-1);
    }

    private void deleteSelectedStudent() {
        Student selected = (Student) studentSelector.getSelectedItem();
        if (selected == null) {
            return;
        }
        int option = JOptionPane.showConfirmDialog(this,
                "Delete student profile for " + selected.getFullName() + "?\nThis will also remove their saved OJT records.",
                "Delete Student", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            studentService.deleteStudent(selected.getId());
            refresh();
        }
    }

    private void clearForm() {
        fullNameField.setText("");
        studentIdField.setText("");
        courseField.setText("");
        yearLevelField.setText("");
        schoolField.setText("");
        companyField.setText("");
        companyAddressField.setText("");
        supervisorField.setText("");
        supervisorContactField.setText("");
        requiredHoursField.setText("");
        startDateField.setSelectedItem(LocalDate.now());
        endDateField.setSelectedItem(LocalDate.now().plusDays(30));
    }

    private void loadStudent(Student student) {
        if (student == null) {
            clearForm();
            return;
        }
        fullNameField.setText(student.getFullName());
        studentIdField.setText(student.getStudentId());
        courseField.setText(student.getCourse());
        yearLevelField.setText(student.getYearLevel());
        schoolField.setText(student.getSchool());
        companyField.setText(student.getOjtCompany());
        companyAddressField.setText(student.getCompanyAddress());
        supervisorField.setText(student.getSupervisor());
        supervisorContactField.setText(student.getSupervisorContact());
        requiredHoursField.setText(String.valueOf(student.getRequiredHours()));
        startDateField.setSelectedItem(student.getOjtStartDate() == null ? LocalDate.now() : student.getOjtStartDate());
        endDateField.setSelectedItem(student.getExpectedEndDate() == null ? LocalDate.now().plusDays(30) : student.getExpectedEndDate());
    }

    private void save() {
        try {
            Student student = (Student) studentSelector.getSelectedItem();
            if (student == null) {
                student = new Student();
            }
            student.setFullName(fullNameField.getText().trim());
            student.setStudentId(studentIdField.getText().trim());
            student.setCourse(courseField.getText().trim());
            student.setYearLevel(yearLevelField.getText().trim());
            student.setSchool(schoolField.getText().trim());
            student.setOjtCompany(companyField.getText().trim());
            student.setCompanyAddress(companyAddressField.getText().trim());
            student.setSupervisor(supervisorField.getText().trim());
            student.setSupervisorContact(supervisorContactField.getText().trim());
            student.setRequiredHours(parseHours(requiredHoursField.getText()));
            student.setOjtStartDate(FormFields.readDateValue(startDateField.getSelectedItem()));
            student.setExpectedEndDate(FormFields.readDateValue(endDateField.getSelectedItem()));

            studentService.saveOrUpdate(student);
            refresh();
            JOptionPane.showMessageDialog(this, "Student information saved.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            onSaved.run();
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

    private LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return LocalDate.parse(text.trim());
    }
}