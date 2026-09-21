package com.ojttracker.ui;

import com.ojttracker.components.GlassPanel;
import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.StatCard;
import com.ojttracker.components.Theme;
import com.ojttracker.model.OJTRecord;
import com.ojttracker.model.Student;
import com.ojttracker.service.DashboardService;
import com.ojttracker.service.StudentService;
import com.ojttracker.util.DateUtils;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;

/**
 * The main dashboard: statistic cards, an overall progress bar, a weekly
 * hours chart, and a preview of the most recent OJT records.
 */
public class DashboardPanel extends JPanel {

    private final DashboardService dashboardService;
    private final StudentService studentService;
    private final Runnable onViewAllRecords;
    private final JComboBox<Student> studentSelector = new JComboBox<>();
    private boolean refreshing;

    private StatCard requiredCard;
    private StatCard completedCard;
    private StatCard remainingCard;
    private StatCard progressCard;
    private ProgressBarPanel progressBar;
    private WeeklyChartPanel weeklyChart;
    private JLabel statusLabel;
    private JLabel dashboardTitle;
    private DefaultTableModel recentTableModel;
    private com.ojttracker.components.ModernTable recentTable;
    private JLabel emptyStateLabel;
    private JPanel recentCard;
    private final CardLayout recentCardLayout = new CardLayout();
    private JPanel recentCardsContainer;
    private static final String RECENT_TABLE = "TABLE";
    private static final String RECENT_EMPTY = "EMPTY";

    public DashboardPanel(StudentService studentService, DashboardService dashboardService, Runnable onViewAllRecords) {
        this.studentService = studentService;
        this.dashboardService = dashboardService;
        this.onViewAllRecords = onViewAllRecords;
        setOpaque(false);
        setLayout(new BorderLayout(0, 20));
        build();
    }

    private void build() {
        dashboardTitle = new JLabel("Dashboard");
        dashboardTitle.setFont(Theme.FONT_TITLE);
        dashboardTitle.setForeground(Theme.TEXT_PRIMARY);

        studentSelector.setPrototypeDisplayValue(new Student());
        studentSelector.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list,
                                                                    Object value, int index,
                                                                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student student) {
                    setText(student.getFullName() == null || student.getFullName().isBlank()
                            ? "Select student" : student.getFullName());
                }
                return this;
            }
        });
        studentSelector.addActionListener(e -> {
            if (refreshing) {
                return;
            }
            Student selected = (Student) studentSelector.getSelectedItem();
            if (selected != null) {
                studentService.setActiveStudent(selected.getId());
                refresh();
            }
        });

        statusLabel = new JLabel("● NOT STARTED");
        statusLabel.setFont(Theme.FONT_SUBHEADING);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(dashboardTitle, BorderLayout.WEST);
        JPanel rightHeader = new JPanel(new BorderLayout(10, 0));
        rightHeader.setOpaque(false);
        rightHeader.add(studentSelector, BorderLayout.CENTER);
        rightHeader.add(statusLabel, BorderLayout.EAST);
        header.add(rightHeader, BorderLayout.EAST);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        requiredCard = new StatCard("Required Hours", "0.0 hrs", " ", Theme.TEXT_PRIMARY);
        completedCard = new StatCard("Completed", "0.0 hrs", " ", Theme.PRIMARY_ACCENT);
        remainingCard = new StatCard("Remaining", "0.0 hrs", " ", Theme.WARNING);
        progressCard = new StatCard("Progress", "0.0%", " ", Theme.SUCCESS);
        statsRow.add(requiredCard);
        statsRow.add(completedCard);
        statsRow.add(remainingCard);
        statsRow.add(progressCard);

        GlassPanel progressSection = new GlassPanel();
        progressSection.setLayout(new BorderLayout(0, 10));
        progressSection.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JLabel progressTitle = new JLabel("OJT PROGRESS");
        progressTitle.setFont(Theme.FONT_LABEL);
        progressTitle.setForeground(Theme.TEXT_SECONDARY);
        progressBar = new ProgressBarPanel();
        progressBar.setPreferredSize(new Dimension(0, 26));
        progressSection.add(progressTitle, BorderLayout.NORTH);
        progressSection.add(progressBar, BorderLayout.CENTER);

        JPanel middleRow = new JPanel(new GridLayout(1, 2, 16, 0));
        middleRow.setOpaque(false);

        GlassPanel weeklyCard = new GlassPanel();
        weeklyCard.setLayout(new BorderLayout(0, 10));
        weeklyCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JLabel weeklyTitle = new JLabel("WEEKLY HOURS");
        weeklyTitle.setFont(Theme.FONT_LABEL);
        weeklyTitle.setForeground(Theme.TEXT_SECONDARY);
        weeklyChart = new WeeklyChartPanel();
        weeklyChart.setPreferredSize(new Dimension(0, 180));
        weeklyCard.add(weeklyTitle, BorderLayout.NORTH);
        weeklyCard.add(weeklyChart, BorderLayout.CENTER);

        recentCard = new GlassPanel();
        recentCard.setLayout(new BorderLayout(0, 10));
        recentCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);
        JLabel recentTitle = new JLabel("RECENT OJT RECORDS");
        recentTitle.setFont(Theme.FONT_LABEL);
        recentTitle.setForeground(Theme.TEXT_SECONDARY);
        RoundedButton viewAll = new RoundedButton("View All Records →", RoundedButton.Style.SECONDARY);
        viewAll.addActionListener(e -> onViewAllRecords.run());
        recentHeader.add(recentTitle, BorderLayout.WEST);
        recentHeader.add(viewAll, BorderLayout.EAST);

        recentTableModel = new DefaultTableModel(new Object[]{"Date", "Time In", "Time Out", "Break", "Hours"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recentTable = new com.ojttracker.components.ModernTable(recentTableModel);
        JScrollPane scrollPane = new JScrollPane(recentTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        emptyStateLabel = new JLabel("<html><div style='text-align:center'>No OJT records yet.<br>"
                + "Start tracking your OJT hours by adding your first attendance record.</div></html>",
                SwingConstants.CENTER);
        emptyStateLabel.setFont(Theme.FONT_BODY);
        emptyStateLabel.setForeground(Theme.TEXT_SECONDARY);

        recentCardsContainer = new JPanel(recentCardLayout);
        recentCardsContainer.setOpaque(false);
        recentCardsContainer.add(scrollPane, RECENT_TABLE);
        recentCardsContainer.add(emptyStateLabel, RECENT_EMPTY);

        recentCard.add(recentHeader, BorderLayout.NORTH);
        recentCard.add(recentCardsContainer, BorderLayout.CENTER);

        middleRow.add(weeklyCard);
        middleRow.add(recentCard);

        JPanel topGroup = new JPanel();
        topGroup.setOpaque(false);
        topGroup.setLayout(new java.awt.GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new java.awt.Insets(0, 0, 16, 0);
        g.gridy = 0;
        topGroup.add(statsRow, g);
        g.gridy = 1;
        topGroup.add(progressSection, g);

        add(header, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(topGroup, BorderLayout.NORTH);
        body.add(middleRow, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    /** Re-pulls a fresh {@link DashboardService.Snapshot} and repaints everything from it. */
    public void refresh() {
        if (refreshing) {
            return;
        }
        refreshing = true;
        try {
            List<Student> students = studentService.getStudents();
            DefaultComboBoxModel<Student> model = new DefaultComboBoxModel<>();
            for (Student student : students) {
                model.addElement(student);
            }
            studentSelector.setModel(model);

            Student selected = studentService.getStudent().orElse(null);
            if (selected != null) {
                studentSelector.setSelectedItem(selected);
            } else if (!students.isEmpty()) {
                studentSelector.setSelectedIndex(0);
            }

            String studentName = selected == null ? null : selected.getFullName();
            dashboardTitle.setText(studentName == null || studentName.isBlank()
                    ? "Dashboard" : "Dashboard - " + studentName);

            DashboardService.Snapshot snap = selected != null
                    ? dashboardService.buildSnapshot(selected.getId())
                    : dashboardService.buildSnapshot();

            requiredCard.setValue(snap.requiredHours() + " hrs");
            completedCard.setValue(snap.completedHours() + " hrs");
            completedCard.setCaption(snap.progressPercent() + "% of required hours");
            remainingCard.setValue(snap.remainingHours() + " hrs");
            progressCard.setValue(snap.progressPercent() + "%");

            String status = DashboardService.statusLabel(snap.status());
            statusLabel.setText("● " + status);
            statusLabel.setForeground(statusColor(snap.status()));

            progressBar.setPercent(snap.progressPercent());
            weeklyChart.setData(snap.weeklyHours());

            List<OJTRecord> recent = snap.recentRecords();
            recentTableModel.setRowCount(0);
            for (OJTRecord r : recent) {
                recentTableModel.addRow(new Object[]{
                        DateUtils.formatDate(r.getWorkDate()),
                        DateUtils.formatTime(r.getTimeIn()),
                        DateUtils.formatTime(r.getTimeOut()),
                        r.getBreakHours(),
                        r.getTotalHours()
                });
            }

            boolean empty = recent.isEmpty();
            recentCardLayout.show(recentCardsContainer, empty ? RECENT_EMPTY : RECENT_TABLE);
        } finally {
            refreshing = false;
        }
    }

    private Color statusColor(DashboardService.CompletionStatus status) {
        return switch (status) {
            case COMPLETED -> Theme.SUCCESS;
            case ALMOST_COMPLETE -> Theme.PRIMARY_ACCENT;
            case IN_PROGRESS -> Theme.WARNING;
            case NOT_STARTED -> Theme.TEXT_SECONDARY;
        };
    }

    /** Custom-painted horizontal progress bar (Completed vs Remaining). */
    private static class ProgressBarPanel extends JPanel {
        private double percent = 0;

        ProgressBarPanel() {
            setOpaque(false);
        }

        void setPercent(double percent) {
            this.percent = Math.max(0, Math.min(100, percent));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = h;

            g2.setColor(new Color(255, 255, 255, 25));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            int filledWidth = (int) (w * (percent / 100.0));
            if (filledWidth > 0) {
                g2.setColor(Theme.PRIMARY_ACCENT);
                g2.fillRoundRect(0, 0, Math.max(filledWidth, h), h, arc, arc);
            }

            g2.setColor(Theme.TEXT_PRIMARY);
            g2.setFont(Theme.FONT_SUBHEADING);
            String text = String.format("%.1f%%", percent);
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (w - textWidth) / 2, h / 2 + 5);
            g2.dispose();
        }
    }

    /** Simple custom-painted bar chart for the last 7 days of hours. */
    private static class WeeklyChartPanel extends JPanel {
        private Map<String, Double> data = Map.of();

        WeeklyChartPanel() {
            setOpaque(false);
        }

        void setData(Map<String, Double> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int n = data.size();
            int barAreaWidth = w / n;
            int barWidth = Math.max(18, Math.min(42, barAreaWidth / 2));
            double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
            max = Math.max(max, 1.0);

            g2.setFont(Theme.FONT_SMALL);
            int fontAscent = g2.getFontMetrics().getAscent();
            int labelBaseline = h - 8;
            int baseline = labelBaseline - fontAscent - 6;
            int plotTop = fontAscent + 8;
            int plotHeight = Math.max(1, baseline - plotTop);
            int i = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double hours = entry.getValue();
                int barHeight = (int) (plotHeight * (hours / max));
                int x = i * barAreaWidth + (barAreaWidth - barWidth) / 2;
                int y = baseline - barHeight;

                g2.setColor(hours > 0 ? Theme.PRIMARY_ACCENT : new Color(255, 255, 255, 20));
                g2.fillRoundRect(x, y, barWidth, Math.max(barHeight, 3), 8, 8);

                g2.setColor(Theme.TEXT_SECONDARY);
                String label = entry.getKey();
                int labelWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, i * barAreaWidth + (barAreaWidth - labelWidth) / 2, labelBaseline);

                if (hours > 0) {
                    String hoursText = String.format("%.1f", hours);
                    int hoursWidth = g2.getFontMetrics().stringWidth(hoursText);
                    g2.setColor(Theme.TEXT_PRIMARY);
                    int valueBaseline = Math.max(plotTop + fontAscent, y - 6);
                    g2.drawString(hoursText, i * barAreaWidth + (barAreaWidth - hoursWidth) / 2, valueBaseline);
                }
                i++;
            }
            g2.dispose();
        }
    }
}