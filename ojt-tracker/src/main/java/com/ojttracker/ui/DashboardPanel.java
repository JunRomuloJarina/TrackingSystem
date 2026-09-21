package com.ojttracker.ui;

import com.ojttracker.components.GlassPanel;
import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.StatCard;
import com.ojttracker.components.Theme;
import com.ojttracker.model.OJTRecord;
import com.ojttracker.model.Student;
import com.ojttracker.service.DashboardService;
import com.ojttracker.service.StudentService;
import com.ojttracker.util.DateUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final DashboardService dashboardService; private final StudentService studentService; private final Runnable onViewAllRecords; private final JComboBox<Student> studentSelector=new JComboBox<>(); private boolean refreshing; private StatCard required,completed,remaining,progress; private JLabel status,title; private DefaultTableModel recentModel;
    public DashboardPanel(StudentService s,DashboardService d,Runnable view){studentService=s;dashboardService=d;onViewAllRecords=view;setOpaque(false);setLayout(new BorderLayout(0,20));build();}
    private void build(){title=new JLabel("Dashboard");title.setFont(Theme.FONT_TITLE);title.setForeground(Theme.TEXT_PRIMARY);status=new JLabel("● NOT STARTED");status.setForeground(Theme.TEXT_SECONDARY);studentSelector.addActionListener(e->{if(!refreshing&&studentSelector.getSelectedItem() instanceof Student s){studentService.setActiveStudent(s.getId());refresh();}});JPanel h=new JPanel(new BorderLayout());h.setOpaque(false);h.add(title,BorderLayout.WEST);JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT));right.setOpaque(false);right.add(studentSelector);right.add(status);h.add(right,BorderLayout.EAST);
        JPanel cards=new JPanel(new GridLayout(1,4,16,0));cards.setOpaque(false);required=new StatCard("Required Hours","0.0 hrs"," ",Theme.TEXT_PRIMARY);completed=new StatCard("Completed","0.0 hrs"," ",Theme.PRIMARY_ACCENT);remaining=new StatCard("Remaining","0.0 hrs"," ",Theme.WARNING);progress=new StatCard("Progress","0.0%"," ",Theme.SUCCESS);cards.add(required);cards.add(completed);cards.add(remaining);cards.add(progress);
        recentModel=new DefaultTableModel(new Object[]{"Date","Morning In","Morning Out","Afternoon In","Afternoon Out","Break","Hours"},0){public boolean isCellEditable(int r,int c){return false;}};JTable recent=new JTable(recentModel);JScrollPane scroll=new JScrollPane(recent);GlassPanel card=new GlassPanel();card.setLayout(new BorderLayout(0,10));card.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));JPanel rh=new JPanel(new BorderLayout());rh.setOpaque(false);JLabel rt=new JLabel("RECENT OJT RECORDS");rt.setForeground(Theme.TEXT_SECONDARY);RoundedButton view=new RoundedButton("View All Records →",RoundedButton.Style.SECONDARY);view.addActionListener(e->onViewAllRecords.run());rh.add(rt,BorderLayout.WEST);rh.add(view,BorderLayout.EAST);card.add(rh,BorderLayout.NORTH);card.add(scroll,BorderLayout.CENTER);JPanel body=new JPanel(new BorderLayout(0,16));body.setOpaque(false);body.add(cards,BorderLayout.NORTH);body.add(card,BorderLayout.CENTER);add(h,BorderLayout.NORTH);add(body,BorderLayout.CENTER);}
    public void refresh(){if(refreshing)return;refreshing=true;try{DefaultComboBoxModel<Student> m=new DefaultComboBoxModel<>();for(Student s:studentService.getStudents())m.addElement(s);studentSelector.setModel(m);Student s=studentService.getStudent().orElse(null);if(s!=null)studentSelector.setSelectedItem(s);DashboardService.Snapshot snap=s==null?dashboardService.buildSnapshot():dashboardService.buildSnapshot(s.getId());title.setText(s==null?"Dashboard":"Dashboard - "+s.getFullName());required.setValue(snap.requiredHours()+" hrs");completed.setValue(snap.completedHours()+" hrs");remaining.setValue(snap.remainingHours()+" hrs");progress.setValue(snap.progressPercent()+"%");status.setText("● "+DashboardService.statusLabel(snap.status()));recentModel.setRowCount(0);for(OJTRecord r:snap.recentRecords())recentModel.addRow(new Object[]{DateUtils.formatDate(r.getWorkDate()),DateUtils.formatTime(r.getMorningTimeIn()),DateUtils.formatTime(r.getMorningTimeOut()),DateUtils.formatTime(r.getAfternoonTimeIn()),DateUtils.formatTime(r.getAfternoonTimeOut()),r.getBreakHours(),r.getTotalHours()});}finally{refreshing=false;}}
}
