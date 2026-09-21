package com.ojttracker.ui;

import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.Theme;
import com.ojttracker.dao.OJTSettingsDAO;
import com.ojttracker.service.DashboardService;
import com.ojttracker.service.OJTRecordService;
import com.ojttracker.service.StudentService;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The application's single top-level window. Owns the sidebar navigation
 * and a {@link CardLayout} content area holding one panel per section, so
 * the app never opens extra {@code JFrame} windows (see architecture
 * rules in the spec).
 */
public class MainFrame extends JFrame {

    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_RECORDS = "RECORDS";
    private static final String CARD_STUDENT = "STUDENT";
    private static final String CARD_REPORTS = "REPORTS";
    private static final String CARD_SETTINGS = "SETTINGS";
    private static final String CARD_ABOUT = "ABOUT";
    private static final String CARD_SETUP = "SETUP";

    private final StudentService studentService;
    private final OJTRecordService recordService;
    private final DashboardService dashboardService;
    private final OJTSettingsDAO settingsDAO;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, SidebarItem> sidebarItems = new LinkedHashMap<>();
    private JPanel sidebar;

    private DashboardPanel dashboardPanel;
    private OJTRecordsPanel recordsPanel;
    private StudentPanel studentPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    public MainFrame(StudentService studentService, OJTRecordService recordService,
                      DashboardService dashboardService, OJTSettingsDAO settingsDAO) {
        super("OJT Tracker");
        this.studentService = studentService;
        this.recordService = recordService;
        this.dashboardService = dashboardService;
        this.settingsDAO = settingsDAO;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750));
        setPreferredSize(new Dimension(1400, 850));
        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        buildSidebar();
        buildContentPanels();

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);

        if (!studentService.hasProfile()) {
            showCard(CARD_SETUP);
        } else {
            showCard(CARD_DASHBOARD);
            setActiveSidebarItem(CARD_DASHBOARD);
        }
    }

    private void buildSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new javax.swing.BoxLayout(sidebar, javax.swing.BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 16, 0));

        JLabel logo = new JLabel("🎓  OJT TRACKER");
        logo.setFont(Theme.FONT_HEADING);
        logo.setForeground(Theme.TEXT_PRIMARY);
        logo.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 20, 30, 20));
        sidebar.add(logo);

        addSidebarItem(CARD_DASHBOARD, "📊  Dashboard");
        addSidebarItem(CARD_RECORDS, "🗓  OJT Records");
        addSidebarItem(CARD_STUDENT, "🧑  Student");
        addSidebarItem(CARD_REPORTS, "🖨  Reports");
        addSidebarItem(CARD_SETTINGS, "⚙  Settings");

        sidebar.add(javax.swing.Box.createVerticalGlue());

        JPanel divider = new JPanel();
        divider.setMaximumSize(new Dimension(220, 1));
        divider.setBackground(Theme.GLASS_BORDER);
        divider.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        sidebar.add(divider);
        sidebar.add(javax.swing.Box.createVerticalStrut(8));

        addSidebarItem(CARD_ABOUT, "ℹ  About");
    }

    private void addSidebarItem(String card, String label) {
        SidebarItem item = new SidebarItem(label);
        item.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCard(card);
                setActiveSidebarItem(card);
            }
        });
        sidebarItems.put(card, item);
        sidebar.add(item);
    }

    private void setActiveSidebarItem(String card) {
        sidebarItems.forEach((key, item) -> item.setActive(key.equals(card)));
    }

    private void buildContentPanels() {
        FirstRunSetupPanel setupPanel = new FirstRunSetupPanel(studentService, () -> {
            refreshAll();
            showCard(CARD_DASHBOARD);
            setActiveSidebarItem(CARD_DASHBOARD);
        });

        dashboardPanel = new DashboardPanel(studentService, dashboardService, () -> {
            showCard(CARD_RECORDS);
            setActiveSidebarItem(CARD_RECORDS);
        });
        recordsPanel = new OJTRecordsPanel(studentService, recordService, this::refreshAll);
        studentPanel = new StudentPanel(studentService, this::refreshAll);
        reportsPanel = new ReportsPanel(studentService, recordService, dashboardService);
        settingsPanel = new SettingsPanel(studentService, settingsDAO, this::refreshAll);
        AboutPanel aboutPanel = new AboutPanel();

        contentPanel.add(wrap(setupPanel), CARD_SETUP);
        contentPanel.add(wrap(dashboardPanel), CARD_DASHBOARD);
        contentPanel.add(wrap(recordsPanel), CARD_RECORDS);
        contentPanel.add(wrap(studentPanel), CARD_STUDENT);
        contentPanel.add(wrap(reportsPanel), CARD_REPORTS);
        contentPanel.add(wrap(settingsPanel), CARD_SETTINGS);
        contentPanel.add(wrap(aboutPanel), CARD_ABOUT);
    }

    private JPanel wrap(JPanel inner) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BACKGROUND);
        outer.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        outer.add(inner, BorderLayout.CENTER);
        return outer;
    }

    private void showCard(String card) {
        cardLayout.show(contentPanel, card);
        if (card.equals(CARD_DASHBOARD)) {
            dashboardPanel.refresh();
        } else if (card.equals(CARD_RECORDS)) {
            recordsPanel.refresh();
        } else if (card.equals(CARD_STUDENT)) {
            studentPanel.refresh();
        } else if (card.equals(CARD_REPORTS)) {
            reportsPanel.refresh();
        } else if (card.equals(CARD_SETTINGS)) {
            settingsPanel.refresh();
        }
    }

    /** Called after any data mutation so every screen reflects the latest state. */
    private void refreshAll() {
        dashboardPanel.refresh();
        recordsPanel.refresh();
        studentPanel.refresh();
        reportsPanel.refresh();
    }

    /** A single sidebar navigation entry with a hover/active highlight. */
    private static class SidebarItem extends JPanel {
        private final JLabel label;
        private boolean active = false;
        private boolean hovered = false;

        SidebarItem(String text) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(220, 44));
            setPreferredSize(new Dimension(220, 44));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 8));

            label = new JLabel(text);
            label.setFont(Theme.FONT_BODY);
            label.setForeground(Theme.TEXT_SECONDARY);
            add(label, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            label.setForeground(active ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
            label.setFont(active ? Theme.FONT_SUBHEADING : Theme.FONT_BODY);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(Theme.SIDEBAR_ACTIVE);
                g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 12, 12);
                g2.setColor(Theme.PRIMARY_ACCENT);
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
            } else if (hovered) {
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Simple static About screen. */
    private static class AboutPanel extends JPanel {
        AboutPanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
            JPanel card = new com.ojttracker.components.GlassPanel();
            card.setLayout(new javax.swing.BoxLayout(card, javax.swing.BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            JLabel title = new JLabel("OJT Tracker");
            title.setFont(Theme.FONT_TITLE);
            title.setForeground(Theme.TEXT_PRIMARY);

            JLabel version = new JLabel("Version 1.0.0");
            version.setFont(Theme.FONT_BODY);
            version.setForeground(Theme.TEXT_SECONDARY);
            version.setBorder(BorderFactory.createEmptyBorder(8, 0, 20, 0));

            JLabel desc = new JLabel("<html><div style='width:400px'>A fully offline desktop application "
                    + "for tracking On-the-Job Training (OJT) attendance, monitoring required hours, "
                    + "and generating printable OJT attendance reports.<br><br>"
                    + "Built with Java Swing, SQLite and JDBC.</div></html>");
            desc.setFont(Theme.FONT_BODY);
            desc.setForeground(Theme.TEXT_SECONDARY);

            JLabel creator = new JLabel("Created by Jun Romulo Jarina");
            creator.setFont(Theme.FONT_SUBHEADING);
            creator.setForeground(Theme.TEXT_PRIMARY);
            creator.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

            card.add(title);
            card.add(version);
            card.add(desc);
            card.add(creator);

            add(card, BorderLayout.NORTH);
        }
    }
}