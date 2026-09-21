package com.ojttracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link OJTSettingsDAO}, using an
 * upsert so callers do not need to know whether a key already exists.
 */
public class OJTSettingsDAOImpl implements OJTSettingsDAO {

    private final Connection connection;

    public OJTSettingsDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<String> get(String key) {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("setting_value"));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read setting " + key, e);
        }
    }

    @Override
    public void set(String key, String value) {
        String sql = """
            INSERT INTO settings (setting_key, setting_value) VALUES (?, ?)
            ON CONFLICT(setting_key) DO UPDATE SET setting_value = excluded.setting_value
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save setting " + key, e);
        }
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        return get(key).orElse(defaultValue);
    }
}