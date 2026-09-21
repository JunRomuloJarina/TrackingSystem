package com.ojttracker.service;

import com.ojttracker.dao.StudentDAO;
import com.ojttracker.model.Student;
import com.ojttracker.util.ValidationUtils;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for the student profile: validation and persistence,
 * kept separate from both the DAO (pure SQL) and the UI (pure Swing).
 */
public class StudentService {

    private final StudentDAO studentDAO;
    private Integer activeStudentId;

    public StudentService(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    public boolean hasProfile() {
        return !studentDAO.findAll().isEmpty();
    }

    public List<Student> getStudents() {
        List<Student> students = studentDAO.findAll();
        if (students.isEmpty()) {
            activeStudentId = null;
            return students;
        }
        if (activeStudentId == null || students.stream().noneMatch(s -> s.getId() == activeStudentId)) {
            activeStudentId = students.get(0).getId();
        }
        return students;
    }

    public Optional<Student> getStudent() {
        List<Student> students = getStudents();
        if (students.isEmpty()) {
            return Optional.empty();
        }
        if (activeStudentId == null) {
            activeStudentId = students.get(0).getId();
        }
        return students.stream()
                .filter(student -> student.getId() == activeStudentId)
                .findFirst()
                .or(() -> Optional.of(students.get(0)));
    }

    public void setActiveStudent(int studentId) {
        if (studentDAO.findById(studentId).isPresent()) {
            this.activeStudentId = studentId;
        }
    }

    /** Creates the profile if none exists yet, otherwise updates the existing one. */
    public Student saveOrUpdate(Student student) {
        validate(student);
        if (student.getId() > 0) {
            studentDAO.update(student);
            activeStudentId = student.getId();
            return student;
        }
        Student saved = studentDAO.save(student);
        activeStudentId = saved.getId();
        return saved;
    }

    public void deleteStudent(int studentId) {
        studentDAO.delete(studentId);
        if (activeStudentId != null && activeStudentId == studentId) {
            activeStudentId = null;
        }
    }

    private void validate(Student student) {
        ValidationUtils.requireNonBlank(student.getFullName(), "Full Name");
        ValidationUtils.requirePositive(student.getRequiredHours(), "Required OJT Hours");
        if (student.getOjtStartDate() != null && student.getExpectedEndDate() != null
                && student.getExpectedEndDate().isBefore(student.getOjtStartDate())) {
            throw new ValidationUtils.ValidationException("Expected End Date cannot be before the OJT Start Date.");
        }
    }
}