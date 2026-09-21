package com.ojttracker.model;

import java.time.LocalDate;

/**
 * Represents the OJT student and their placement information.
 * The application is designed around a single active student profile
 * (the person using the desktop app), stored as one row in the
 * {@code students} table.
 */
public class Student {

    private int id;
    private String studentId;
    private String fullName;
    private String course;
    private String yearLevel;
    private String school;
    private String ojtCompany;
    private String companyAddress;
    private String supervisor;
    private String supervisorContact;
    private LocalDate ojtStartDate;
    private LocalDate expectedEndDate;
    private double requiredHours;

    public Student() {
    }

    public Student(int id, String studentId, String fullName, String course, String yearLevel,
                   String school, String ojtCompany, String companyAddress, String supervisor,
                   String supervisorContact, LocalDate ojtStartDate, LocalDate expectedEndDate,
                   double requiredHours) {
        this.id = id;
        this.studentId = studentId;
        this.fullName = fullName;
        this.course = course;
        this.yearLevel = yearLevel;
        this.school = school;
        this.ojtCompany = ojtCompany;
        this.companyAddress = companyAddress;
        this.supervisor = supervisor;
        this.supervisorContact = supervisorContact;
        this.ojtStartDate = ojtStartDate;
        this.expectedEndDate = expectedEndDate;
        this.requiredHours = requiredHours;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getOjtCompany() {
        return ojtCompany;
    }

    public void setOjtCompany(String ojtCompany) {
        this.ojtCompany = ojtCompany;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getSupervisorContact() {
        return supervisorContact;
    }

    public void setSupervisorContact(String supervisorContact) {
        this.supervisorContact = supervisorContact;
    }

    public LocalDate getOjtStartDate() {
        return ojtStartDate;
    }

    public void setOjtStartDate(LocalDate ojtStartDate) {
        this.ojtStartDate = ojtStartDate;
    }

    public LocalDate getExpectedEndDate() {
        return expectedEndDate;
    }

    public void setExpectedEndDate(LocalDate expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public double getRequiredHours() {
        return requiredHours;
    }

    public void setRequiredHours(double requiredHours) {
        this.requiredHours = requiredHours;
    }

    @Override
    public String toString() {
        return fullName == null ? "Unnamed Student" : fullName;
    }
}