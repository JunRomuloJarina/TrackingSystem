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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;
import java.util.Optional;

/**
 * Dedicated OJT Records management screen: add / edit / delete / search /
 * filter / sort, backed entirely by {@link OJTRecordService}.
 */
public class OJTRecordsPanel extends JPanel {

    private final StudentService studentService;
    private final OJTRecordService recordService;
    private final Runnable onDataChanged;

    private final javax.swing.JTextField searchField = FormFields.textField("Search by date or remarks");
    private final JComboBox<String> filterCombo = new JComboBox<>(
            new String[]{"All Dates", "This Week", "This Month"});
    private final JComboBox<Student> studentSelector = new JComboBox<>();
    private boolean refreshing;

    private DefaultTableModel tableModel;
    private ModernTable table;
    private List<OJTRecord> currentRecords = List.of();

    private final CardLayout cardLayout = new CardLayout();
    private JPanel cardsContainer;
    private static final String CARD_TABLE = "TABLE";
    private static final String CARD_EMPTY = "EMPTY";

    public OJTRecordsPanel(StudentService studentService, OJTRecordService recordService, Runnable onDataChanged) {
        this.studentService = studentService;
        this.recordService = recordService;
        this.onDataChanged = onDataChanged;
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

        studentSelector.setPreferredSize(new java.awt.Dimension(220, 40));
        studentSelector.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                                                                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Student) {
                    Student student = (Student) value;
                    setText(student.getFullName() == null || student.getFullName().isBlank()
                            ? "Select student" : student.getFullName());
                }
                return this;
            }
        });
        studentSelector.addActionListener(e -> {
            if (refreshing) {
                return;
            }
            Student selected = (Student) studentSelector.getSelectedItem();
            if (selected != null) {
                studentService.setActiveStudent(selected.getId());
                applyFilters();
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);
        headerActions.add(studentSelector);
        headerActions.add(addButton);
        header.add(headerActions, BorderLayout.EAST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        toolbar.setOpaque(false);
        searchField.setPreferredSize(new java.awt.Dimension(280, 36));
        JLabel searchIcon = new JLabel("🔍");
        filterCombo.setPreferredSize(new java.awt.Dimension(160, 36));
        toolbar.add(searchIcon);
        toolbar.add(searchField);
        toolbar.add(new JLabel("Filter:"));
        toolbar.add(filterCombo);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        filterCombo.addActionListener(e -> applyFilters());

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Date", "Time In", "Time Out", "Break", "Total Hours", "Remarks", "Actions"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };
        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionsEditor());
        table.setRowHeight(38);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JLabel emptyLabel = new JLabel("<html><div style='text-align:center'>No OJT records yet.<br>"
                + "Start tracking your OJT hours by adding your first attendance record.<br><br>"
                + "[ + Add OJT Record ]</div></html>", SwingConstants.CENTER);
        emptyLabel.setFont(Theme.FONT_BODY);
        emptyLabel.setForeground(Theme.TEXT_SECONDARY);

        cardsContainer = new JPanel(cardLayout);
        cardsContainer.setOpaque(false);
        cardsContainer.add(scrollPane, CARD_TABLE);
        cardsContainer.add(emptyLabel, CARD_EMPTY);

        GlassPanel tableCard = new GlassPanel();
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        tableCard.add(toolbar, BorderLayout.NORTH);
        tableCard.add(cardsContainer, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    /** Reloads records for the current student and re-applies search/filter. */
    public void refresh() {
        if (refreshing) {
            return;
        }
        refreshing = true;
        try {
            List<Student> students = studentService.getStudents();
            DefaultComboBoxModel<Student> model = new DefaultComboBoxModel<>();
            for (Student student : students) {
                model.addElement(student);
            }
            studentSelector.setModel(model);
            studentService.getStudent().ifPresent(student -> studentSelector.setSelectedItem(student));
            applyFilters();
        } finally {
            refreshing = false;
        }
    }

    private void applyFilters() {
        Optional<Student> studentOpt = studentService.getStudent();
        if (studentOpt.isEmpty()) {
            currentRecords = List.of();
            renderRows();
            return;
        }
        int studentId = studentOpt.get().getId();
        OJTRecordService.DateFilter filter = switch (filterCombo.getSelectedItem() == null ? "" :
                filterCombo.getSelectedItem().toString()) {
            case "This Week" -> OJTRecordService.DateFilter.thisWeek();
            case "This Month" -> OJTRecordService.DateFilter.thisMonth();
            default -> OJTRecordService.DateFilter.all();
        };
        currentRecords = recordService.searchAndFilter(studentId, searchField.getText(), filter);
        renderRows();
    }

    private void renderRows() {
        tableModel.setRowCount(0);
        for (OJTRecord r : currentRecords) {
            tableModel.addRow(new Object[]{
                    r.getId(),
                    DateUtils.formatDate(r.getWorkDate()),
                    DateUtils.formatTime(r.getTimeIn()),
                    DateUtils.formatTime(r.getTimeOut()),
                    r.getBreakHours(),
                    r.getTotalHours(),
                    r.getRemarks(),
                    "Actions"
            });
        }
        cardLayout.show(cardsContainer, currentRecords.isEmpty() ? CARD_EMPTY : CARD_TABLE);
    }

    private void openAddDialog() {
        Optional<Student> studentOpt = studentService.getStudent();
        if (studentOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please set up your Student profile first.",
                    "Student Profile Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AddEditRecordDialog dialog = new AddEditRecordDialog(
                (Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                recordService, studentOpt.get().getId(), null, this::onSaved);
        dialog.setVisible(true);
    }

    private void openEditDialog(OJTRecord record) {
        Optional<Student> studentOpt = studentService.getStudent();
        if (studentOpt.isEmpty()) {
            return;
        }
        AddEditRecordDialog dialog = new AddEditRecordDialog(
                (Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                recordService, studentOpt.get().getId(), record, this::onSaved);
        dialog.setVisible(true);
    }

    private void confirmDelete(OJTRecord record) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this OJT record?",
                "Delete OJT Record", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            recordService.deleteRecord(record.getId());
            onSaved();
        }
    }

    private void onSaved() {
        applyFilters();
        onDataChanged.run();
    }

    private OJTRecord findRecordAtViewRow(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        return currentRecords.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    /** Renders a small "Edit" / "Delete" button pair inside the Actions column. */
    private class ActionsRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                                                                  boolean isSelected, boolean hasFocus,
                                                                  int row, int column) {
            return actionsPanel(null);
        }
    }

    private class ActionsEditor extends javax.swing.DefaultCellEditor {
        private JPanel panel;
        private int editingRow;

        ActionsEditor() {
            super(new javax.swing.JCheckBox());
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value,
                                                                boolean isSelected, int row, int column) {
            editingRow = row;
            panel = actionsPanel(row);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }

    private JPanel actionsPanel(Integer viewRow) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        panel.setOpaque(false);
        RoundedButton edit = new RoundedButton("Edit", RoundedButton.Style.SECONDARY);
        edit.setMargin(new java.awt.Insets(2, 10, 2, 10));
        RoundedButton delete = new RoundedButton("Delete", RoundedButton.Style.DANGER);
        delete.setMargin(new java.awt.Insets(2, 10, 2, 10));
        if (viewRow != null) {
            edit.addActionListener(e -> {
                OJTRecord record = findRecordAtViewRow(viewRow);
                stopEditing();
                if (record != null) {
                    openEditDialog(record);
                }
            });
            delete.addActionListener(e -> {
                OJTRecord record = findRecordAtViewRow(viewRow);
                stopEditing();
                if (record != null) {
                    confirmDelete(record);
                }
            });
        }
        panel.add(edit);
        panel.add(delete);
        return panel;
    }

    private void stopEditing() {
        if (table.isEditing() && table.getCellEditor() != null) {
            table.getCellEditor().stopCellEditing();
        }
    }
}