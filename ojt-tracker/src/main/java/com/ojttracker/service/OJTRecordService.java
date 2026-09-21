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

public class OJTRecordService {
    private final OJTRecordDAO recordDAO;
    public OJTRecordService(OJTRecordDAO recordDAO) { this.recordDAO = recordDAO; }

    public OJTRecord addRecord(int studentId, LocalDate date, LocalTime morningIn, LocalTime morningOut,
                               LocalTime afternoonIn, LocalTime afternoonOut, double breakHours, String remarks) {
        ValidationUtils.requireValidDate(date, "Date");
        if (recordDAO.findByStudentAndDate(studentId, date).isPresent())
            throw new ValidationUtils.ValidationException("An OJT record already exists for this date.");
        OJTRecord r = build(0, studentId, date, morningIn, morningOut, afternoonIn, afternoonOut, breakHours, remarks);
        return recordDAO.save(r);
    }

    public void updateRecord(int id, int studentId, LocalDate date, LocalTime morningIn, LocalTime morningOut,
                             LocalTime afternoonIn, LocalTime afternoonOut, double breakHours, String remarks) {
        ValidationUtils.requireValidDate(date, "Date");
        Optional<OJTRecord> same = recordDAO.findByStudentAndDate(studentId, date);
        if (same.isPresent() && same.get().getId() != id)
            throw new ValidationUtils.ValidationException("An OJT record already exists for this date.");
        recordDAO.update(build(id, studentId, date, morningIn, morningOut, afternoonIn, afternoonOut, breakHours, remarks));
    }

    private OJTRecord build(int id, int studentId, LocalDate date, LocalTime mi, LocalTime mo, LocalTime ai, LocalTime ao, double br, String remarks) {
        OJTRecord r = new OJTRecord(); r.setId(id); r.setStudentId(studentId); r.setWorkDate(date);
        r.setMorningTimeIn(mi); r.setMorningTimeOut(mo); r.setAfternoonTimeIn(ai); r.setAfternoonTimeOut(ao);
        r.setBreakHours(br); r.setTotalHours(ValidationUtils.calculateAndValidateHours(mi, mo, ai, ao, br)); r.setRemarks(remarks); return r;
    }
    public void deleteRecord(int id) { recordDAO.delete(id); }
    public List<OJTRecord> getAllRecords(int sid) { return recordDAO.findAll(sid); }
    public List<OJTRecord> getRecentRecords(int sid, int limit) { return recordDAO.findRecent(sid, limit); }
    public double getTotalHours(int sid) { return recordDAO.sumTotalHours(sid); }
    public int getRecordCount(int sid) { return recordDAO.countRecords(sid); }
    public List<OJTRecord> searchAndFilter(int sid, String text, DateFilter filter) {
        LocalDate today=LocalDate.now(); return recordDAO.findAll(sid).stream().filter(r -> matchesDate(r,filter,today)).filter(r -> text==null||text.isBlank()||r.getRemarks()!=null&&r.getRemarks().toLowerCase().contains(text.trim().toLowerCase())||r.getWorkDate().toString().contains(text.trim())).sorted(Comparator.comparing(OJTRecord::getWorkDate).reversed()).collect(Collectors.toList());
    }
    private boolean matchesDate(OJTRecord r, DateFilter f, LocalDate t) { if(f==null||f.type()==DateFilterType.ALL)return true; LocalDate d=r.getWorkDate(); return switch(f.type()){case THIS_WEEK -> !d.isBefore(t.minusDays(t.getDayOfWeek().getValue()-1))&&!d.isAfter(t.minusDays(t.getDayOfWeek().getValue()-1).plusDays(6));case THIS_MONTH->d.getMonth()==t.getMonth()&&d.getYear()==t.getYear();case CUSTOM_RANGE->(f.start()==null||!d.isBefore(f.start()))&&(f.end()==null||!d.isAfter(f.end()));case ALL->true;}; }
    public enum DateFilterType { ALL, THIS_WEEK, THIS_MONTH, CUSTOM_RANGE }
    public record DateFilter(DateFilterType type, LocalDate start, LocalDate end) { public static DateFilter all(){return new DateFilter(DateFilterType.ALL,null,null);} public static DateFilter thisWeek(){return new DateFilter(DateFilterType.THIS_WEEK,null,null);} public static DateFilter thisMonth(){return new DateFilter(DateFilterType.THIS_MONTH,null,null);} public static DateFilter range(LocalDate s,LocalDate e){return new DateFilter(DateFilterType.CUSTOM_RANGE,s,e);} }
}
