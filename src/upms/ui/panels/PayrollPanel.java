package upms.ui.panels;

import upms.dao.*;
import upms.model.*;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class PayrollPanel extends JPanel {
    private final PayrollDAO payDAO = new PayrollDAO();
    private final EmployeeDAO empDAO = new EmployeeDAO();
    private final SalaryDAO salDAO = new SalaryDAO();
    private final BonusDAO bonDAO = new BonusDAO();
    private final DeductionDAO dedDAO = new DeductionDAO();
    private JTable table;
    private DefaultTableModel model;
    private Runnable dataChangeListener = () -> {};
    private static final String[] COLS = {"Payroll ID","Employee ID","Name","Month","Generated Date","Total Salary","Status"};

    public PayrollPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(Theme.headerLabel("Payroll Processing"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.CONTENT_BG);
        JButton genBtn    = Theme.successButton("⚡ Generate Payroll");
        JButton statusBtn = Theme.primaryButton("🔄 Update Status");
        JButton viewBtn   = Theme.primaryButton("👁 View Details");
        JButton delBtn    = Theme.dangerButton("🗑 Delete");

        genBtn.addActionListener(e -> generatePayroll());
        statusBtn.addActionListener(e -> updateStatus());
        viewBtn.addActionListener(e -> viewDetails());
        delBtn.addActionListener(e -> deleteSelected());

        btnPanel.add(genBtn); btnPanel.add(statusBtn); btnPanel.add(viewBtn); btnPanel.add(delBtn);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(COLS, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        Theme.styleTable(table);

        // Status column color
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String s = String.valueOf(v);
                if ("Disbursed".equals(s))  { setForeground(new Color(0x276749)); setBackground(new Color(0xC6F6D5)); }
                else if ("Processed".equals(s)) { setForeground(new Color(0x2B6CB0)); setBackground(new Color(0xBEE3F8)); }
                else { setForeground(new Color(0x744210)); setBackground(new Color(0xFEFCBF)); }
                if (sel) { setBackground(new Color(0xBEE3F8)); setForeground(Theme.TEXT_DARK); }
                setHorizontalAlignment(CENTER); setFont(new Font("Segoe UI", Font.BOLD, 12));
                return this;
            }
        });

        // Right-align salary column
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(5).setCellRenderer(right);

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
        for (Payroll p : payDAO.getAll()) {
            Employee emp = empDAO.getById(p.getEmpId());
            model.addRow(new Object[]{
                p.getPayrollId(), p.getEmpId(), emp != null ? emp.getName() : "Unknown",
                p.getMonth(), p.getGeneratedDate() != null ? sdf.format(p.getGeneratedDate()) : "",
                String.format("%.2f", p.getTotalSalary()), p.getPaymentStatus()
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

    private void generatePayroll() {
        List<Employee> emps = empDAO.getAll();
        if (emps.isEmpty()) { JOptionPane.showMessageDialog(this, "No employees are available for payroll generation."); return; }

        String[] empItems = emps.stream().map(e -> e.getEmpId() + " - " + e.getName()).toArray(String[]::new);
        JComboBox<String> empC = new JComboBox<>(empItems);
        empC.setFont(Theme.FONT_BODY);
        JTextField monthF = Theme.styledField(); monthF.setText(new SimpleDateFormat("MMMM yyyy").format(new Date()));
        JComboBox<String> bonusC = new JComboBox<>(); bonusC.addItem("None");
        for (Bonus b : bonDAO.getAll()) bonusC.addItem(b.getBonusId() + " - " + b.getType() + " (" + b.getAmount() + ")");
        JComboBox<String> dedC = new JComboBox<>(); dedC.addItem("None");
        for (Deduction d : dedDAO.getAll()) dedC.addItem(d.getDeductionId() + " - " + d.getReason() + " (" + d.getAmount() + ")");

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        form.add(Theme.label("Employee:")); form.add(empC);
        form.add(Theme.label("Month:")); form.add(monthF);
        form.add(Theme.label("Bonus:")); form.add(bonusC);
        form.add(Theme.label("Deduction:")); form.add(dedC);

        int res = JOptionPane.showConfirmDialog(this, form, "Generate Payroll", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        String month = monthF.getText().trim();
        if (month.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Payroll month is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String empId = emps.get(empC.getSelectedIndex()).getEmpId();
        Salary sal = salDAO.getByEmpId(empId);
        if (sal == null) { JOptionPane.showMessageDialog(this, "Cannot generate payroll because this employee has no salary record.", "Error", JOptionPane.ERROR_MESSAGE); return; }

        String selectedBonus = (String) bonusC.getSelectedItem();
        String bonusId = null;
        if (selectedBonus != null && !selectedBonus.equals("None")) {
            bonusId = selectedBonus.split(" - ")[0];
        }

        String selectedDed = (String) dedC.getSelectedItem();
        String dedId = null;
        if (selectedDed != null && !selectedDed.equals("None")) {
            dedId = selectedDed.split(" - ")[0];
        }

        String payrollId = payDAO.nextId();
        PayrollDAO.GenerateResult generated = payDAO.generatePayroll(payrollId, month, empId, bonusId, dedId);
        if (generated == null) {
            JOptionPane.showMessageDialog(this, payDAO.getLastErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, String.format("Payroll %s generated!\nEmployee: %s\nMonth: %s\nTotal: %.2f",
            generated.getPayrollId(), empId, monthF.getText(), generated.getTotalSalary()));
        notifyDataChanged();
    }

    private void updateStatus() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a payroll record to update."); return; }
        String id = (String) model.getValueAt(row, 0);
        String[] statuses = {"Pending","Processed","Disbursed"};
        String chosen = (String) JOptionPane.showInputDialog(this, "Select new status:", "Update Status",
            JOptionPane.PLAIN_MESSAGE, null, statuses, model.getValueAt(row, 6));
        if (chosen != null) {
            if (payDAO.updateStatus(id, chosen)) { JOptionPane.showMessageDialog(this, "Payroll status updated successfully."); notifyDataChanged(); }
            else JOptionPane.showMessageDialog(this, payDAO.getLastErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewDetails() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a payroll to view."); return; }
        String payrollId = (String) model.getValueAt(row, 0);
        List<Bonus> bonuses = bonDAO.getByPayroll(payrollId);
        List<Deduction> deds = dedDAO.getByPayroll(payrollId);

        StringBuilder sb = new StringBuilder();
        sb.append("Payroll ID: ").append(payrollId).append("\n");
        sb.append("Employee:   ").append(model.getValueAt(row, 1)).append(" - ").append(model.getValueAt(row, 2)).append("\n");
        sb.append("Month:      ").append(model.getValueAt(row, 3)).append("\n");
        sb.append("Total:      ").append(model.getValueAt(row, 5)).append("\n");
        sb.append("Status:     ").append(model.getValueAt(row, 6)).append("\n\n");

        sb.append("BONUSES:\n");
        if (bonuses.isEmpty()) sb.append("  None\n");
        else for (Bonus b : bonuses) sb.append("  ").append(b.getType()).append(": +").append(b.getAmount()).append("\n");

        sb.append("\nDEDUCTIONS:\n");
        if (deds.isEmpty()) sb.append("  None\n");
        else for (Deduction d : deds) sb.append("  ").append(d.getReason()).append(": -").append(d.getAmount()).append("\n");

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setEditable(false); ta.setBackground(new Color(0xF8FAFC));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(380, 280));
        JOptionPane.showMessageDialog(this, sp, "Payroll Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a payroll to delete."); return; }
        String id = (String) model.getValueAt(row, 0);
        int c = JOptionPane.showConfirmDialog(this, "Delete payroll " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            if (payDAO.delete(id)) { JOptionPane.showMessageDialog(this, "Payroll deleted successfully."); notifyDataChanged(); }
            else JOptionPane.showMessageDialog(this, payDAO.getLastErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
