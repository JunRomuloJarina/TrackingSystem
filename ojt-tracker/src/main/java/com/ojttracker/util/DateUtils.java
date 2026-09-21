package com.ojttracker.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Formatting and parsing helpers for dates/times used across the UI,
 * keeping presentation format decisions out of the Swing panels.
 */
public final class DateUtils {

    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
    public static final DateTimeFormatter DISPLAY_DATE_LONG = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);
    public static final DateTimeFormatter DISPLAY_TIME_12H = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);
    public static final DateTimeFormatter INPUT_TIME_24H = DateTimeFormatter.ofPattern("HH:mm");

    private DateUtils() {
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DISPLAY_DATE);
    }

    public static String formatDateLong(LocalDate date) {
        return date == null ? "" : date.format(DISPLAY_DATE_LONG);
    }

    public static String formatTime(LocalTime time) {
        return time == null ? "" : time.format(DISPLAY_TIME_12H);
    }

    /** Parses a 12-hour time string like "8:00 AM" or a 24-hour string like "08:00". Returns null if unparsable. */
    public static LocalTime parseTimeFlexible(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim();
        DateTimeFormatter[] formats = {
                DateTimeFormatter.ofPattern("h:mm a", Locale.US),
                DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm")
        };
        for (DateTimeFormatter fmt : formats) {
            try {
                return LocalTime.parse(trimmed, fmt);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }

    public static int weekOfYear(LocalDate date) {
        return date.get(WeekFields.of(Locale.US).weekOfWeekBasedYear());
    }

    public static List<LocalDate> buildDateOptions(LocalDate startDate, int days) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate == null ? LocalDate.now() : startDate;
        for (int i = 0; i < days; i++) {
            dates.add(current.plusDays(i));
        }
        return dates;
    }

    public static List<LocalTime> buildTimeOptions(int stepMinutes) {
        if (stepMinutes <= 0) {
            return List.of(LocalTime.MIDNIGHT);
        }
        int totalMinutes = 24 * 60;
        int steps = totalMinutes / stepMinutes;
        List<LocalTime> times = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            times.add(LocalTime.MIDNIGHT.plusMinutes((long) i * stepMinutes));
        }
        return times;
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end) + 1;
    }
}