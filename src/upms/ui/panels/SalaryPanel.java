package upms.ui.panels;

import upms.dao.EmployeeDAO;
import upms.dao.SalaryDAO;
import upms.model.Employee;
import upms.model.Salary;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class SalaryPanel extends JPanel {
    private final SalaryDAO salDAO = new SalaryDAO();
    private final EmployeeDAO empDAO = new EmployeeDAO();
    private JTable table;
    private DefaultTableModel model;
    private static final String[] COLS = {"Salary ID","Employee ID","Employee Name","Basic Salary","Allowance","Gross Pay"};

    public SalaryPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(Theme.headerLabel("Salary Configuration"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.CONTENT_BG);
        JButton addBtn  = Theme.primaryButton("+ Assign Salary");
        JButton editBtn = Theme.primaryButton("✏ Edit");
        JButton delBtn  = Theme.dangerButton("🗑 Remove");
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
        // Right-align numeric columns
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 3; i <= 5; i++) table.getColumnModel().getColumn(i).setCellRenderer(rightAlign);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));
        JPanel card = Theme.cardPanel(null);
        card.add(scroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        List<Salary> sals = salDAO.getAll();
        for (Salary s : sals) {
            Employee emp = empDAO.getById(s.getEmpId());
            String empName = emp != null ? emp.getName() : "Unknown";
            model.addRow(new Object[]{
                s.getSalaryId(), s.getEmpId(), empName,
                String.format("%.2f", s.getBasicSalary()),
                String.format("%.2f", s.getAllowance()),
                String.format("%.2f", s.getGross())
            });
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a record to edit."); return; }
        String empId = (String) model.getValueAt(row, 1);
        showForm(salDAO.getByEmpId(empId));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a record to remove."); return; }
        String id = (String) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Remove salary record " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (salDAO.delete(id)) { JOptionPane.showMessageDialog(this, "Removed."); refresh(); }
            else JOptionPane.showMessageDialog(this, "Failed to remove.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showForm(Salary existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Assign Salary" : "Edit Salary", true);
        dlg.setSize(420, 280);
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
        JTextField basicF = Theme.styledField();
        JTextField allowF = Theme.styledField();

        if (existing != null) {
            idF.setText(existing.getSalaryId()); idF.setEditable(false);
            for (int i = 0; i < empIds.length; i++) if (empIds[i].equals(existing.getEmpId())) { empC.setSelectedIndex(i); break; }
            empC.setEnabled(false);
            basicF.setText(String.valueOf(existing.getBasicSalary()));
            allowF.setText(String.valueOf(existing.getAllowance()));
        } else {
            idF.setText(salDAO.nextId()); idF.setEditable(false);
        }

        String[] labels = {"Salary ID", "Employee", "Basic Salary", "Allowance"};
        JComponent[] fields = {idF, empC, basicF, allowF};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.35; p.add(Theme.label(labels[i]), gc);
            gc.gridx = 1; gc.weightx = 0.65; p.add(fields[i], gc);
        }

        JButton save = Theme.primaryButton(existing == null ? "Assign" : "Save");
        JButton cancel = new JButton("Cancel"); cancel.setFont(Theme.FONT_BODY);
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btnRow.setBackground(Color.WHITE);
        btnRow.add(cancel); btnRow.add(save);
        p.add(btnRow, gc);

        save.addActionListener(e -> {
            try {
                Salary s = new Salary(idF.getText().trim(),
                    Double.parseDouble(basicF.getText().trim()),
                    Double.parseDouble(allowF.getText().trim()),
                    empIds[empC.getSelectedIndex()]);
                boolean ok = existing == null ? salDAO.insert(s) : salDAO.update(s);
                if (ok) { JOptionPane.showMessageDialog(dlg, "Saved."); dlg.dispose(); refresh(); }
                else JOptionPane.showMessageDialog(dlg, "Failed.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Enter valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(p); dlg.setVisible(true);
    }
}
