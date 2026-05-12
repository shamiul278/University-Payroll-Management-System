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
    private static final String[] COLS = {"Attendance ID","Employee ID","Employee Name","Date","Status"};

    public AttendancePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(Theme.headerLabel("Attendance Tracking"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.CONTENT_BG);
        JButton addBtn  = Theme.primaryButton("+ Mark Attendance");
        JButton editBtn = Theme.primaryButton("✏ Update");
        JButton delBtn  = Theme.dangerButton("🗑 Delete");
        editBtn.setBackground(new Color(0x744210));
        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(delBtn);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(COLS, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        Theme.styleTable(table);
        // Color status column
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String s = String.valueOf(v);
                if ("Present".equals(s)) { setForeground(new Color(0x276749)); setBackground(new Color(0xC6F6D5)); }
                else if ("Absent".equals(s)) { setForeground(new Color(0x822727)); setBackground(new Color(0xFED7D7)); }
                else { setForeground(new Color(0x744210)); setBackground(new Color(0xFEFCBF)); }
                if (sel) { setBackground(new Color(0xBEE3F8)); setForeground(Theme.TEXT_DARK); }
                setHorizontalAlignment(CENTER); setFont(new Font("Segoe UI", Font.BOLD, 12));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));
        JPanel card = Theme.cardPanel(null);
        card.add(scroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Attendance a : attDAO.getAll()) {
            Employee emp = empDAO.getById(a.getEmpId());
            String name = emp != null ? emp.getName() : "Unknown";
            model.addRow(new Object[]{
                a.getAttendanceId(), a.getEmpId(), name,
                a.getDate() != null ? sdf.format(a.getDate()) : "", a.getStatus()
            });
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a record to update."); return; }
        String id = (String) model.getValueAt(row, 0);
        for (Attendance a : attDAO.getAll()) if (a.getAttendanceId().equals(id)) { showForm(a); return; }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a record to delete."); return; }
        String id = (String) model.getValueAt(row, 0);
        int c = JOptionPane.showConfirmDialog(this, "Delete record " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION && attDAO.delete(id)) { JOptionPane.showMessageDialog(this, "Deleted."); refresh(); }
    }

    private void showForm(Attendance existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Mark Attendance" : "Update Attendance", true);
        dlg.setSize(400, 260);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(8, 4, 8, 4);

        List<Employee> emps = empDAO.getAll();
        String[] empIds = emps.stream().map(Employee::getEmpId).toArray(String[]::new);
        String[] empNames = emps.stream().map(e -> e.getEmpId() + " - " + e.getName()).toArray(String[]::new);

        JTextField idF    = Theme.styledField();
        JComboBox<String> empC = new JComboBox<>(empNames); empC.setFont(Theme.FONT_BODY);
        JTextField dateF  = Theme.styledField(); dateF.setToolTipText("yyyy-MM-dd");
        JComboBox<String> statusC = Theme.styledCombo("Present","Absent","Leave");

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

        String[] labels = {"Attendance ID","Employee","Date (yyyy-MM-dd)","Status"};
        JComponent[] fields = {idF, empC, dateF, statusC};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.4; p.add(Theme.label(labels[i]), gc);
            gc.gridx = 1; gc.weightx = 0.6; p.add(fields[i], gc);
        }

        JButton save = Theme.primaryButton(existing == null ? "Mark" : "Update");
        JButton cancel = new JButton("Cancel"); cancel.setFont(Theme.FONT_BODY);
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btnRow.setBackground(Color.WHITE);
        btnRow.add(cancel); btnRow.add(save);
        p.add(btnRow, gc);

        save.addActionListener(e -> {
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd").parse(dateF.getText().trim());
                Attendance a = new Attendance(idF.getText().trim(), d,
                    (String) statusC.getSelectedItem(), empIds[empC.getSelectedIndex()]);
                boolean ok = existing == null ? attDAO.insert(a) : attDAO.update(a);
                if (ok) { JOptionPane.showMessageDialog(dlg, "Saved."); dlg.dispose(); refresh(); }
                else JOptionPane.showMessageDialog(dlg, "Failed.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid date format (yyyy-MM-dd).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(p); dlg.setVisible(true);
    }
}
