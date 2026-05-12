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

public class ReportPanel extends JPanel {
    private final EmployeeDAO empDAO = new EmployeeDAO();
    private final DepartmentDAO deptDAO = new DepartmentDAO();
    private final PayrollDAO payDAO = new PayrollDAO();
    private final SalaryDAO salDAO = new SalaryDAO();
    private final AttendanceDAO attDAO = new AttendanceDAO();
    private final BonusDAO bonDAO = new BonusDAO();
    private final DeductionDAO dedDAO = new DeductionDAO();

    private JTabbedPane tabs;

    public ReportPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(Theme.headerLabel("Reports & Analytics"), BorderLayout.WEST);

        JButton refreshBtn = Theme.primaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btnP.setBackground(Theme.CONTENT_BG);
        btnP.add(refreshBtn); header.add(btnP, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_BODY);
        tabs.setBackground(Color.WHITE);
        add(tabs, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        tabs.removeAll();
        tabs.addTab("Payroll Summary",    buildPayrollSummary());
        tabs.addTab("Employee Report",    buildEmployeeReport());
        tabs.addTab("Department Report",  buildDeptReport());
        tabs.addTab("Attendance Summary", buildAttendanceSummary());
        tabs.addTab("Salary Overview",    buildSalaryOverview());
    }

    private JPanel buildPayrollSummary() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Payroll ID","Employee","Month","Total Salary","Status","Generated"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(m); Theme.styleTable(t);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        double grandTotal = 0;
        for (Payroll pay : payDAO.getAll()) {
            Employee emp = empDAO.getById(pay.getEmpId());
            grandTotal += pay.getTotalSalary();
            m.addRow(new Object[]{
                pay.getPayrollId(), emp != null ? emp.getName() : pay.getEmpId(),
                pay.getMonth(), String.format("%.2f", pay.getTotalSalary()),
                pay.getPaymentStatus(), pay.getGeneratedDate() != null ? sdf.format(pay.getGeneratedDate()) : ""
            });
        }

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(Theme.BORDER, 1));
        p.add(sp, BorderLayout.CENTER);

        JLabel total = new JLabel(String.format("Grand Total Disbursed: %.2f   |   Records: %d", grandTotal, m.getRowCount()));
        total.setFont(new Font("Segoe UI", Font.BOLD, 13));
        total.setForeground(Theme.ACCENT);
        total.setBorder(new EmptyBorder(8, 0, 0, 0));
        p.add(total, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildEmployeeReport() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Emp ID","Name","Designation","Dept","Employment Type","Basic Salary","Allowance","Total Absences"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(m); Theme.styleTable(t);

        for (Employee e : empDAO.getAll()) {
            Salary s = salDAO.getByEmpId(e.getEmpId());
            int absences = attDAO.countByStatus(e.getEmpId(), "Absent");
            Department d = deptDAO.getById(e.getDeptId());
            m.addRow(new Object[]{
                e.getEmpId(), e.getName(), e.getDesignation(),
                d != null ? d.getDeptName() : e.getDeptId(), e.getEmploymentType(),
                s != null ? String.format("%.2f", s.getBasicSalary()) : "N/A",
                s != null ? String.format("%.2f", s.getAllowance())    : "N/A",
                absences
            });
        }

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(Theme.BORDER, 1));
        p.add(sp, BorderLayout.CENTER);

        JLabel count = new JLabel("Total Employees: " + m.getRowCount());
        count.setFont(new Font("Segoe UI", Font.BOLD, 13));
        count.setForeground(Theme.ACCENT);
        count.setBorder(new EmptyBorder(8, 0, 0, 0));
        p.add(count, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildDeptReport() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Dept ID","Department Name","Building","Contact","Employee Count","Total Basic Salary"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(m); Theme.styleTable(t);

        for (Department d : deptDAO.getAll()) {
            List<Employee> emps = empDAO.getByDept(d.getDeptId());
            double totalSal = 0;
            for (Employee e : emps) {
                Salary s = salDAO.getByEmpId(e.getEmpId());
                if (s != null) totalSal += s.getBasicSalary();
            }
            m.addRow(new Object[]{
                d.getDeptId(), d.getDeptName(), d.getBuilding(), d.getContactNo(),
                emps.size(), String.format("%.2f", totalSal)
            });
        }

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(Theme.BORDER, 1));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildAttendanceSummary() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Employee ID","Name","Present Days","Absent Days","Leave Days","Total Records"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(m); Theme.styleTable(t);

        for (Employee e : empDAO.getAll()) {
            int present = attDAO.countByStatus(e.getEmpId(), "Present");
            int absent  = attDAO.countByStatus(e.getEmpId(), "Absent");
            int leave   = attDAO.countByStatus(e.getEmpId(), "Leave");
            if (present + absent + leave > 0)
                m.addRow(new Object[]{e.getEmpId(), e.getName(), present, absent, leave, present+absent+leave});
        }

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(Theme.BORDER, 1));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildSalaryOverview() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Salary ID","Employee","Basic Salary","Allowance","Gross Pay"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(m); Theme.styleTable(t);

        double totalBasic = 0, totalAllow = 0, totalGross = 0;
        for (Salary s : salDAO.getAll()) {
            Employee e = empDAO.getById(s.getEmpId());
            totalBasic += s.getBasicSalary(); totalAllow += s.getAllowance(); totalGross += s.getGross();
            m.addRow(new Object[]{
                s.getSalaryId(), e != null ? e.getName() : s.getEmpId(),
                String.format("%.2f", s.getBasicSalary()),
                String.format("%.2f", s.getAllowance()),
                String.format("%.2f", s.getGross())
            });
        }

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(Theme.BORDER, 1));
        p.add(sp, BorderLayout.CENTER);

        JLabel totals = new JLabel(String.format(
            "Total Basic: %.2f   |   Total Allowance: %.2f   |   Total Gross: %.2f",
            totalBasic, totalAllow, totalGross));
        totals.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totals.setForeground(Theme.ACCENT);
        totals.setBorder(new EmptyBorder(8, 0, 0, 0));
        p.add(totals, BorderLayout.SOUTH);
        return p;
    }
}
