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
    private Runnable dataChangeListener = () -> {};
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

    public void setDataChangeListener(Runnable listener) {
        dataChangeListener = listener != null ? listener : () -> {};
    }

    private void notifyDataChanged() {
        refresh();
        dataChangeListener.run();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a salary record to edit."); return; }
        String empId = (String) model.getValueAt(row, 1);
        showForm(salDAO.getByEmpId(empId));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a salary record to remove."); return; }
        String id = (String) model.getValueAt(row, 0);
        String empId = (String) model.getValueAt(row, 1);
        int payrollCount = salDAO.countPayrollsForEmployee(empId);
        String message = "Remove salary record " + id + "?";
        if (payrollCount == 1) message += "\nThis employee has 1 payroll record whose total may be affected.";
        else if (payrollCount > 1) message += "\nThis employee has " + payrollCount + " payroll records whose totals may be affected.";
        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (salDAO.delete(id)) { JOptionPane.showMessageDialog(this, "Salary record removed successfully."); notifyDataChanged(); }
            else JOptionPane.showMessageDialog(this, "Unable to remove salary record.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showForm(Salary existing) {
        List<Employee> emps = empDAO.getAll();
        if (emps.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add an employee before assigning salary.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Assign Salary" : "Edit Salary", true);
        dlg.setSize(420, 280);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(8, 4, 8, 4);

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
                if (s.getBasicSalary() < 0 || s.getAllowance() < 0) {
                    JOptionPane.showMessageDialog(dlg, "Basic salary and allowance cannot be negative.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean ok = existing == null ? salDAO.insert(s) : salDAO.update(s);
                if (ok) {
                    String success = existing == null ? "Salary assigned successfully." : "Salary updated successfully.";
                    int payrollCount = salDAO.countPayrollsForEmployee(s.getEmpId());
                    if (payrollCount == 1) success += "\n1 payroll total was updated.";
                    else if (payrollCount > 1) success += "\n" + payrollCount + " payroll totals were updated.";
                    JOptionPane.showMessageDialog(dlg, success);
                    dlg.dispose();
                    notifyDataChanged();
                }
                else JOptionPane.showMessageDialog(dlg, "Unable to save salary record.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Enter valid numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(p); dlg.setVisible(true);
    }
}
