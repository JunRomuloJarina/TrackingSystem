package com.ojttracker.model;

/**
 * Simple typed wrapper around the key/value {@code settings} table.
 * Holds application-level preferences that are not tied to a single
 * student record (e.g. default daily hours, theme).
 */
public class OJTSettings {

    public static final String KEY_STANDARD_DAILY_HOURS = "standard_daily_hours";
    public static final String KEY_DEFAULT_BREAK_HOURS = "default_break_hours";
    public static final String KEY_THEME = "theme";
    public static final String KEY_FIRST_RUN_COMPLETE = "first_run_complete";

    private String key;
    private String value;

    public OJTSettings() {
    }

    public OJTSettings(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}