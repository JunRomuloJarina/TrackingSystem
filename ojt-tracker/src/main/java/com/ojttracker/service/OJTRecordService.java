package com.ojttracker.service;

import com.ojttracker.dao.OJTRecordDAO;
import com.ojttracker.model.OJTRecord;
import com.ojttracker.util.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Business logic for OJT attendance records: validation, automatic hour
 * calculation, duplicate-date prevention, search and filtering.
 */
public class OJTRecordService {

    private final OJTRecordDAO recordDAO;

    public OJTRecordService(OJTRecordDAO recordDAO) {
        this.recordDAO = recordDAO;
    }

    /**
     * Validates and saves a new OJT record, computing total hours
     * automatically and rejecting a duplicate date for the same student.
     */
    public OJTRecord addRecord(int studentId, LocalDate workDate, LocalTime timeIn, LocalTime timeOut,
                                double breakHours, String remarks) {
        ValidationUtils.requireValidDate(workDate, "Date");
        if (recordDAO.findByStudentAndDate(studentId, workDate).isPresent()) {
            throw new ValidationUtils.ValidationException("An OJT record already exists for this date.");
        }
        double totalHours = ValidationUtils.calculateAndValidateHours(timeIn, timeOut, breakHours);

        OJTRecord record = new OJTRecord();
        record.setStudentId(studentId);
        record.setWorkDate(workDate);
        record.setTimeIn(timeIn);
        record.setTimeOut(timeOut);
        record.setBreakHours(breakHours);
        record.setTotalHours(totalHours);
        record.setRemarks(remarks);
        return recordDAO.save(record);
    }

    /** Validates and updates an existing record, re-checking the duplicate-date rule against other rows. */
    public void updateRecord(int recordId, int studentId, LocalDate workDate, LocalTime timeIn, LocalTime timeOut,
                              double breakHours, String remarks) {
        ValidationUtils.requireValidDate(workDate, "Date");
        Optional<OJTRecord> sameDate = recordDAO.findByStudentAndDate(studentId, workDate);
        if (sameDate.isPresent() && sameDate.get().getId() != recordId) {
            throw new ValidationUtils.ValidationException("An OJT record already exists for this date.");
        }
        double totalHours = ValidationUtils.calculateAndValidateHours(timeIn, timeOut, breakHours);

        OJTRecord record = new OJTRecord();
        record.setId(recordId);
        record.setStudentId(studentId);
        record.setWorkDate(workDate);
        record.setTimeIn(timeIn);
        record.setTimeOut(timeOut);
        record.setBreakHours(breakHours);
        record.setTotalHours(totalHours);
        record.setRemarks(remarks);
        recordDAO.update(record);
    }

    public void deleteRecord(int recordId) {
        recordDAO.delete(recordId);
    }

    public List<OJTRecord> getAllRecords(int studentId) {
        return recordDAO.findAll(studentId);
    }

    public List<OJTRecord> getRecentRecords(int studentId, int limit) {
        return recordDAO.findRecent(studentId, limit);
    }

    public double getTotalHours(int studentId) {
        return recordDAO.sumTotalHours(studentId);
    }

    public int getRecordCount(int studentId) {
        return recordDAO.countRecords(studentId);
    }

    /**
     * Filters and searches the full record list in memory. The dataset
     * for a single OJT term is small, so this keeps the DAO layer's SQL
     * simple while still giving the UI flexible search/filter/sort.
     */
    public List<OJTRecord> searchAndFilter(int studentId, String searchText, DateFilter filter) {
        List<OJTRecord> all = recordDAO.findAll(studentId);
        LocalDate today = LocalDate.now();

        return all.stream()
                .filter(r -> matchesDateFilter(r, filter, today))
                .filter(r -> matchesSearch(r, searchText))
                .sorted(Comparator.comparing(OJTRecord::getWorkDate).reversed())
                .collect(Collectors.toList());
    }

    private boolean matchesDateFilter(OJTRecord record, DateFilter filter, LocalDate today) {
        if (filter == null || filter.type() == DateFilterType.ALL) {
            return true;
        }
        LocalDate d = record.getWorkDate();
        return switch (filter.type()) {
            case THIS_WEEK -> {
                LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                LocalDate weekEnd = weekStart.plusDays(6);
                yield !d.isBefore(weekStart) && !d.isAfter(weekEnd);
            }
            case THIS_MONTH -> d.getMonth() == today.getMonth() && d.getYear() == today.getYear();
            case CUSTOM_RANGE -> (filter.start() == null || !d.isBefore(filter.start()))
                    && (filter.end() == null || !d.isAfter(filter.end()));
            case ALL -> true;
        };
    }

    private boolean matchesSearch(OJTRecord record, String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return true;
        }
        String needle = searchText.trim().toLowerCase();
        String remarks = record.getRemarks() == null ? "" : record.getRemarks().toLowerCase();
        String dateText = com.ojttracker.util.DateUtils.formatDate(record.getWorkDate()).toLowerCase();
        return remarks.contains(needle) || dateText.contains(needle);
    }

    public enum DateFilterType { ALL, THIS_WEEK, THIS_MONTH, CUSTOM_RANGE }

    public record DateFilter(DateFilterType type, LocalDate start, LocalDate end) {
        public static DateFilter all() {
            return new DateFilter(DateFilterType.ALL, null, null);
        }

        public static DateFilter thisWeek() {
            return new DateFilter(DateFilterType.THIS_WEEK, null, null);
        }

        public static DateFilter thisMonth() {
            return new DateFilter(DateFilterType.THIS_MONTH, null, null);
        }

        public static DateFilter range(LocalDate start, LocalDate end) {
            return new DateFilter(DateFilterType.CUSTOM_RANGE, start, end);
        }
    }
}