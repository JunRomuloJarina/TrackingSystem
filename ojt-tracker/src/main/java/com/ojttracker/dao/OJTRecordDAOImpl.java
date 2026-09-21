package com.ojttracker.dao;

import com.ojttracker.model.OJTRecord;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class OJTRecordDAOImpl implements OJTRecordDAO {
    private final Connection connection;
    public OJTRecordDAOImpl(Connection connection){this.connection=connection;}
    public OJTRecord save(OJTRecord r){String sql="INSERT INTO ojt_records (student_id,work_date,time_in,time_out,morning_time_in,morning_time_out,afternoon_time_in,afternoon_time_out,break_hours,total_hours,remarks) VALUES (?,?,?,?,?,?,?,?,?,?,?)";try(PreparedStatement p=connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){bind(p,r);p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){if(k.next())r.setId(k.getInt(1));}return r;}catch(SQLException e){throw new RuntimeException("Failed to save OJT record.",e);}}
    public List<OJTRecord> findAll(int sid){return query("SELECT * FROM ojt_records WHERE student_id=? ORDER BY work_date DESC,id DESC",sid);}
    public Optional<OJTRecord> findById(int id){try(PreparedStatement p=connection.prepareStatement("SELECT * FROM ojt_records WHERE id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(map(r)):Optional.empty();}}catch(SQLException e){throw new RuntimeException("Failed to load OJT record.",e);}}
    public Optional<OJTRecord> findByStudentAndDate(int sid,LocalDate date){try(PreparedStatement p=connection.prepareStatement("SELECT * FROM ojt_records WHERE student_id=? AND work_date=?")){p.setInt(1,sid);p.setString(2,date.toString());try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(map(r)):Optional.empty();}}catch(SQLException e){throw new RuntimeException("Failed to check existing record.",e);}}
    public void update(OJTRecord r){String sql="UPDATE ojt_records SET work_date=?,time_in=?,time_out=?,morning_time_in=?,morning_time_out=?,afternoon_time_in=?,afternoon_time_out=?,break_hours=?,total_hours=?,remarks=? WHERE id=?";try(PreparedStatement p=connection.prepareStatement(sql)){p.setString(1,r.getWorkDate().toString());p.setString(2,stored(r.getMorningTimeIn()));p.setString(3,stored(r.getAfternoonTimeOut()));setTimes(p,r,4);p.setDouble(8,r.getBreakHours());p.setDouble(9,r.getTotalHours());p.setString(10,r.getRemarks());p.setInt(11,r.getId());p.executeUpdate();}catch(SQLException e){throw new RuntimeException("Failed to update OJT record.",e);}}
    public void delete(int id){try(PreparedStatement p=connection.prepareStatement("DELETE FROM ojt_records WHERE id=?")){p.setInt(1,id);p.executeUpdate();}catch(SQLException e){throw new RuntimeException("Failed to delete OJT record.",e);}}
    public double sumTotalHours(int sid){try(PreparedStatement p=connection.prepareStatement("SELECT COALESCE(SUM(total_hours),0) FROM ojt_records WHERE student_id=?")){p.setInt(1,sid);try(ResultSet r=p.executeQuery()){return r.next()?r.getDouble(1):0;}}catch(SQLException e){throw new RuntimeException(e);}}
    public int countRecords(int sid){try(PreparedStatement p=connection.prepareStatement("SELECT COUNT(*) FROM ojt_records WHERE student_id=?")){p.setInt(1,sid);try(ResultSet r=p.executeQuery()){return r.next()?r.getInt(1):0;}}catch(SQLException e){throw new RuntimeException(e);}}
    public List<OJTRecord> findRecent(int sid,int limit){return query("SELECT * FROM ojt_records WHERE student_id=? ORDER BY work_date DESC,id DESC LIMIT "+Math.max(1,limit),sid);}
    private List<OJTRecord> query(String sql,int sid){try(PreparedStatement p=connection.prepareStatement(sql)){p.setInt(1,sid);List<OJTRecord> out=new ArrayList<>();try(ResultSet r=p.executeQuery()){while(r.next())out.add(map(r));}return out;}catch(SQLException e){throw new RuntimeException("Failed to load OJT records.",e);}}
    private void bind(PreparedStatement p,OJTRecord r)throws SQLException{p.setInt(1,r.getStudentId());p.setString(2,r.getWorkDate().toString());p.setString(3,stored(r.getMorningTimeIn()));p.setString(4,stored(r.getAfternoonTimeOut()));setTimes(p,r,5);p.setDouble(9,r.getBreakHours());p.setDouble(10,r.getTotalHours());p.setString(11,r.getRemarks());}
    private void setTimes(PreparedStatement p,OJTRecord r,int start)throws SQLException{p.setString(start,stored(r.getMorningTimeIn()));p.setString(start+1,stored(r.getMorningTimeOut()));p.setString(start+2,stored(r.getAfternoonTimeIn()));p.setString(start+3,stored(r.getAfternoonTimeOut()));}
    private String stored(LocalTime value){return value==null?"":value.toString();}
    private OJTRecord map(ResultSet r)throws SQLException{OJTRecord o=new OJTRecord();o.setId(r.getInt("id"));o.setStudentId(r.getInt("student_id"));o.setWorkDate(LocalDate.parse(r.getString("work_date")));o.setMorningTimeIn(parse(r,"morning_time_in","time_in"));o.setMorningTimeOut(parse(r,"morning_time_out",null));o.setAfternoonTimeIn(parse(r,"afternoon_time_in",null));o.setAfternoonTimeOut(parse(r,"afternoon_time_out","time_out"));o.setBreakHours(r.getDouble("break_hours"));o.setTotalHours(r.getDouble("total_hours"));o.setRemarks(r.getString("remarks"));return o;}
    private LocalTime parse(ResultSet r,String primary,String fallback)throws SQLException{String v=r.getString(primary);if(v==null||v.isBlank())v=fallback==null?null:r.getString(fallback);return v==null||v.isBlank()?null:LocalTime.parse(v);}
}
