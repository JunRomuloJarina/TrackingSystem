package com.ojttracker.service;

import com.ojttracker.dao.OJTRecordDAO;
import com.ojttracker.dao.StudentDAO;
import com.ojttracker.model.OJTRecord;
import com.ojttracker.model.Student;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Computes all dashboard analytics dynamically from the database. Nothing
 * here is hardcoded: required hours come from the student profile, and
 * completed/remaining/progress/averages are all derived from the stored
 * OJT records at call time.
 */
public class DashboardService {

    private final StudentDAO studentDAO;
    private final OJTRecordDAO recordDAO;

    public DashboardService(StudentDAO studentDAO, OJTRecordDAO recordDAO) {
        this.studentDAO = studentDAO;
        this.recordDAO = recordDAO;
    }

    public enum CompletionStatus { NOT_STARTED, IN_PROGRESS, ALMOST_COMPLETE, COMPLETED }

    /** Immutable snapshot of everything the Dashboard and Reports screens need to render. */
    public record Snapshot(
            double requiredHours,
            double completedHours,
            double remainingHours,
            double progressPercent,
            int ojtDays,
            double averageHoursPerDay,
            double averageHoursPerWeek,
            double averageHoursPerMonth,
            CompletionStatus status,
            List<OJTRecord> recentRecords,
            Map<String, Double> weeklyHours
    ) {
    }

    public Snapshot buildSnapshot() {
        Optional<Student> studentOpt = studentDAO.findAll().stream().findFirst();
        return studentOpt.map(student -> buildSnapshot(student.getId()))
                .orElseGet(this::emptySnapshot);
    }

    public Snapshot buildSnapshot(int studentId) {
        if (studentId <= 0) {
            return emptySnapshot();
        }
        Optional<Student> studentOpt = studentDAO.findById(studentId);
        if (studentOpt.isEmpty()) {
            return emptySnapshot();
        }
        Student student = studentOpt.get();

        double required = student.getRequiredHours();
        double completed = recordDAO.sumTotalHours(studentId);
        double remaining = Math.max(required - completed, 0);
        double progress = required > 0 ? clampPercent((completed / required) * 100.0) : 0.0;

        List<OJTRecord> allRecords = recordDAO.findAll(studentId);
        int ojtDays = allRecords.size();

        double avgPerDay = ojtDays > 0 ? completed / ojtDays : 0.0;
        double avgPerWeek = avgPerDay * 5.0; // assumes a standard 5-day OJT week
        double avgPerMonth = avgPerDay * 22.0; // assumes ~22 working days per month

        CompletionStatus status = determineStatus(progress);
        List<OJTRecord> recent = recordDAO.findRecent(studentId, 5);
        Map<String, Double> weeklyHours = buildLast7DaysHours(allRecords);

        return new Snapshot(round2(required), round2(completed), round2(remaining), round2(progress),
                ojtDays, round2(avgPerDay), round2(avgPerWeek), round2(avgPerMonth), status, recent, weeklyHours);
    }

    private CompletionStatus determineStatus(double progressPercent) {
        if (progressPercent >= 100.0) {
            return CompletionStatus.COMPLETED;
        } else if (progressPercent >= 80.0) {
            return CompletionStatus.ALMOST_COMPLETE;
        } else if (progressPercent > 0.0) {
            return CompletionStatus.IN_PROGRESS;
        }
        return CompletionStatus.NOT_STARTED;
    }

    /** Builds a MON..SUN style map of hours logged in the most recent 7 calendar days that have records. */
    private Map<String, Double> buildLast7DaysHours(List<OJTRecord> allRecords) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        Map<String, Double> hoursByDay = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            String label = day.getDayOfWeek().toString().substring(0, 3);
            hoursByDay.put(label, 0.0);
        }
        for (OJTRecord record : allRecords) {
            LocalDate d = record.getWorkDate();
            if (!d.isBefore(weekStart) && !d.isAfter(today)) {
                String label = d.getDayOfWeek().toString().substring(0, 3);
                hoursByDay.merge(label, record.getTotalHours(), Double::sum);
            }
        }
        return hoursByDay;
    }

    /** Total hours grouped by calendar month (YYYY-MM), oldest first — used by the Reports/monthly view. */
    public Map<String, Double> buildMonthlyHours(int studentId) {
        Map<String, Double> byMonth = new LinkedHashMap<>();
        for (OJTRecord record : recordDAO.findAll(studentId)) {
            YearMonth ym = YearMonth.from(record.getWorkDate());
            byMonth.merge(ym.toString(), record.getTotalHours(), Double::sum);
        }
        return byMonth;
    }

    private Snapshot emptySnapshot() {
        return new Snapshot(0, 0, 0, 0, 0, 0, 0, 0, CompletionStatus.NOT_STARTED,
                List.of(), Map.of());
    }

    private double clampPercent(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** Human-readable status label used directly by the UI. */
    public static String statusLabel(CompletionStatus status) {
        return switch (status) {
            case NOT_STARTED -> "NOT STARTED";
            case IN_PROGRESS -> "IN PROGRESS";
            case ALMOST_COMPLETE -> "ALMOST COMPLETE";
            case COMPLETED -> "OJT COMPLETED";
        };
    }
}