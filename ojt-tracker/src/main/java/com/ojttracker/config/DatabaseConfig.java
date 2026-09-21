package com.ojttracker.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single SQLite connection used by the application and is
 * responsible for creating the database file and schema on first run.
 *
 * The database file is stored in the current user's application data folder
 * so the app works fully offline and does not need write access beside the
 * executable.
 */
public final class DatabaseConfig {

    private static final String DB_DIR_NAME = "OJT Tracker";
    private static final String LEGACY_DB_DIR_NAME = ".ojt-tracker";
    private static final String DB_FILE_NAME = "ojt_tracker.db";

    private static Connection connection;

    private DatabaseConfig() {
    }

    /**
     * Returns the single shared connection, creating the database file,
     * directory and schema the first time it is called.
     */
    public static synchronized Connection getConnection() {
        if (connection != null) {
            return connection;
        }
        try {
            Class.forName("org.sqlite.JDBC");
            File dbDir = new File(System.getProperty("user.home"), DB_DIR_NAME);
            if (!dbDir.exists() && !dbDir.mkdirs()) {
                throw new IllegalStateException("Could not create database directory: " + dbDir);
            }
            File dbFile = new File(dbDir, DB_FILE_NAME);
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            try (Statement pragma = connection.createStatement()) {
                pragma.execute("PRAGMA foreign_keys = ON");
            }
            initializeSchema(connection);
            return connection;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("SQLite JDBC driver not found on classpath.", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to connect to the OJT database.", e);
        }
    }

    /** Returns the absolute path of the SQLite database file, for display in Settings. */
    public static String getDatabasePath() {
        return databaseFile().toString();
    }

    private static File databaseFile() {
        String localAppData = System.getenv("LOCALAPPDATA");
        File dbDir = localAppData == null || localAppData.isBlank()
                ? new File(System.getProperty("user.home"), ".ojt-tracker")
                : new File(localAppData, DB_DIR_NAME);
        Path directory = dbDir.toPath();
        try {
            Files.createDirectories(directory);
            migrateLegacyDatabase(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create database directory: " + directory, e);
        }
        return directory.resolve(DB_FILE_NAME).toFile();
    }

    private static void migrateLegacyDatabase(Path currentDirectory) throws IOException {
        Path currentFile = currentDirectory.resolve(DB_FILE_NAME);
        Path legacyFile = Path.of(System.getProperty("user.home"), LEGACY_DB_DIR_NAME, DB_FILE_NAME);
        if (Files.notExists(currentFile) && Files.exists(legacyFile)) {
            Files.copy(legacyFile, currentFile);
        }
    }

    private static void initializeSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id TEXT,
                    full_name TEXT NOT NULL,
                    course TEXT,
                    year_level TEXT,
                    school TEXT,
                    ojt_company TEXT,
                    company_address TEXT,
                    supervisor TEXT,
                    supervisor_contact TEXT,
                    ojt_start_date TEXT,
                    expected_end_date TEXT,
                    required_hours REAL DEFAULT 0
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ojt_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    work_date TEXT NOT NULL,
                    time_in TEXT NOT NULL,
                    time_out TEXT NOT NULL,
                    break_hours REAL DEFAULT 0,
                    total_hours REAL NOT NULL,
                    remarks TEXT,
                    FOREIGN KEY(student_id) REFERENCES students(id),
                    UNIQUE(student_id, work_date)
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS settings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    setting_key TEXT UNIQUE NOT NULL,
                    setting_value TEXT
                )
                """);
        }
    }
}