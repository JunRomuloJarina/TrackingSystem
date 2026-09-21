package com.ojttracker.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void buildDateOptions_shouldCreateUserFriendlySelectableDates() {
        List<LocalDate> dates = DateUtils.buildDateOptions(LocalDate.of(2026, 1, 1), 3);

        assertFalse(dates.isEmpty());
        assertEquals(LocalDate.of(2026, 1, 1), dates.get(0));
        assertEquals(LocalDate.of(2026, 1, 3), dates.get(dates.size() - 1));
        assertEquals(3, dates.size());
    }

    @Test
    void buildTimeOptions_shouldCreateSelectableTimesAtThirtyMinuteIntervals() {
        List<LocalTime> times = DateUtils.buildTimeOptions(30);

        assertFalse(times.isEmpty());
        assertEquals(LocalTime.of(0, 0), times.get(0));
        assertEquals(LocalTime.of(23, 30), times.get(times.size() - 1));
        assertEquals(48, times.size());
    }

    @Test
    void parseTimeFlexible_shouldAcceptCustomMinuteValues() {
        assertEquals(LocalTime.of(9, 3), DateUtils.parseTimeFlexible("9:03 AM"));
        assertEquals(LocalTime.of(21, 3), DateUtils.parseTimeFlexible("9:03 PM"));
    }
}
