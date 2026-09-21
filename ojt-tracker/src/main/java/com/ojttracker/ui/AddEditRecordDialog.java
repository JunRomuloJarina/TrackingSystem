package com.ojttracker/ui;

import com.ojttracker.components.RoundedButton;
import com.ojttracker.components.Theme;
import com.ojttracker.model.OJTRecord;
import com.ojttracker.service.OJTRecordService;
import com.ojttracker.util.DateUtils;
import com.ojttracker.util.ValidationUtils;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class AddEditRecordDialog extends JDialog {
    private final OJTRecordService service; private final int studentId; private final OJTRecord existing; private final Runnable saved;
    private final JComboBox<LocalDate> date=FormFields.dateComboBox(DateUtils.buildDateOptions(LocalDate.now().minusYears(10),3650),LocalDate.now());
    private final JComboBox<LocalTime> morningIn=FormFields.timeComboBox(DateUtils.buildTimeOptions(30),LocalTime.of(8,0));
    private final JComboBox<LocalTime> morningOut=FormFields.timeComboBox(DateUtils.buildTimeOptions(30),LocalTime.of(12,0));
    private final JComboBox<LocalTime> afternoonIn=FormFields.timeComboBox(DateUtils.buildTimeOptions(30),LocalTime.of(13,0));
    private final JComboBox<LocalTime> afternoonOut=FormFields.timeComboBox(DateUtils.buildTimeOptions(30),LocalTime.of(17,0));
    private final JTextField breakField=FormFields.textField("hours, e.g. 1.0"),remarks=FormFields.textField();
    public AddEditRecordDialog(Frame owner,OJTRecordService service,int studentId,OJTRecord existing,Runnable saved){super(owner,existing==null?"Add OJT Record":"Edit OJT Record",true);this.service=service;this.studentId=studentId;this.existing=existing;this.saved=saved;getContentPane().setBackground(Theme.SECONDARY);setLayout(new BorderLayout());build();prefill();pack();setLocationRelativeTo(owner);}
    private void build(){JPanel f=new JPanel(new GridBagLayout());f.setBackground(Theme.SECONDARY);f.setBorder(BorderFactory.createEmptyBorder(24,28,12,28));GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(8,8,8,8);g.fill=GridBagConstraints.HORIZONTAL;int r=0;r=FormFields.addRow(f,g,r,"Date *",date);r=FormFields.addRow(f,g,r,"Morning Time In *",morningIn);r=FormFields.addRow(f,g,r,"Morning Time Out *",morningOut);r=FormFields.addRow(f,g,r,"Afternoon Time In",afternoonIn);r=FormFields.addRow(f,g,r,"Afternoon Time Out",afternoonOut);r=FormFields.addRow(f,g,r,"Break (hours)",breakField);r=FormFields.addRow(f,g,r,"Remarks",remarks);JLabel note=new JLabel("Leave both afternoon fields empty if absent.");note.setForeground(Theme.TEXT_SECONDARY);g.gridx=1;g.gridy=r;f.add(note,g);RoundedButton cancel=new RoundedButton("Cancel",RoundedButton.Style.SECONDARY),save=new RoundedButton("Save Record",RoundedButton.Style.PRIMARY);cancel.addActionListener(e->dispose());save.addActionListener(e->save());JPanel b=new JPanel(new FlowLayout(FlowLayout.RIGHT));b.setBackground(Theme.SECONDARY);b.add(cancel);b.add(save);add(f,BorderLayout.CENTER);add(b,BorderLayout.SOUTH);setMinimumSize(new Dimension(540,500));}
    private void prefill(){if(existing==null){breakField.setText("0.0");set(afternoonIn,null);set(afternoonOut,null);return;}date.setSelectedItem(existing.getWorkDate());set(morningIn,existing.getMorningTimeIn());set(morningOut,existing.getMorningTimeOut());set(afternoonIn,existing.getAfternoonTimeIn());set(afternoonOut,existing.getAfternoonTimeOut());breakField.setText(String.valueOf(existing.getBreakHours()));remarks.setText(existing.getRemarks());}
    private void set(JComboBox<LocalTime> field,LocalTime value){field.getEditor().setItem(value==null?"":DateUtils.formatTime(value));}
    private LocalTime read(JComboBox<LocalTime> field){Object value=field.getEditor().getItem();if(value==null||value.toString().isBlank())return null;return value instanceof LocalTime?(LocalTime)value:DateUtils.parseTimeFlexible(value.toString());}
    private void save(){try{LocalDate d=FormFields.readDateValue(date.getSelectedItem());LocalTime mi=read(morningIn),mo=read(morningOut),ai=read(afternoonIn),ao=read(afternoonOut);if(d==null)throw new ValidationUtils.ValidationException("Please select a valid date.");double br=breakField.getText().isBlank()?0:Double.parseDouble(breakField.getText().trim());if(existing==null)service.addRecord(studentId,d,mi,mo,ai,ao,br,remarks.getText().trim());else service.updateRecord(existing.getId(),studentId,d,mi,mo,ai,ao,br,remarks.getText().trim());saved.run();dispose();}catch(ValidationUtils.ValidationException|NumberFormatException e){JOptionPane.showMessageDialog(this,e.getMessage(),"Please check your input",JOptionPane.WARNING_MESSAGE);}catch(DateTimeParseException e){JOptionPane.showMessageDialog(this,"Please enter a valid date or time.","Invalid Input",JOptionPane.WARNING_MESSAGE);}}
}
