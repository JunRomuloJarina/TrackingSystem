package com.ojttracker.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Represents one day's morning and afternoon attendance periods. */
public class OJTRecord {
    private int id;
    private int studentId;
    private LocalDate workDate;
    private LocalTime morningTimeIn;
    private LocalTime morningTimeOut;
    private LocalTime afternoonTimeIn;
    private LocalTime afternoonTimeOut;
    private double breakHours;
    private double totalHours;
    private String remarks;

    public OJTRecord() { }

    public OJTRecord(int id, int studentId, LocalDate workDate, LocalTime morningTimeIn,
                     LocalTime morningTimeOut, LocalTime afternoonTimeIn, LocalTime afternoonTimeOut,
                     double breakHours, double totalHours, String remarks) {
        this.id = id; this.studentId = studentId; this.workDate = workDate;
        this.morningTimeIn = morningTimeIn; this.morningTimeOut = morningTimeOut;
        this.afternoonTimeIn = afternoonTimeIn; this.afternoonTimeOut = afternoonTimeOut;
        this.breakHours = breakHours; this.totalHours = totalHours; this.remarks = remarks;
    }

    /** Backward-compatible constructor for older callers. */
    public OJTRecord(int id, int studentId, LocalDate date, LocalTime timeIn, LocalTime timeOut,
                     double breakHours, double totalHours, String remarks) {
        this(id, studentId, date, timeIn, null, null, timeOut, breakHours, totalHours, remarks);
    }

    public int getId() { return id; }
    public void setId(int value) { id = value; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int value) { studentId = value; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate value) { workDate = value; }
    public LocalTime getMorningTimeIn() { return morningTimeIn; }
    public void setMorningTimeIn(LocalTime value) { morningTimeIn = value; }
    public LocalTime getMorningTimeOut() { return morningTimeOut; }
    public void setMorningTimeOut(LocalTime value) { morningTimeOut = value; }
    public LocalTime getAfternoonTimeIn() { return afternoonTimeIn; }
    public void setAfternoonTimeIn(LocalTime value) { afternoonTimeIn = value; }
    public LocalTime getAfternoonTimeOut() { return afternoonTimeOut; }
    public void setAfternoonTimeOut(LocalTime value) { afternoonTimeOut = value; }
    /** Legacy alias for the first time-in. */
    public LocalTime getTimeIn() { return morningTimeIn; }
    public void setTimeIn(LocalTime value) { morningTimeIn = value; }
    /** Legacy alias for the final time-out. */
    public LocalTime getTimeOut() { return afternoonTimeOut; }
    public void setTimeOut(LocalTime value) { afternoonTimeOut = value; }
    public double getBreakHours() { return breakHours; }
    public void setBreakHours(double value) { breakHours = value; }
    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double value) { totalHours = value; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String value) { remarks = value; }
}
