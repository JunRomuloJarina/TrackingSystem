package com.ojttracker.ui;

import com.ojttracker.components.GlassPanel;
import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.Theme;
import com.ojttracker.config.DatabaseConfig;
import com.ojttracker.dao.OJTSettingsDAO;
import com.ojttracker.model.OJTSettings;
import com.ojttracker.model.Student;
import com.ojttracker.service.StudentService;
import com.ojttracker.util.ValidationUtils;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Optional;

/**
 * Application settings: the configurable OJT hour requirement (spec
 * explicitly forbids hardcoding it) plus default daily hours/break, and
 * read-only application/database information.
 */
public class SettingsPanel extends JPanel {

    private final StudentService studentService;
    private final OJTSettingsDAO settingsDAO;
    private final Runnable onSaved;

    private final JTextField requiredHoursField = FormFields.numberField();
    private final JTextField standardDailyHoursField = FormFields.numberField();
    private final JTextField defaultBreakField = FormFields.numberField();

    public SettingsPanel(StudentService studentService, OJTSettingsDAO settingsDAO, Runnable onSaved) {
        this.studentService = studentService;
        this.settingsDAO = settingsDAO;
        this.onSaved = onSaved;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        build();
    }

    private void build() {
        JLabel title = new JLabel("Settings");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        GlassPanel configCard = new GlassPanel();
        configCard.setLayout(new GridBagLayout());
        configCard.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        JLabel sectionLabel = new JLabel("OJT CONFIGURATION");
        sectionLabel.setFont(Theme.FONT_LABEL);
        sectionLabel.setForeground(Theme.TEXT_SECONDARY);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        configCard.add(sectionLabel, gbc);
        gbc.gridwidth = 1;

        row = FormFields.addRow(configCard, gbc, row, "Required OJT Hours", requiredHoursField);
        row = FormFields.addRow(configCard, gbc, row, "Standard Daily Hours", standardDailyHoursField);
        row = FormFields.addRow(configCard, gbc, row, "Default Break Duration (hrs)", defaultBreakField);

        RoundedButton save = new RoundedButton("Save Settings", RoundedButton.Style.PRIMARY);
        save.addActionListener(e -> save());
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonRow.setOpaque(false);
        buttonRow.add(save);
        GridBagConstraints buttonGbc = (GridBagConstraints) gbc.clone();
        buttonGbc.gridx = 0;
        buttonGbc.gridy = row;
        buttonGbc.gridwidth = 2;
        buttonGbc.insets = new Insets(20, 8, 8, 8);
        configCard.add(buttonRow, buttonGbc);

        GlassPanel infoCard = new GlassPanel();
        infoCard.setLayout(new GridLayout(0, 1, 0, 10));
        infoCard.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel appInfoTitle = new JLabel("APPLICATION INFORMATION");
        appInfoTitle.setFont(Theme.FONT_LABEL);
        appInfoTitle.setForeground(Theme.TEXT_SECONDARY);
        JLabel appVersion = infoLine("Version", "1.0.0");
        JLabel dbInfoTitle = new JLabel("DATABASE INFORMATION");
        dbInfoTitle.setFont(Theme.FONT_LABEL);
        dbInfoTitle.setForeground(Theme.TEXT_SECONDARY);
        JLabel dbPath = infoLine("Database file", DatabaseConfig.getDatabasePath());
        JLabel dbEngine = infoLine("Engine", "SQLite (offline, local storage)");

        infoCard.add(appInfoTitle);
        infoCard.add(appVersion);
        infoCard.add(dbInfoTitle);
        infoCard.add(dbPath);
        infoCard.add(dbEngine);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.add(configCard);
        body.add(javax.swing.Box.createVerticalStrut(16));
        body.add(infoCard);

        add(title, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    private JLabel infoLine(String label, String value) {
        JLabel l = new JLabel("<html><b style='color:#F8FAFC'>" + label + ":</b> "
                + "<span style='color:#94A3B8'>" + value + "</span></html>");
        l.setFont(Theme.FONT_BODY);
        return l;
    }

    public void refresh() {
        Optional<Student> studentOpt = studentService.getStudent();
        studentOpt.ifPresent(s -> requiredHoursField.setText(String.valueOf(s.getRequiredHours())));
        standardDailyHoursField.setText(settingsDAO.getOrDefault(OJTSettings.KEY_STANDARD_DAILY_HOURS, "8.0"));
        defaultBreakField.setText(settingsDAO.getOrDefault(OJTSettings.KEY_DEFAULT_BREAK_HOURS, "1.0"));
    }

    private void save() {
        try {
            double requiredHours = Double.parseDouble(requiredHoursField.getText().trim());
            double standardDaily = Double.parseDouble(standardDailyHoursField.getText().trim());
            double defaultBreak = Double.parseDouble(defaultBreakField.getText().trim());
            ValidationUtils.requirePositive(requiredHours, "Required OJT Hours");
            ValidationUtils.requirePositive(standardDaily, "Standard Daily Hours");
            ValidationUtils.requireNonNegative(defaultBreak, "Default Break Duration");

            Optional<Student> studentOpt = studentService.getStudent();
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                student.setRequiredHours(requiredHours);
                studentService.saveOrUpdate(student);
            }
            settingsDAO.set(OJTSettings.KEY_STANDARD_DAILY_HOURS, String.valueOf(standardDaily));
            settingsDAO.set(OJTSettings.KEY_DEFAULT_BREAK_HOURS, String.valueOf(defaultBreak));

            JOptionPane.showMessageDialog(this, "Settings saved. The dashboard will update automatically.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            onSaved.run();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for all fields.", "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (ValidationUtils.ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Please check your input",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}