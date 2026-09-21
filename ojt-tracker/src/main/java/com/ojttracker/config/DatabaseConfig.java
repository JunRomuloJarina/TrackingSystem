package com.ojttracker.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConfig {
    private static final String DB_DIR_NAME = "OJT Tracker";
    private static final String LEGACY_DB_DIR_NAME = ".ojt-tracker";
    private static final String DB_FILE_NAME = "ojt_tracker.db";
    private static Connection connection;
    private DatabaseConfig() { }

    public static synchronized Connection getConnection() {
        if (connection != null) return connection;
        try {
            Class.forName("org.sqlite.JDBC");
            File dbDir = new File(System.getProperty("user.home"), DB_DIR_NAME);
            if (!dbDir.exists() && !dbDir.mkdirs()) throw new IllegalStateException("Could not create database directory: " + dbDir);
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dbDir, DB_FILE_NAME).getAbsolutePath());
            try (Statement pragma = connection.createStatement()) { pragma.execute("PRAGMA foreign_keys = ON"); }
            initializeSchema(connection);
            return connection;
        } catch (ClassNotFoundException e) { throw new IllegalStateException("SQLite JDBC driver not found on classpath.", e); }
        catch (SQLException e) { throw new IllegalStateException("Failed to connect to the OJT database.", e); }
    }

    public static String getDatabasePath() { return databaseFile().toString(); }
    private static File databaseFile() {
        String local = System.getenv("LOCALAPPDATA");
        File dir = local == null || local.isBlank() ? new File(System.getProperty("user.home"), LEGACY_DB_DIR_NAME) : new File(local, DB_DIR_NAME);
        try { Files.createDirectories(dir.toPath()); migrateLegacyDatabase(dir.toPath()); }
        catch (IOException e) { throw new IllegalStateException("Could not create database directory: " + dir, e); }
        return new File(dir, DB_FILE_NAME);
    }
    private static void migrateLegacyDatabase(Path dir) throws IOException {
        Path current = dir.resolve(DB_FILE_NAME);
        Path legacy = Path.of(System.getProperty("user.home"), LEGACY_DB_DIR_NAME, DB_FILE_NAME);
        if (Files.notExists(current) && Files.exists(legacy)) Files.copy(legacy, current);
    }
    private static void initializeSchema(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id TEXT, full_name TEXT NOT NULL, course TEXT, year_level TEXT, school TEXT, ojt_company TEXT, company_address TEXT, supervisor TEXT, supervisor_contact TEXT, ojt_start_date TEXT, expected_end_date TEXT, required_hours REAL DEFAULT 0)");
            s.execute("CREATE TABLE IF NOT EXISTS ojt_records (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, work_date TEXT NOT NULL, time_in TEXT NOT NULL, time_out TEXT NOT NULL, break_hours REAL DEFAULT 0, total_hours REAL NOT NULL, remarks TEXT, FOREIGN KEY(student_id) REFERENCES students(id), UNIQUE(student_id, work_date))");
            addColumnIfMissing(s, "morning_time_in TEXT");
            addColumnIfMissing(s, "morning_time_out TEXT");
            addColumnIfMissing(s, "afternoon_time_in TEXT");
            addColumnIfMissing(s, "afternoon_time_out TEXT");
            s.execute("UPDATE ojt_records SET morning_time_in = COALESCE(morning_time_in, time_in), afternoon_time_out = COALESCE(afternoon_time_out, time_out)");
            s.execute("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY AUTOINCREMENT, setting_key TEXT UNIQUE NOT NULL, setting_value TEXT)");
        }
    }
    private static void addColumnIfMissing(Statement s, String definition) throws SQLException {
        try { s.execute("ALTER TABLE ojt_records ADD COLUMN " + definition); } catch (SQLException ignored) { }
    }
}
