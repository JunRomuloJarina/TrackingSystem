package com.ojttracker.dao;

import com.ojttracker.model.OJTRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OJTRecordDAO {
    OJTRecord save(OJTRecord record);
    List<OJTRecord> findAll(int studentId);
    Optional<OJTRecord> findById(int id);
    Optional<OJTRecord> findByStudentAndDate(int studentId, LocalDate date);
    void update(OJTRecord record);
    void delete(int id);
    double sumTotalHours(int studentId);
    int countRecords(int studentId);
    List<OJTRecord> findRecent(int studentId, int limit);
}
