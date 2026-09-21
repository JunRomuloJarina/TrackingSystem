package com.ojttracker;

import com.ojttracker.config.DatabaseConfig;
import com.ojttracker.dao.OJTRecordDAO;
import com.ojttracker.dao.OJTRecordDAOImpl;
import com.ojttracker.dao.OJTSettingsDAO;
import com.ojttracker.dao.OJTSettingsDAOImpl;
import com.ojttracker.dao.StudentDAO;
import com.ojttracker.dao.StudentDAOImpl;
import com.ojttracker.service.DashboardService;
import com.ojttracker.service.OJTRecordService;
import com.ojttracker.service.StudentService;
import com.ojttracker.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;

/**
 * Application entry point.
 *
 * Responsibilities: initialize the SQLite database, wire up the
 * DAO/Service layers, and hand off to {@link MainFrame} for the UI.
 * The UI never talks to the database directly (see architecture rules).
 */
public final class Main {   

    private Main() {
    }

    public static void main(String[] args) {
        // Use the platform look and feel as a sane baseline; our custom
        // components override the visuals that matter most.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the default cross-platform look and feel.
        }

        SwingUtilities.invokeLater(() -> {
            try {
                Connection connection = DatabaseConfig.getConnection();

                StudentDAO studentDAO = new StudentDAOImpl(connection);
                OJTRecordDAO recordDAO = new OJTRecordDAOImpl(connection);
                OJTSettingsDAO settingsDAO = new OJTSettingsDAOImpl(connection);

                StudentService studentService = new StudentService(studentDAO);
                OJTRecordService recordService = new OJTRecordService(recordDAO);
                DashboardService dashboardService = new DashboardService(studentDAO, recordDAO);

                MainFrame mainFrame = new MainFrame(studentService, recordService, dashboardService, settingsDAO);
                mainFrame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to start OJT Tracker:\n" + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}