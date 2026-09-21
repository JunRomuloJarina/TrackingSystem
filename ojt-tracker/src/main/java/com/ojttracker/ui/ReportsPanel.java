package com.ojttracker.ui;

import com.ojttracker.components.GlassPanel;
import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.Theme;
import com.ojttracker.model.OJTRecord;
import com.ojttracker.model.Student;
import com.ojttracker.service.DashboardService;
import com.ojttracker.service.OJTRecordService;
import com.ojttracker.service.StudentService;
import com.ojttracker.util.DateUtils;
import com.ojttracker.util.PrintUtils;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Optional;

/**
 * Reports screen: shows a preview of the official OJT attendance report
 * (student info, hour totals, full attendance table) and lets the
 * student print it.
 */
public class ReportsPanel extends JPanel {

    private final StudentService studentService;
    private final OJTRecordService recordService;
    private final DashboardService dashboardService;

    private JLabel studentInfoLabel;
    private JLabel hoursSummaryLabel;
    private DefaultTableModel tableModel;

    public ReportsPanel(StudentService studentService, OJTRecordService recordService,
                         DashboardService dashboardService) {
        this.studentService = studentService;
        this.recordService = recordService;
        this.dashboardService = dashboardService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        build();
    }

    private void build() {
        JLabel title = new JLabel("Reports");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        RoundedButton printButton = new RoundedButton("🖨  Print Report", RoundedButton.Style.PRIMARY);
        printButton.addActionListener(e -> printReport());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(printButton, BorderLayout.EAST);

        GlassPanel infoCard = new GlassPanel();
        infoCard.setLayout(new BorderLayout());
        infoCard.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel reportTitle = new JLabel("OJT ATTENDANCE REPORT", SwingConstants.CENTER);
        reportTitle.setFont(Theme.FONT_HEADING);
        reportTitle.setForeground(Theme.TEXT_PRIMARY);

        studentInfoLabel = new JLabel();
        studentInfoLabel.setFont(Theme.FONT_BODY);
        studentInfoLabel.setForeground(Theme.TEXT_SECONDARY);

        hoursSummaryLabel = new JLabel();
        hoursSummaryLabel.setFont(Theme.FONT_SUBHEADING);
        hoursSummaryLabel.setForeground(Theme.PRIMARY_ACCENT);

        JPanel infoText = new JPanel();
        infoText.setOpaque(false);
        infoText.setLayout(new javax.swing.BoxLayout(infoText, javax.swing.BoxLayout.Y_AXIS));
        infoText.add(reportTitle);
        infoText.add(javax.swing.Box.createVerticalStrut(12));
        infoText.add(studentInfoLabel);
        infoText.add(javax.swing.Box.createVerticalStrut(8));
        infoText.add(hoursSummaryLabel);
        infoCard.add(infoText, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(
                new Object[]{"Date", "Time In", "Time Out", "Break", "Hours", "Remarks"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        com.ojttracker.components.ModernTable table = new com.ojttracker.components.ModernTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        GlassPanel tableCard = new GlassPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(infoCard, BorderLayout.NORTH);
        body.add(tableCard, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public void refresh() {
        Optional<Student> studentOpt = studentService.getStudent();
        if (studentOpt.isEmpty()) {
            studentInfoLabel.setText("<html>No student profile set up yet. Go to the Student page first.</html>");
            hoursSummaryLabel.setText("");
            tableModel.setRowCount(0);
            return;
        }
        Student student = studentOpt.get();
        DashboardService.Snapshot snapshot = dashboardService.buildSnapshot();

        studentInfoLabel.setText(String.format(
                "<html>Student: %s &nbsp;|&nbsp; Course: %s<br>OJT Company: %s &nbsp;|&nbsp; Supervisor: %s</html>",
                safe(student.getFullName()), safe(student.getCourse()),
                safe(student.getOjtCompany()), safe(student.getSupervisor())));

        hoursSummaryLabel.setText(String.format(
                "Required: %.1f hrs   •   Completed: %.1f hrs   •   Remaining: %.1f hrs   •   Progress: %.1f%%",
                snapshot.requiredHours(), snapshot.completedHours(), snapshot.remainingHours(),
                snapshot.progressPercent()));

        List<OJTRecord> records = recordService.getAllRecords(student.getId());
        tableModel.setRowCount(0);
        for (OJTRecord r : records) {
            tableModel.addRow(new Object[]{
                    DateUtils.formatDate(r.getWorkDate()),
                    DateUtils.formatTime(r.getTimeIn()),
                    DateUtils.formatTime(r.getTimeOut()),
                    r.getBreakHours(),
                    r.getTotalHours(),
                    r.getRemarks()
            });
        }
    }

    private void printReport() {
        Optional<Student> studentOpt = studentService.getStudent();
        if (studentOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please set up your Student profile first.",
                    "Student Profile Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Student student = studentOpt.get();
        DashboardService.Snapshot snapshot = dashboardService.buildSnapshot();
        List<OJTRecord> records = recordService.getAllRecords(student.getId());
        PrintUtils.printReport(student, snapshot, records);
    }

    private String safe(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }
}