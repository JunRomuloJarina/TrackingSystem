package com.ojttracker.util;

import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

class AttendanceValidationTest {
    @Test void calculatesMorningAndAfternoonHoursMinusBreak() {
        assertEquals(7.0, ValidationUtils.calculateAndValidateHours(LocalTime.of(8,0), LocalTime.of(12,0), LocalTime.of(13,0), LocalTime.of(17,0), 1.0));
    }
    @Test void rejectsMorningTimeOutBeforeMorningTimeIn() {
        assertThrows(ValidationUtils.ValidationException.class, () -> ValidationUtils.calculateAndValidateHours(LocalTime.of(12,0), LocalTime.of(8,0), LocalTime.of(13,0), LocalTime.of(17,0), 0));
    }
    @Test void rejectsAfternoonTimeInBeforeMorningTimeOut() {
        assertThrows(ValidationUtils.ValidationException.class, () -> ValidationUtils.calculateAndValidateHours(LocalTime.of(8,0), LocalTime.of(12,0), LocalTime.of(11,0), LocalTime.of(17,0), 0));
    }
    @Test void rejectsAfternoonTimeOutBeforeAfternoonTimeIn() {
        assertThrows(ValidationUtils.ValidationException.class, () -> ValidationUtils.calculateAndValidateHours(LocalTime.of(8,0), LocalTime.of(12,0), LocalTime.of(13,0), LocalTime.of(12,0), 0));
    }
    @Test void rejectsBreakThatConsumesAllWorkingTime() {
        assertThrows(ValidationUtils.ValidationException.class, () -> ValidationUtils.calculateAndValidateHours(LocalTime.of(8,0), LocalTime.of(12,0), LocalTime.of(13,0), LocalTime.of(17,0), 8));
    }
    @Test void retainsLegacySingleIntervalCalculation() { assertEquals(7.0, ValidationUtils.calculateAndValidateHours(LocalTime.of(8,0),LocalTime.of(16,0),1)); }
}
