package com.ojttracker.ui;

import com.ojttracker.components.GlassPanel;
import com.ojttracker.components.ModernTable;
import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.Theme;
import com.ojttracker.model.OJTRecord;
import com.ojttracker.model.Student;
import com.ojttracker.service.OJTRecordService;
import com.ojttracker.service.StudentService;
import com.ojttracker.util.DateUtils;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class OJTRecordsPanel extends JPanel {
    private final StudentService studentService; private final OJTRecordService recordService; private final Runnable onDataChanged;
    private final JTextField searchField=FormFields.textField("Search by date or remarks"); private final JComboBox<String> filterCombo=new JComboBox<>(new String[]{"All Dates","This Week","This Month"}); private final JComboBox<Student> studentSelector=new JComboBox<>();
    private DefaultTableModel model; private ModernTable table; private List<OJTRecord> records=List.of(); private boolean refreshing;
    public OJTRecordsPanel(StudentService s,OJTRecordService r,Runnable changed){studentService=s;recordService=r;onDataChanged=changed;setOpaque(false);setLayout(new BorderLayout(0,16));build();}
    private void build(){
        JLabel title=new JLabel("OJT Records");title.setFont(Theme.FONT_TITLE);title.setForeground(Theme.TEXT_PRIMARY);RoundedButton add=new RoundedButton("+ Add OJT Record",RoundedButton.Style.PRIMARY);add.addActionListener(e->openAddDialog());
        studentSelector.setPreferredSize(new Dimension(220,40));studentSelector.setRenderer(new DefaultListCellRenderer(){public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){super.getListCellRendererComponent(l,v,i,s,f);if(v instanceof Student x)setText(x.getFullName()==null||x.getFullName().isBlank()?"Select student":x.getFullName());return this;}});studentSelector.addActionListener(e->{if(!refreshing&&studentSelector.getSelectedItem() instanceof Student x){studentService.setActiveStudent(x.getId());applyFilters();}});
        JPanel header=new JPanel(new BorderLayout());header.setOpaque(false);header.add(title,BorderLayout.WEST);JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));actions.setOpaque(false);actions.add(studentSelector);actions.add(add);header.add(actions,BorderLayout.EAST);
        JPanel toolbar=new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));toolbar.setOpaque(false);searchField.setPreferredSize(new Dimension(280,36));filterCombo.setPreferredSize(new Dimension(160,36));toolbar.add(new JLabel("🔍"));toolbar.add(searchField);toolbar.add(new JLabel("Filter:"));toolbar.add(filterCombo);DocumentListener listener=new DocumentListener(){public void insertUpdate(DocumentEvent e){applyFilters();}public void removeUpdate(DocumentEvent e){applyFilters();}public void changedUpdate(DocumentEvent e){applyFilters();}};searchField.getDocument().addDocumentListener(listener);filterCombo.addActionListener(e->applyFilters());
        model=new DefaultTableModel(new Object[]{"ID","Date","Morning In","Morning Out","Afternoon In","Afternoon Out","Break","Total Hours","Remarks","Actions"},0){public boolean isCellEditable(int r,int c){return c==9;}};table=new ModernTable(model);table.getColumnModel().getColumn(0).setMinWidth(0);table.getColumnModel().getColumn(0).setMaxWidth(0);table.getColumnModel().getColumn(0).setWidth(0);table.getColumnModel().getColumn(9).setCellRenderer(new ActionsRenderer());table.getColumnModel().getColumn(9).setCellEditor(new ActionsEditor());table.setRowHeight(38);
        JScrollPane scroll=new JScrollPane(table);scroll.setOpaque(false);scroll.getViewport().setOpaque(false);scroll.setBorder(BorderFactory.createEmptyBorder());GlassPanel card=new GlassPanel();card.setLayout(new BorderLayout(0,12));card.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));card.add(toolbar,BorderLayout.NORTH);card.add(scroll,BorderLayout.CENTER);add(header,BorderLayout.NORTH);add(card,BorderLayout.CENTER);
    }
    public void refresh(){if(refreshing)return;refreshing=true;try{DefaultComboBoxModel<Student> m=new DefaultComboBoxModel<>();for(Student s:studentService.getStudents())m.addElement(s);studentSelector.setModel(m);studentService.getStudent().ifPresent(s->studentSelector.setSelectedItem(s));applyFilters();}finally{refreshing=false;}}
    private void applyFilters(){Optional<Student> s=studentService.getStudent();records=s.isEmpty()?List.of():recordService.searchAndFilter(s.get().getId(),searchField.getText(),switch(String.valueOf(filterCombo.getSelectedItem())){case "This Week"->OJTRecordService.DateFilter.thisWeek();case "This Month"->OJTRecordService.DateFilter.thisMonth();default->OJTRecordService.DateFilter.all();});render();}
    private void render(){model.setRowCount(0);for(OJTRecord r:records)model.addRow(new Object[]{r.getId(),DateUtils.formatDate(r.getWorkDate()),DateUtils.formatTime(r.getMorningTimeIn()),DateUtils.formatTime(r.getMorningTimeOut()),DateUtils.formatTime(r.getAfternoonTimeIn()),DateUtils.formatTime(r.getAfternoonTimeOut()),r.getBreakHours(),r.getTotalHours(),r.getRemarks(),"Actions"});}
    private void openAddDialog(){Optional<Student>s=studentService.getStudent();if(s.isEmpty()){JOptionPane.showMessageDialog(this,"Please set up your Student profile first.","Student Profile Required",JOptionPane.WARNING_MESSAGE);return;}new AddEditRecordDialog((Frame)SwingUtilities.getWindowAncestor(this),recordService,s.get().getId(),null,this::onSaved).setVisible(true);}
    private void openEdit(OJTRecord r){studentService.getStudent().ifPresent(s->new AddEditRecordDialog((Frame)SwingUtilities.getWindowAncestor(this),recordService,s.getId(),r,this::onSaved).setVisible(true));}
    private void delete(OJTRecord r){if(JOptionPane.showConfirmDialog(this,"Are you sure you want to delete this OJT record?","Delete OJT Record",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){recordService.deleteRecord(r.getId());onSaved();}}
    private void onSaved(){applyFilters();onDataChanged.run();} private OJTRecord row(int view){int id=(int)model.getValueAt(table.convertRowIndexToModel(view),0);return records.stream().filter(r->r.getId()==id).findFirst().orElse(null);} private void stop(){if(table.isEditing())table.getCellEditor().stopCellEditing();}
    private JPanel buttons(Integer row){JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,6,2));p.setOpaque(false);RoundedButton edit=new RoundedButton("Edit",RoundedButton.Style.SECONDARY),del=new RoundedButton("Delete",RoundedButton.Style.DANGER);if(row!=null){edit.addActionListener(e->{OJTRecord r=row(row);stop();if(r!=null)openEdit(r);});del.addActionListener(e->{OJTRecord r=row(row);stop();if(r!=null)delete(r);});}p.add(edit);p.add(del);return p;}
    private class ActionsRenderer extends DefaultTableCellRenderer{public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean h,int r,int c){return buttons(null);}}
    private class ActionsEditor extends DefaultCellEditor{ActionsEditor(){super(new JCheckBox());}public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){return buttons(r);}public Object getCellEditorValue(){return "Actions";}}
}
