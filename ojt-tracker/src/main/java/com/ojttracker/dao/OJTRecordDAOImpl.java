package com.ojttracker.dao;

import com.ojttracker.model.OJTRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-backed implementation of {@link OJTRecordDAO}.
 */
public class OJTRecordDAOImpl implements OJTRecordDAO {

    private final Connection connection;

    public OJTRecordDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public OJTRecord save(OJTRecord record) {
        String sql = """
            INSERT INTO ojt_records
                (student_id, work_date, time_in, time_out, break_hours, total_hours, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindRecord(ps, record);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    record.setId(keys.getInt(1));
                }
            }
            return record;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save OJT record.", e);
        }
    }

    @Override
    public List<OJTRecord> findAll(int studentId) {
        String sql = "SELECT * FROM ojt_records WHERE student_id = ? ORDER BY work_date DESC, id DESC";
        List<OJTRecord> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load OJT records.", e);
        }
    }

    @Override
    public Optional<OJTRecord> findById(int id) {
        String sql = "SELECT * FROM ojt_records WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load OJT record with id " + id, e);
        }
    }

    @Override
    public Optional<OJTRecord> findByStudentAndDate(int studentId, LocalDate date) {
        String sql = "SELECT * FROM ojt_records WHERE student_id = ? AND work_date = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check existing record for date " + date, e);
        }
    }

    @Override
    public void update(OJTRecord record) {
        String sql = """
            UPDATE ojt_records SET
                work_date = ?, time_in = ?, time_out = ?, break_hours = ?, total_hours = ?, remarks = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, record.getWorkDate().toString());
            ps.setString(2, record.getTimeIn().toString());
            ps.setString(3, record.getTimeOut().toString());
            ps.setDouble(4, record.getBreakHours());
            ps.setDouble(5, record.getTotalHours());
            ps.setString(6, record.getRemarks());
            ps.setInt(7, record.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update OJT record.", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM ojt_records WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete OJT record.", e);
        }
    }

    @Override
    public double sumTotalHours(int studentId) {
        String sql = "SELECT COALESCE(SUM(total_hours), 0) AS total FROM ojt_records WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("total") : 0.0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to sum OJT hours.", e);
        }
    }

    @Override
    public int countRecords(int studentId) {
        String sql = "SELECT COUNT(*) AS cnt FROM ojt_records WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count OJT records.", e);
        }
    }

    @Override
    public List<OJTRecord> findRecent(int studentId, int limit) {
        String sql = "SELECT * FROM ojt_records WHERE student_id = ? ORDER BY work_date DESC, id DESC LIMIT ?";
        List<OJTRecord> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load recent OJT records.", e);
        }
    }

    private void bindRecord(PreparedStatement ps, OJTRecord record) throws SQLException {
        ps.setInt(1, record.getStudentId());
        ps.setString(2, record.getWorkDate().toString());
        ps.setString(3, record.getTimeIn().toString());
        ps.setString(4, record.getTimeOut().toString());
        ps.setDouble(5, record.getBreakHours());
        ps.setDouble(6, record.getTotalHours());
        ps.setString(7, record.getRemarks());
    }

    private OJTRecord mapRow(ResultSet rs) throws SQLException {
        OJTRecord record = new OJTRecord();
        record.setId(rs.getInt("id"));
        record.setStudentId(rs.getInt("student_id"));
        record.setWorkDate(LocalDate.parse(rs.getString("work_date")));
        record.setTimeIn(LocalTime.parse(rs.getString("time_in")));
        record.setTimeOut(LocalTime.parse(rs.getString("time_out")));
        record.setBreakHours(rs.getDouble("break_hours"));
        record.setTotalHours(rs.getDouble("total_hours"));
        record.setRemarks(rs.getString("remarks"));
        return record;
    }
}