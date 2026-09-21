package com.ojttracker.ui;

import com.ojttracker.components.GlassPanel;
import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.Theme;
import com.ojttracker.model.OJTRecord;
import com.ojttracker.model.Student;
import com.ojttracker.service.DashboardService;
import com.ojttracker.service.OJTRecordService;
import com.ojttracker.service.StudentService;
import com.ojttracker.util.DateUtils;
import com.ojttracker.util.PrintUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final StudentService studentService; private final OJTRecordService recordService; private final DashboardService dashboardService; private JLabel studentInfo,hours; private DefaultTableModel model;
    public ReportsPanel(StudentService s,OJTRecordService r,DashboardService d){studentService=s;recordService=r;dashboardService=d;setOpaque(false);setLayout(new BorderLayout(0,16));build();}
    private void build(){JLabel title=new JLabel("Reports");title.setFont(Theme.FONT_TITLE);title.setForeground(Theme.TEXT_PRIMARY);RoundedButton print=new RoundedButton("🖨  Print Report",RoundedButton.Style.PRIMARY);print.addActionListener(e->printReport());JPanel header=new JPanel(new BorderLayout());header.setOpaque(false);header.add(title,BorderLayout.WEST);header.add(print,BorderLayout.EAST);GlassPanel info=new GlassPanel();info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));info.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));studentInfo=new JLabel();hours=new JLabel();hours.setForeground(Theme.PRIMARY_ACCENT);info.add(new JLabel("OJT ATTENDANCE REPORT"));info.add(studentInfo);info.add(hours);model=new DefaultTableModel(new Object[]{"Date","Morning In","Morning Out","Afternoon In","Afternoon Out","Break","Hours","Remarks"},0){public boolean isCellEditable(int r,int c){return false;}};JTable table=new JTable(model);JScrollPane scroll=new JScrollPane(table);GlassPanel card=new GlassPanel();card.setLayout(new BorderLayout());card.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));card.add(scroll);add(header,BorderLayout.NORTH);add(info,BorderLayout.CENTER);add(card,BorderLayout.SOUTH);}
    public void refresh(){Optional<Student> o=studentService.getStudent();if(o.isEmpty()){studentInfo.setText("No student profile set up yet.");hours.setText("");model.setRowCount(0);return;}Student s=o.get();DashboardService.Snapshot snap=dashboardService.buildSnapshot(s.getId());studentInfo.setText("Student: "+safe(s.getFullName())+" | Course: "+safe(s.getCourse()));hours.setText(String.format("Required: %.1f hrs • Completed: %.1f hrs • Remaining: %.1f hrs • Progress: %.1f%%",snap.requiredHours(),snap.completedHours(),snap.remainingHours(),snap.progressPercent()));model.setRowCount(0);for(OJTRecord r:recordService.getAllRecords(s.getId()))model.addRow(new Object[]{DateUtils.formatDate(r.getWorkDate()),DateUtils.formatTime(r.getMorningTimeIn()),DateUtils.formatTime(r.getMorningTimeOut()),DateUtils.formatTime(r.getAfternoonTimeIn()),DateUtils.formatTime(r.getAfternoonTimeOut()),r.getBreakHours(),r.getTotalHours(),r.getRemarks()});}
    private void printReport(){studentService.getStudent().ifPresentOrElse(s->PrintUtils.printReport(s,dashboardService.buildSnapshot(s.getId()),recordService.getAllRecords(s.getId())),()->JOptionPane.showMessageDialog(this,"Please set up your Student profile first.","Student Profile Required",JOptionPane.WARNING_MESSAGE));} private String safe(String s){return s==null||s.isBlank()?"—":s;}
}
