package com.ojttracker.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public final class ValidationUtils {
    private ValidationUtils() { }
    public static class ValidationException extends RuntimeException { public ValidationException(String message) { super(message); } }
    public static void requireNonBlank(String value, String label) { if (value == null || value.isBlank()) throw new ValidationException(label + " cannot be empty."); }
    public static void requirePositive(double value, String label) { if (value <= 0) throw new ValidationException(label + " must be greater than 0."); }
    public static void requireNonNegative(double value, String label) { if (value < 0) throw new ValidationException(label + " cannot be negative."); }
    public static void requireValidDate(LocalDate date, String label) { if (date == null) throw new ValidationException("Please enter a valid " + label + "."); }

    public static double calculateAndValidateHours(LocalTime morningIn, LocalTime morningOut, LocalTime afternoonIn, LocalTime afternoonOut, double breakHours) {
        if (morningIn == null || morningOut == null || afternoonIn == null || afternoonOut == null)
            throw new ValidationException("Please enter all morning and afternoon time values.");
        if (!morningOut.isAfter(morningIn)) throw new ValidationException("Morning Time Out must be later than Morning Time In.");
        if (!afternoonIn.isAfter(morningOut)) throw new ValidationException("Afternoon Time In must be later than Morning Time Out.");
        if (!afternoonOut.isAfter(afternoonIn)) throw new ValidationException("Afternoon Time Out must be later than Afternoon Time In.");
        requireNonNegative(breakHours, "Break duration");
        double raw = (Duration.between(morningIn, morningOut).toMinutes() + Duration.between(afternoonIn, afternoonOut).toMinutes()) / 60.0;
        if (breakHours >= raw) throw new ValidationException("Break duration cannot exceed the total working duration.");
        return round(raw - breakHours);
    }

    /** Legacy single-interval calculation retained for older callers. */
    public static double calculateAndValidateHours(LocalTime timeIn, LocalTime timeOut, double breakHours) {
        if (timeIn == null || timeOut == null) throw new ValidationException("Please enter both a Time In and a Time Out.");
        if (!timeOut.isAfter(timeIn)) throw new ValidationException("Time Out must be later than Time In.");
        requireNonNegative(breakHours, "Break duration");
        double raw = Duration.between(timeIn, timeOut).toMinutes() / 60.0;
        if (breakHours >= raw) throw new ValidationException("Break duration cannot exceed the total working duration.");
        return round(raw - breakHours);
    }

    private static double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
