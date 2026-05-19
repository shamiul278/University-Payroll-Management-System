package upms.ui.panels;

import upms.dao.AttendanceDAO;
import upms.dao.EmployeeDAO;
import upms.model.Attendance;
import upms.model.Employee;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class AttendancePanel extends JPanel {
    private final AttendanceDAO attDAO = new AttendanceDAO();
    private final EmployeeDAO empDAO = new EmployeeDAO();
    private JTable table;
    private DefaultTableModel model;
    private Runnable dataChangeListener = () -> {};
    private static final String[] COLS = {"Attendance ID", "Employee ID", "Employee Name", "Date", "Status"};

    public AttendancePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleBlock = new JPanel(new BorderLayout(0, 3));
        titleBlock.setBackground(Theme.CONTENT_BG);
        JLabel title = Theme.headerLabel("Daily Attendance");
        JLabel sub = new JLabel("Ensure all department staff records are accurate before finalizing for the payroll cycle.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(title, BorderLayout.NORTH);
        titleBlock.add(sub, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.CONTENT_BG);
        JButton addBtn  = Theme.primaryButton("+ Mark Attendance");
        JButton editBtn = Theme.primaryButton("Update");
        JButton delBtn  = Theme.dangerButton("Delete");
        editBtn.setBackground(new Color(0x744210));
        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);

        // Top section: title row + stat cards
        JPanel topSection = new JPanel(new BorderLayout(0, 12));
        topSection.setBackground(Theme.CONTENT_BG);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(buildStatCards(), BorderLayout.CENTER);

        // Table card
        JPanel tableCard = new JPanel(new BorderLayout(0, 0));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)));

        model = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Status badge renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                String status = v != null ? v.toString() : "";
                JLabel badge = Theme.statusBadge(status);
                badge.setHorizontalAlignment(SwingConstants.CENTER);
                if (sel) badge.setBackground(new Color(0xEEF2FF));
                return badge;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        // Bottom action buttons
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(Color.WHITE);
        JButton resetBtn = new JButton("Reset Selection");
        resetBtn.setFont(Theme.FONT_BODY);
        resetBtn.addActionListener(e -> table.clearSelection());
        JButton commitBtn = Theme.primaryButton("Commit Daily Attendance");
        commitBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Daily attendance committed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE));
        bottomBar.add(resetBtn);
        bottomBar.add(commitBtn);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        // Assemble
        setLayout(new BorderLayout(0, 12));
        add(topSection, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildStatCards() {
        int total = 0, present = 0, onLeave = 0;
        try {
            total   = empDAO.getAll().size();
            for (Attendance a : attDAO.getAll()) {
                if ("Present".equals(a.getStatus())) present++;
                else if ("Leave".equals(a.getStatus())) onLeave++;
            }
        } catch (Exception ignored) {}

        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setBackground(Theme.CONTENT_BG);
        row.add(Theme.statCard("TOTAL EMPLOYEES",  String.valueOf(total),   Theme.PRIMARY));
        row.add(Theme.statCard("LOGGED PRESENT",   String.valueOf(present), Theme.SUCCESS));
        row.add(Theme.statCard("ON LEAVE",          String.valueOf(onLeave), Theme.WARNING));
        return row;
    }

    public void refresh() {
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Attendance a : attDAO.getAll()) {
            Employee emp = empDAO.getById(a.getEmpId());
            model.addRow(new Object[]{
                a.getAttendanceId(), a.getEmpId(),
                emp != null ? emp.getName() : "Unknown",
                a.getDate() != null ? sdf.format(a.getDate()) : "",
                a.getStatus()
            });
        }
    }

    public void setDataChangeListener(Runnable listener) {
        dataChangeListener = listener != null ? listener : () -> {};
    }

    private void notifyDataChanged() {
        refresh();
        dataChangeListener.run();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an attendance record to update."); return; }
        String id = (String) model.getValueAt(row, 0);
        for (Attendance a : attDAO.getAll()) if (a.getAttendanceId().equals(id)) { showForm(a); return; }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an attendance record to delete."); return; }
        String id = (String) model.getValueAt(row, 0);
        int c = JOptionPane.showConfirmDialog(this, "Delete attendance record " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            if (attDAO.delete(id)) { JOptionPane.showMessageDialog(this, "Attendance record deleted successfully."); notifyDataChanged(); }
            else JOptionPane.showMessageDialog(this, "Unable to delete attendance record.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showForm(Attendance existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Mark Attendance" : "Update Attendance", true);
        dlg.setSize(420, 280);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 4, 8, 4);

        List<Employee> emps = empDAO.getAll();
        String[] empIds   = emps.stream().map(Employee::getEmpId).toArray(String[]::new);
        String[] empNames = emps.stream().map(e -> e.getEmpId() + " - " + e.getName()).toArray(String[]::new);

        JTextField idF    = Theme.styledField();
        JComboBox<String> empC   = new JComboBox<>(empNames); empC.setFont(Theme.FONT_BODY);
        JTextField dateF  = Theme.styledField(); dateF.setToolTipText("yyyy-MM-dd");
        JComboBox<String> statusC = Theme.styledCombo("Present", "Absent", "Leave");

        if (existing != null) {
            idF.setText(existing.getAttendanceId()); idF.setEditable(false);
            for (int i = 0; i < empIds.length; i++) if (empIds[i].equals(existing.getEmpId())) { empC.setSelectedIndex(i); break; }
            empC.setEnabled(false);
            if (existing.getDate() != null) dateF.setText(new SimpleDateFormat("yyyy-MM-dd").format(existing.getDate()));
            statusC.setSelectedItem(existing.getStatus());
        } else {
            idF.setText(attDAO.nextId()); idF.setEditable(false);
            dateF.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }

        String[] labels = {"Attendance ID", "Employee", "Date (yyyy-MM-dd)", "Status"};
        JComponent[] fields = {idF, empC, dateF, statusC};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.4; p.add(Theme.label(labels[i]), gc);
            gc.gridx = 1; gc.weightx = 0.6; p.add(fields[i], gc);
        }

        JButton save = Theme.primaryButton(existing == null ? "Mark" : "Update");
        JButton cancel = new JButton("Cancel"); cancel.setFont(Theme.FONT_BODY);
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(cancel);
        btnRow.add(save);
        p.add(btnRow, gc);

        save.addActionListener(e -> {
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd").parse(dateF.getText().trim());
                Attendance a = new Attendance(idF.getText().trim(), d,
                    (String) statusC.getSelectedItem(), empIds[empC.getSelectedIndex()]);
                boolean ok = existing == null ? attDAO.insert(a) : attDAO.update(a);
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, existing == null ? "Attendance marked." : "Attendance updated.");
                    dlg.dispose();
                    notifyDataChanged();
                } else JOptionPane.showMessageDialog(dlg, "Unable to save attendance record.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid date format (yyyy-MM-dd).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(p);
        dlg.setVisible(true);
    }
}
