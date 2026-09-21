package com.ojttracker.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a single day's OJT attendance entry: time in/out, break, and
 * the resulting total hours rendered for that day.
 */
public class OJTRecord {

    private int id;
    private int studentId;
    private LocalDate workDate;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private double breakHours;
    private double totalHours;
    private String remarks;

    public OJTRecord() {
    }

    public OJTRecord(int id, int studentId, LocalDate workDate, LocalTime timeIn, LocalTime timeOut,
                      double breakHours, double totalHours, String remarks) {
        this.id = id;
        this.studentId = studentId;
        this.workDate = workDate;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.breakHours = breakHours;
        this.totalHours = totalHours;
        this.remarks = remarks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalTime getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(LocalTime timeIn) {
        this.timeIn = timeIn;
    }

    public LocalTime getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(LocalTime timeOut) {
        this.timeOut = timeOut;
    }

    public double getBreakHours() {
        return breakHours;
    }

    public void setBreakHours(double breakHours) {
        this.breakHours = breakHours;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}