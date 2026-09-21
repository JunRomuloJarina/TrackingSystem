package com.ojttracker.util;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Centralized validation rules for OJT records and student data, shared
 * between the UI forms and the service layer so the rules are enforced
 * consistently and can be unit tested independently of Swing.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    /** Thrown when a validation rule fails; message is friendly enough to show directly to the user. */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static void requireNonBlank(String value, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldLabel + " cannot be empty.");
        }
    }

    public static void requirePositive(double value, String fieldLabel) {
        if (value <= 0) {
            throw new ValidationException(fieldLabel + " must be greater than 0.");
        }
    }

    public static void requireNonNegative(double value, String fieldLabel) {
        if (value < 0) {
            throw new ValidationException(fieldLabel + " cannot be negative.");
        }
    }

    public static void requireValidDate(LocalDate date, String fieldLabel) {
        if (date == null) {
            throw new ValidationException("Please enter a valid " + fieldLabel + ".");
        }
    }

    /**
     * Validates a proposed time-in/time-out/break combination for a single
     * OJT attendance record and returns the resulting total hours.
     *
     * Rules enforced (spec section 11 &amp; 26):
     * - Time Out must be later than Time In
     * - Break cannot be negative
     * - Break cannot exceed (or equal) the raw working duration
     */
    public static double calculateAndValidateHours(LocalTime timeIn, LocalTime timeOut, double breakHours) {
        if (timeIn == null || timeOut == null) {
            throw new ValidationException("Please enter both a Time In and a Time Out.");
        }
        if (!timeOut.isAfter(timeIn)) {
            throw new ValidationException("⚠ Please enter a valid Time Out.\nTime Out must be later than Time In.");
        }
        requireNonNegative(breakHours, "Break duration");

        double rawDurationHours = java.time.Duration.between(timeIn, timeOut).toMinutes() / 60.0;
        if (breakHours >= rawDurationHours) {
            throw new ValidationException("Break duration cannot exceed the total working duration.");
        }
        double total = rawDurationHours - breakHours;
        // Round to 2 decimal places for clean display (e.g. 7.999999 -> 8.0)
        return Math.round(total * 100.0) / 100.0;
    }
}