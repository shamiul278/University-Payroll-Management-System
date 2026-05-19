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
    private final EmployeeDAO   empDAO  = new EmployeeDAO();
    private final DepartmentDAO deptDAO = new DepartmentDAO();
    private final PayrollDAO    payDAO  = new PayrollDAO();
    private final SalaryDAO     salDAO  = new SalaryDAO();
    private final AttendanceDAO attDAO  = new AttendanceDAO();

    private JTabbedPane tabs;

    public ReportPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleBlock = new JPanel(new BorderLayout(0, 3));
        titleBlock.setBackground(Theme.CONTENT_BG);
        JLabel title = Theme.headerLabel("Institutional Reports");
        JLabel sub = new JLabel("Generate and analyze academic payroll and budgetary performance.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(title, BorderLayout.NORTH);
        titleBlock.add(sub, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(Theme.CONTENT_BG);
        JButton refreshBtn = Theme.primaryButton("Refresh");
        JButton exportBtn  = Theme.primaryButton("Export Archive");
        exportBtn.setBackground(new Color(0x374151));
        refreshBtn.addActionListener(e -> refresh());
        btns.add(refreshBtn);
        btns.add(exportBtn);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(btns, BorderLayout.EAST);
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
        tabs.addTab("Payroll Trends",     buildPayrollTrendsTab());
    }

    // ── Payroll Summary ────────────────────────────────────────────────────────

    private JPanel buildPayrollSummary() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Summary stat cards
        List<Payroll> payrolls = payDAO.getAll();
        double grandTotal = payrolls.stream().mapToDouble(Payroll::getTotalSalary).sum();
        long disbursed = payrolls.stream().filter(pay -> "Disbursed".equals(pay.getPaymentStatus())).count();
        long pending   = payrolls.stream().filter(pay -> "Pending".equals(pay.getPaymentStatus())).count();

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setBackground(Color.WHITE);
        cards.setBorder(new EmptyBorder(0, 0, 14, 0));
        cards.add(Theme.statCard("TOTAL DISBURSED", String.format("%.2f", grandTotal), Theme.PRIMARY));
        cards.add(Theme.statCard("DISBURSED RECORDS", String.valueOf(disbursed), Theme.SUCCESS));
        cards.add(Theme.statCard("PENDING RECORDS",   String.valueOf(pending),   Theme.WARNING));
        p.add(cards, BorderLayout.NORTH);

        String[] cols = {"Payroll ID", "Employee", "Month", "Total Salary", "Status", "Generated"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        Theme.styleTable(t);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Payroll pay : payrolls) {
            Employee emp = empDAO.getById(pay.getEmpId());
            m.addRow(new Object[]{
                pay.getPayrollId(), emp != null ? emp.getName() : pay.getEmpId(),
                pay.getMonth(), String.format("%.2f", pay.getTotalSalary()),
                pay.getPaymentStatus(),
                pay.getGeneratedDate() != null ? sdf.format(pay.getGeneratedDate()) : ""
            });
        }

        // Status badge renderer
        t.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel badge = Theme.statusBadge(v != null ? v.toString() : "");
                badge.setHorizontalAlignment(SwingConstants.CENTER);
                return badge;
            }
        });

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(Theme.BORDER, 1));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ── Employee Report ────────────────────────────────────────────────────────

    private JPanel buildEmployeeReport() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Emp ID", "Name", "Designation", "Dept", "Type", "Basic Salary", "Allowance", "Absences"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        Theme.styleTable(t);
        t.setRowHeight(42);
        t.getColumnModel().getColumn(1).setCellRenderer(new AvatarCellRenderer());

        for (Employee e : empDAO.getAll()) {
            Salary s    = salDAO.getByEmpId(e.getEmpId());
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
        count.setForeground(Theme.PRIMARY);
        count.setBorder(new EmptyBorder(8, 0, 0, 0));
        p.add(count, BorderLayout.SOUTH);
        return p;
    }

    // ── Department Report ──────────────────────────────────────────────────────

    private JPanel buildDeptReport() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Dept ID", "Department Name", "Building", "Contact", "Employees", "Total Basic Salary"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        Theme.styleTable(t);

        for (Department d : deptDAO.getAll()) {
            List<Employee> emps = empDAO.getByDept(d.getDeptId());
            double totalSal = emps.stream()
                .mapToDouble(e -> { Salary s = salDAO.getByEmpId(e.getEmpId()); return s != null ? s.getBasicSalary() : 0; })
                .sum();
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

    // ── Attendance Summary ─────────────────────────────────────────────────────

    private JPanel buildAttendanceSummary() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Employee ID", "Name", "Present Days", "Absent Days", "Leave Days", "Total Records"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        Theme.styleTable(t);

        for (Employee e : empDAO.getAll()) {
            int present = attDAO.countByStatus(e.getEmpId(), "Present");
            int absent  = attDAO.countByStatus(e.getEmpId(), "Absent");
            int leave   = attDAO.countByStatus(e.getEmpId(), "Leave");
            if (present + absent + leave > 0)
                m.addRow(new Object[]{e.getEmpId(), e.getName(), present, absent, leave, present + absent + leave});
        }

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(Theme.BORDER, 1));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ── Salary Overview ────────────────────────────────────────────────────────

    private JPanel buildSalaryOverview() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Salary ID", "Employee", "Basic Salary", "Allowance", "Gross Pay"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        Theme.styleTable(t);

        double totalBasic = 0, totalAllow = 0, totalGross = 0;
        for (Salary s : salDAO.getAll()) {
            Employee e = empDAO.getById(s.getEmpId());
            totalBasic += s.getBasicSalary();
            totalAllow += s.getAllowance();
            totalGross += s.getGross();
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
        totals.setForeground(Theme.PRIMARY);
        totals.setBorder(new EmptyBorder(8, 0, 0, 0));
        p.add(totals, BorderLayout.SOUTH);
        return p;
    }

    // ── Payroll Trends tab with bar chart ─────────────────────────────────────

    private JPanel buildPayrollTrendsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Summary cards row
        List<Payroll> all = payDAO.getAll();
        double totalDisbursed = all.stream().filter(pay -> "Disbursed".equals(pay.getPaymentStatus()))
            .mapToDouble(Payroll::getTotalSalary).sum();
        double upcomingLiabilities = all.stream().filter(pay -> "Pending".equals(pay.getPaymentStatus()))
            .mapToDouble(Payroll::getTotalSalary).sum();

        JPanel summaryCards = new JPanel(new GridLayout(1, 2, 14, 0));
        summaryCards.setBackground(Color.WHITE);
        summaryCards.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel disbCard = new JPanel(new BorderLayout(0, 4));
        disbCard.setBackground(Theme.PRIMARY);
        disbCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel disbLabel = new JLabel("TOTAL DISBURSEMENTS");
        disbLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        disbLabel.setForeground(new Color(0xBFDBFE));
        JLabel disbValue = new JLabel(String.format("$%.1fK", totalDisbursed / 1000));
        disbValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        disbValue.setForeground(Color.WHITE);
        disbCard.add(disbLabel, BorderLayout.NORTH);
        disbCard.add(disbValue, BorderLayout.CENTER);

        JPanel liabCard = new JPanel(new BorderLayout(0, 4));
        liabCard.setBackground(Color.WHITE);
        liabCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(16, 18, 16, 18)));
        JLabel liabLabel = new JLabel("UPCOMING LIABILITIES");
        liabLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        liabLabel.setForeground(Theme.TEXT_MUTED);
        JLabel liabValue = new JLabel(String.format("$%.1fK", upcomingLiabilities / 1000));
        liabValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        liabValue.setForeground(Theme.TEXT_DARK);
        JLabel liabSub = new JLabel("Expected disbursement by June 30th");
        liabSub.setFont(Theme.FONT_SMALL);
        liabSub.setForeground(Theme.TEXT_MUTED);
        liabCard.add(liabLabel, BorderLayout.NORTH);
        liabCard.add(liabValue, BorderLayout.CENTER);
        liabCard.add(liabSub, BorderLayout.SOUTH);

        summaryCards.add(disbCard);
        summaryCards.add(liabCard);
        p.add(summaryCards, BorderLayout.NORTH);

        // Bar chart card
        JPanel chartCard = new JPanel(new BorderLayout(0, 10));
        chartCard.setBackground(Color.WHITE);
        chartCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)));

        JLabel chartTitle = new JLabel("Annual Payroll Trends");
        chartTitle.setFont(Theme.FONT_HEADER);
        chartTitle.setForeground(Theme.TEXT_DARK);
        JLabel chartSub = new JLabel("Comparative spending against allocated budget cap");
        chartSub.setFont(Theme.FONT_SMALL);
        chartSub.setForeground(Theme.TEXT_MUTED);

        JPanel chartHeader = new JPanel(new BorderLayout(0, 3));
        chartHeader.setBackground(Color.WHITE);
        chartHeader.add(chartTitle, BorderLayout.NORTH);
        chartHeader.add(chartSub, BorderLayout.CENTER);
        chartCard.add(chartHeader, BorderLayout.NORTH);

        // Build monthly data
        Map<String, Double> monthData = buildMonthlyData(all);
        String[] labels = monthData.keySet().toArray(new String[0]);
        double[] values = monthData.values().stream().mapToDouble(Double::doubleValue).toArray();
        chartCard.add(new BarChartPanel(labels, values), BorderLayout.CENTER);

        p.add(chartCard, BorderLayout.CENTER);
        return p;
    }

    private Map<String, Double> buildMonthlyData(List<Payroll> payrolls) {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Map<String, Double> result = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.MONTH, -i);
            result.put(monthNames[c.get(Calendar.MONTH)], 0.0);
        }
        for (Payroll pay : payrolls) {
            if (pay.getMonth() == null) continue;
            for (String key : result.keySet()) {
                if (pay.getMonth().startsWith(key)) {
                    result.put(key, result.get(key) + pay.getTotalSalary());
                    break;
                }
            }
        }
        return result;
    }

    // ── Bar chart component ───────────────────────────────────────────────────

    private static class BarChartPanel extends JPanel {
        private final String[] labels;
        private final double[] values;

        BarChartPanel(String[] labels, double[] values) {
            this.labels = labels;
            this.values = values;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(0, 220));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values == null || values.length == 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padL = 14, padR = 14, padT = 16, padB = 28;
            int w = getWidth() - padL - padR;
            int h = getHeight() - padT - padB;
            double max = Arrays.stream(values).max().orElse(1);
            if (max == 0) max = 1;

            int n = values.length;
            int groupW = w / n;
            int barW = Math.max(10, groupW - 16);

            for (int i = 0; i < n; i++) {
                int barH = (int) (values[i] / max * h);
                int x = padL + i * groupW + (groupW - barW) / 2;
                int y = padT + h - barH;

                // Bar fill with gradient
                GradientPaint gp = new GradientPaint(x, y, Theme.PRIMARY, x, padT + h, new Color(0x3B5BDB));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                // Value label on top
                if (values[i] > 0) {
                    g2.setColor(Theme.TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    String valStr = values[i] >= 1000
                        ? String.format("%.0fK", values[i] / 1000)
                        : String.format("%.0f", values[i]);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(valStr, x + (barW - fm.stringWidth(valStr)) / 2, y - 3);
                }

                // Month label
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                FontMetrics fm = g2.getFontMetrics();
                String lbl = labels[i];
                g2.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, getHeight() - 6);
            }
            g2.dispose();
        }
    }

    // ── Avatar renderer for Name column ──────────────────────────────────────

    private static class AvatarCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean sel, boolean foc, int row, int col) {
            String name = value != null ? value.toString() : "";
            JPanel cell = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            cell.setBackground(sel ? new Color(0xEEF2FF) : Color.WHITE);
            JPanel avatar = Theme.avatarCircle(name, Theme.avatarColor(name));
            JLabel lbl = new JLabel(name);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(sel ? Theme.PRIMARY : Theme.TEXT_DARK);
            cell.add(avatar);
            cell.add(lbl);
            return cell;
        }
    }
}
