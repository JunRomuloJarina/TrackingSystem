package com.ojttracker.dao;

import java.util.Optional;

/**
 * Data access contract for the key/value application settings table.
 */
public interface OJTSettingsDAO {

    Optional<String> get(String key);

    void set(String key, String value);

    String getOrDefault(String key, String defaultValue);
}