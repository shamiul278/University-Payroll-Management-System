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
    private static final String[] COLS = {"Salary ID", "Employee ID", "Name", "Basic Salary", "Allowance", "Gross Pay", "Status"};

    // Form fields (left panel)
    private JTextField idF;
    private JComboBox<String> empC;
    private JTextField basicF;
    private JTextField allowF;
    private JTextField effectiveDateF;
    private String[] empIds;
    private Salary editingRecord = null;

    public SalaryPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        // Page header
        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel title = Theme.headerLabel("Configure Compensation");
        JLabel sub = new JLabel("Define institutional payroll structures by linking unique salary identifiers to academic and administrative personnel.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Two-column layout: form (left) + table (right)
        JPanel twoCol = new JPanel(new GridLayout(1, 2, 16, 0));
        twoCol.setBackground(Theme.CONTENT_BG);

        twoCol.add(buildFormCard());
        twoCol.add(buildTableCard());

        add(twoCol, BorderLayout.CENTER);
        refresh();
    }

    // ── Left: allocation form ──────────────────────────────────────────────────

    private JPanel buildFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(20, 20, 20, 20)));

        JLabel cardTitle = new JLabel("New Allocation");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cardTitle.setForeground(Theme.TEXT_DARK);
        card.add(cardTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.gridwidth = 1;

        // Employee Identity
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1; gc.gridwidth = 2;
        form.add(fieldLabel("EMPLOYEE IDENTITY (EMP_ID)"), gc);

        gc.gridy = 1;
        List<Employee> emps = empDAO.getAll();
        empIds = emps.stream().map(Employee::getEmpId).toArray(String[]::new);
        String[] empNames = emps.stream().map(e -> e.getEmpId() + " - " + e.getName()).toArray(String[]::new);
        empC = new JComboBox<>(empNames);
        empC.setFont(Theme.FONT_BODY);
        empC.setBackground(Color.WHITE);
        form.add(empC, gc);

        // Salary ID (auto)
        gc.gridy = 2;
        form.add(fieldLabel("SALARY ID"), gc);
        gc.gridy = 3;
        idF = Theme.styledField();
        idF.setEditable(false);
        idF.setBackground(new Color(0xF1F5F9));
        form.add(idF, gc);

        // Basic + Allowance side by side
        gc.gridy = 4; gc.gridwidth = 1; gc.weightx = 0.5;
        form.add(fieldLabel("BASIC SALARY"), gc);
        gc.gridx = 1;
        form.add(fieldLabel("ALLOWANCE"), gc);

        gc.gridy = 5; gc.gridx = 0;
        basicF = Theme.styledField();
        basicF.setText("0.00");
        form.add(basicF, gc);
        gc.gridx = 1;
        allowF = Theme.styledField();
        allowF.setText("0.00");
        form.add(allowF, gc);

        // Effective Date
        gc.gridy = 6; gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1;
        form.add(fieldLabel("EFFECTIVE DATE"), gc);
        gc.gridy = 7;
        effectiveDateF = Theme.styledField();
        effectiveDateF.setText("mm/dd/yyyy");
        form.add(effectiveDateF, gc);

        card.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new GridLayout(2, 1, 0, 8));
        btns.setBackground(Color.WHITE);

        JButton finalizeBtn = Theme.primaryButton("Finalize Configuration");
        finalizeBtn.setPreferredSize(new Dimension(0, 42));
        finalizeBtn.addActionListener(e -> saveAllocation());

        JPanel editDelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        editDelRow.setBackground(Color.WHITE);
        JButton editBtn = Theme.primaryButton("Edit Selected");
        editBtn.setBackground(new Color(0x744210));
        JButton delBtn = Theme.dangerButton("Remove Selected");
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        editDelRow.add(editBtn);
        editDelRow.add(delBtn);

        btns.add(finalizeBtn);
        btns.add(editDelRow);
        card.add(btns, BorderLayout.SOUTH);

        // Set initial ID
        idF.setText(salDAO.nextId());
        empC.addActionListener(e -> loadSalaryForSelectedEmployee());
        loadSalaryForSelectedEmployee();
        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    // ── Right: recent config logs ──────────────────────────────────────────────

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(16, 16, 16, 16)));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(Color.WHITE);
        JLabel title = new JLabel("Recent Configuration Logs");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        JLabel viewAll = new JLabel("VIEW FULL ARCHIVE");
        viewAll.setFont(new Font("Segoe UI", Font.BOLD, 10));
        viewAll.setForeground(Theme.PRIMARY);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(viewAll, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        model = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Right-align numeric columns
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 3; i <= 5; i++) table.getColumnModel().getColumn(i).setCellRenderer(rightAlign);

        // Status badge renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel badge = Theme.statusBadge(v != null ? v.toString() : "ACTIVE");
                badge.setHorizontalAlignment(SwingConstants.CENTER);
                return badge;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // Fiscal policy reminder
        JPanel reminder = new JPanel(new BorderLayout());
        reminder.setBackground(new Color(0xEFF6FF));
        reminder.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xBFDBFE), 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        JLabel reminderText = new JLabel("<html><b>Fiscal Policy Reminder</b><br>"
            + "Adjustments exceeding 15% of the previous basic salary require secondary authorization.</html>");
        reminderText.setFont(Theme.FONT_SMALL);
        reminderText.setForeground(new Color(0x1E40AF));
        reminder.add(reminderText);
        card.add(reminder, BorderLayout.SOUTH);

        return card;
    }

    public void refresh() {
        model.setRowCount(0);
        for (Salary s : salDAO.getAll()) {
            Employee emp = empDAO.getById(s.getEmpId());
            String empName = emp != null ? emp.getName() : "Unknown";
            model.addRow(new Object[]{
                s.getSalaryId(), s.getEmpId(), empName,
                String.format("%.2f", s.getBasicSalary()),
                String.format("%.2f", s.getAllowance()),
                String.format("%.2f", s.getGross()),
                "ACTIVE"
            });
        }
        if (idF != null) loadSalaryForSelectedEmployee();
    }

    public void setDataChangeListener(Runnable listener) {
        dataChangeListener = listener != null ? listener : () -> {};
    }

    private void notifyDataChanged() {
        refresh();
        dataChangeListener.run();
    }

    private void saveAllocation() {
        try {
            if (empIds == null || empIds.length == 0) {
                JOptionPane.showMessageDialog(this, "Add an employee before assigning salary.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double basic = Double.parseDouble(basicF.getText().trim());
            double allow = Double.parseDouble(allowF.getText().trim());
            if (basic < 0 || allow < 0) {
                JOptionPane.showMessageDialog(this, "Salary values cannot be negative.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String empId = empIds[empC.getSelectedIndex()];
            Salary current = salDAO.getByEmpId(empId);
            Salary s = new Salary(current != null ? current.getSalaryId() : idF.getText().trim(), basic, allow, empId);
            boolean updating = editingRecord != null || current != null;
            boolean ok = updating ? salDAO.update(s) : salDAO.insert(s);
            if (ok) {
                JOptionPane.showMessageDialog(this, updating ? "Salary updated successfully." : "Salary configured successfully.");
                notifyDataChanged();
            } else JOptionPane.showMessageDialog(this, salDAO.getLastErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numeric values for salary fields.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a salary record to edit."); return; }
        String empId = (String) model.getValueAt(row, 1);
        Salary s = salDAO.getByEmpId(empId);
        if (s == null) return;
        editingRecord = s;
        idF.setText(s.getSalaryId());
        for (int i = 0; i < empIds.length; i++) if (empIds[i].equals(s.getEmpId())) { empC.setSelectedIndex(i); break; }
        empC.setEnabled(false);
        basicF.setText(String.valueOf(s.getBasicSalary()));
        allowF.setText(String.valueOf(s.getAllowance()));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a salary record to remove."); return; }
        String id = (String) model.getValueAt(row, 0);
        String empId = (String) model.getValueAt(row, 1);
        int payrollCount = salDAO.countPayrollsForEmployee(empId);
        String msg = "Remove salary record " + id + "?";
        if (payrollCount > 0) msg += "\n" + payrollCount + " payroll record(s) may be affected.";
        if (JOptionPane.showConfirmDialog(this, msg, "Confirm Remove", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (salDAO.delete(id)) { JOptionPane.showMessageDialog(this, "Salary record removed."); notifyDataChanged(); }
            else JOptionPane.showMessageDialog(this, salDAO.getLastErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSalaryForSelectedEmployee() {
        if (empC == null || idF == null || basicF == null || allowF == null || empIds == null || empIds.length == 0) return;

        Salary existing = salDAO.getByEmpId(empIds[empC.getSelectedIndex()]);
        editingRecord = existing;
        if (existing != null) {
            idF.setText(existing.getSalaryId());
            basicF.setText(String.format("%.2f", existing.getBasicSalary()));
            allowF.setText(String.format("%.2f", existing.getAllowance()));
        } else {
            idF.setText(salDAO.nextId());
            basicF.setText("0.00");
            allowF.setText("0.00");
        }
        empC.setEnabled(true);
    }
}
