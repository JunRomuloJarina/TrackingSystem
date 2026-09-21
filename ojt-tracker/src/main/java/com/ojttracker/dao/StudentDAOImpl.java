package com.ojttracker.dao;

import com.ojttracker.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link StudentDAO}.
 */
public class StudentDAOImpl implements StudentDAO {

    private final Connection connection;

    public StudentDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Student save(Student student) {
        String sql = """
            INSERT INTO students
                (student_id, full_name, course, year_level, school, ojt_company,
                 company_address, supervisor, supervisor_contact, ojt_start_date,
                 expected_end_date, required_hours)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindStudent(ps, student);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    student.setId(keys.getInt(1));
                }
            }
            return student;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save student.", e);
        }
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT * FROM students ORDER BY id DESC";
        List<Student> students = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapRow(rs));
            }
            return students;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load student profiles.", e);
        }
    }

    @Override
    public Optional<Student> findFirst() {
        List<Student> students = findAll();
        return students.isEmpty() ? Optional.empty() : Optional.of(students.get(0));
    }

    @Override
    public Optional<Student> findById(int id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load student with id " + id, e);
        }
    }

    @Override
    public void update(Student student) {
        String sql = """
            UPDATE students SET
                student_id = ?, full_name = ?, course = ?, year_level = ?, school = ?,
                ojt_company = ?, company_address = ?, supervisor = ?, supervisor_contact = ?,
                ojt_start_date = ?, expected_end_date = ?, required_hours = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindStudent(ps, student);
            ps.setInt(13, student.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update student.", e);
        }
    }

    @Override
    public void delete(int id) {
        String deleteRecordsSql = "DELETE FROM ojt_records WHERE student_id = ?";
        String deleteStudentSql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement deleteRecords = connection.prepareStatement(deleteRecordsSql);
             PreparedStatement deleteStudent = connection.prepareStatement(deleteStudentSql)) {
            deleteRecords.setInt(1, id);
            deleteRecords.executeUpdate();

            deleteStudent.setInt(1, id);
            deleteStudent.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete student and their records.", e);
        }
    }

    @Override
    public boolean exists() {
        return findFirst().isPresent();
    }

    private void bindStudent(PreparedStatement ps, Student student) throws SQLException {
        ps.setString(1, student.getStudentId());
        ps.setString(2, student.getFullName());
        ps.setString(3, student.getCourse());
        ps.setString(4, student.getYearLevel());
        ps.setString(5, student.getSchool());
        ps.setString(6, student.getOjtCompany());
        ps.setString(7, student.getCompanyAddress());
        ps.setString(8, student.getSupervisor());
        ps.setString(9, student.getSupervisorContact());
        ps.setString(10, student.getOjtStartDate() == null ? null : student.getOjtStartDate().toString());
        ps.setString(11, student.getExpectedEndDate() == null ? null : student.getExpectedEndDate().toString());
        ps.setDouble(12, student.getRequiredHours());
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setStudentId(rs.getString("student_id"));
        student.setFullName(rs.getString("full_name"));
        student.setCourse(rs.getString("course"));
        student.setYearLevel(rs.getString("year_level"));
        student.setSchool(rs.getString("school"));
        student.setOjtCompany(rs.getString("ojt_company"));
        student.setCompanyAddress(rs.getString("company_address"));
        student.setSupervisor(rs.getString("supervisor"));
        student.setSupervisorContact(rs.getString("supervisor_contact"));
        String start = rs.getString("ojt_start_date");
        student.setOjtStartDate(start == null || start.isBlank() ? null : LocalDate.parse(start));
        String end = rs.getString("expected_end_date");
        student.setExpectedEndDate(end == null || end.isBlank() ? null : LocalDate.parse(end));
        student.setRequiredHours(rs.getDouble("required_hours"));
        return student;
    }
}