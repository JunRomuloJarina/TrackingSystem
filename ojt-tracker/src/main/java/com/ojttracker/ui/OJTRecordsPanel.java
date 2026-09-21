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

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.util.List;
import java.util.Optional;

/** Attendance records screen with morning and afternoon timekeeping columns. */
public class OJTRecordsPanel extends JPanel {
    private static final int ID_COLUMN = 0;
    private static final int ACTIONS_COLUMN = 9;

    private final StudentService studentService;
    private final OJTRecordService recordService;
    private final Runnable onDataChanged;
    private final JTextField searchField = FormFields.textField("Search by date or remarks");
    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{"All Dates", "This Week", "This Month"});
    private final JComboBox<Student> studentSelector = new JComboBox<>();

    private DefaultTableModel tableModel;
    private ModernTable table;
    private List<OJTRecord> currentRecords = List.of();
    private boolean refreshing;

    public OJTRecordsPanel(StudentService studentService, OJTRecordService recordService, Runnable onDataChanged) {
        this.studentService = studentService;
        this.recordService = recordService;
        this.onDataChanged = onDataChanged == null ? () -> { } : onDataChanged;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        build();
    }

    private void build() {
        JLabel title = new JLabel("OJT Records");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        RoundedButton addButton = new RoundedButton("+ Add OJT Record", RoundedButton.Style.PRIMARY);
        addButton.addActionListener(e -> openAddDialog());

        studentSelector.setPreferredSize(new Dimension(220, 40));
        studentSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                            boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Student student) {
                    setText(student.getFullName() == null || student.getFullName().isBlank()
                            ? "Select student" : student.getFullName());
                }
                return this;
            }
        });
        studentSelector.addActionListener(e -> {
            if (!refreshing && studentSelector.getSelectedItem() instanceof Student student) {
                studentService.setActiveStudent(student.getId());
                applyFilters();
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(studentSelector);
        actions.add(addButton);
        header.add(actions, BorderLayout.EAST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);
        searchField.setPreferredSize(new Dimension(280, 36));
        filterCombo.setPreferredSize(new Dimension(160, 36));
        toolbar.add(new JLabel("🔍"));
        toolbar.add(searchField);
        toolbar.add(new JLabel("Filter:"));
        toolbar.add(filterCombo);
        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        };
        searchField.getDocument().addDocumentListener(listener);
        filterCombo.addActionListener(e -> applyFilters());

        tableModel = new DefaultTableModel(new Object[]{
                "ID", "Date", "Morning In", "Morning Out", "Afternoon In", "Afternoon Out",
                "Break", "Total Hours", "Remarks", "Actions"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == ACTIONS_COLUMN; }
        };
        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(ID_COLUMN).setMinWidth(0);
        table.getColumnModel().getColumn(ID_COLUMN).setMaxWidth(0);
        table.getColumnModel().getColumn(ID_COLUMN).setWidth(0);
        table.getColumnModel().getColumn(ACTIONS_COLUMN).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(ACTIONS_COLUMN).setCellEditor(new ActionsEditor());
        table.setRowHeight(38);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        GlassPanel card = new GlassPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.add(toolbar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    public void refresh() {
        if (refreshing) return;
        refreshing = true;
        try {
            DefaultComboBoxModel<Student> model = new DefaultComboBoxModel<>();
            for (Student student : studentService.getStudents()) model.addElement(student);
            studentSelector.setModel(model);
            studentService.getStudent().ifPresent(studentSelector::setSelectedItem);
            applyFilters();
        } finally {
            refreshing = false;
        }
    }

    private void applyFilters() {
        Optional<Student> selected = studentService.getStudent();
        if (selected.isEmpty()) {
            currentRecords = List.of();
        } else {
            OJTRecordService.DateFilter filter = switch (String.valueOf(filterCombo.getSelectedItem())) {
                case "This Week" -> OJTRecordService.DateFilter.thisWeek();
                case "This Month" -> OJTRecordService.DateFilter.thisMonth();
                default -> OJTRecordService.DateFilter.all();
            };
            currentRecords = recordService.searchAndFilter(selected.get().getId(), searchField.getText(), filter);
        }
        renderRows();
    }

    private void renderRows() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (OJTRecord record : currentRecords) {
            tableModel.addRow(new Object[]{record.getId(), DateUtils.formatDate(record.getWorkDate()),
                    DateUtils.formatTime(record.getMorningTimeIn()), DateUtils.formatTime(record.getMorningTimeOut()),
                    DateUtils.formatTime(record.getAfternoonTimeIn()), DateUtils.formatTime(record.getAfternoonTimeOut()),
                    record.getBreakHours(), record.getTotalHours(), record.getRemarks(), "Actions"});
        }
    }

    private void openAddDialog() {
        Optional<Student> selected = studentService.getStudent();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please set up your Student profile first.",
                    "Student Profile Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new AddEditRecordDialog((Frame) SwingUtilities.getWindowAncestor(this), recordService,
                selected.get().getId(), null, this::onSaved).setVisible(true);
    }

    private void openEditDialog(OJTRecord record) {
        studentService.getStudent().ifPresent(student -> new AddEditRecordDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), recordService, student.getId(), record,
                this::onSaved).setVisible(true));
    }

    private void confirmDelete(OJTRecord record) {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this OJT record?",
                "Delete OJT Record", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            recordService.deleteRecord(record.getId());
            onSaved();
        }
    }

    private void onSaved() {
        applyFilters();
        onDataChanged.run();
    }

    private OJTRecord recordAtViewRow(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= tableModel.getRowCount()) return null;
        int id = ((Number) tableModel.getValueAt(modelRow, ID_COLUMN)).intValue();
        return currentRecords.stream().filter(record -> record.getId() == id).findFirst().orElse(null);
    }

    private void stopEditing() {
        if (table.isEditing() && table.getCellEditor() != null) table.getCellEditor().stopCellEditing();
    }

    private JPanel actionsPanel(Integer viewRow) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        panel.setOpaque(false);
        JButton edit = new RoundedButton("Edit", RoundedButton.Style.SECONDARY);
        JButton delete = new RoundedButton("Delete", RoundedButton.Style.DANGER);
        edit.setMargin(new Insets(2, 10, 2, 10));
        delete.setMargin(new Insets(2, 10, 2, 10));
        if (viewRow != null) {
            edit.addActionListener(e -> { OJTRecord record = recordAtViewRow(viewRow); stopEditing(); if (record != null) openEditDialog(record); });
            delete.addActionListener(e -> { OJTRecord record = recordAtViewRow(viewRow); stopEditing(); if (record != null) confirmDelete(record); });
        }
        panel.add(edit);
        panel.add(delete);
        return panel;
    }

    private class ActionsRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                                   boolean focus, int row, int column) {
            return actionsPanel(null);
        }
    }

    private class ActionsEditor extends DefaultCellEditor {
        ActionsEditor() { super(new JCheckBox()); }
        @Override public Component getTableCellEditorComponent(JTable t, Object value, boolean selected, int row, int column) {
            return actionsPanel(row);
        }
        @Override public Object getCellEditorValue() { return "Actions"; }
    }
}
