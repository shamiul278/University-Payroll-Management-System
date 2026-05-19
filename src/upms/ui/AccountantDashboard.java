package upms.ui;

import upms.dao.*;
import upms.model.*;
import upms.model.User;
import upms.ui.panels.*;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class AccountantDashboard extends JFrame {
    private final User currentUser;
    private JPanel contentArea;
    private JButton activeBtn;
    private JLabel breadcrumbCurrent;

    private final AttendancePanel attendancePanel;
    private final PayrollPanel    payrollPanel;
    private final BonusPanel      bonusPanel;
    private final DeductionPanel  deductionPanel;
    private final ReportPanel     reportPanel;
    private final EmployeePanel   employeePanel;

    public AccountantDashboard(User user) {
        this.currentUser = user;
        attendancePanel = new AttendancePanel();
        payrollPanel    = new PayrollPanel();
        bonusPanel      = new BonusPanel();
        deductionPanel  = new DeductionPanel();
        reportPanel     = new ReportPanel();
        employeePanel   = new EmployeePanel();
        wireDataRefresh();

        setTitle("UPMS - Accountant Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);
        Theme.setLookAndFeel();
        initUI();
    }

    private void wireDataRefresh() {
        attendancePanel.setDataChangeListener(this::refreshAll);
        payrollPanel.setDataChangeListener(this::refreshAll);
        bonusPanel.setDataChangeListener(this::refreshAll);
        deductionPanel.setDataChangeListener(this::refreshAll);
        employeePanel.setDataChangeListener(this::refreshAll);
    }

    private void refreshAll() {
        attendancePanel.refresh();
        payrollPanel.refresh();
        bonusPanel.refresh();
        deductionPanel.refresh();
        employeePanel.refresh();
        reportPanel.refresh();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.CONTENT_BG);

        root.add(buildTopBar(), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout());
        main.add(buildSidebar(), BorderLayout.WEST);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Theme.CONTENT_BG);
        main.add(contentArea, BorderLayout.CENTER);

        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        showDashboard();
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(0, 56));
        bar.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setBackground(Color.WHITE);

        JPanel logoBox = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("UPMS", (getWidth() - fm.stringWidth("UPMS")) / 2, getHeight() / 2 + 4);
                g2.dispose();
            }
        };
        logoBox.setOpaque(false);
        logoBox.setPreferredSize(new Dimension(40, 30));

        JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        breadcrumb.setBackground(Color.WHITE);
        JLabel bRoot = new JLabel("UPMS");
        bRoot.setFont(Theme.FONT_BODY);
        bRoot.setForeground(Theme.TEXT_MUTED);
        JLabel slash = new JLabel("/");
        slash.setFont(Theme.FONT_BODY);
        slash.setForeground(Theme.TEXT_MUTED);
        breadcrumbCurrent = new JLabel("Dashboard");
        breadcrumbCurrent.setFont(new Font("Segoe UI", Font.BOLD, 13));
        breadcrumbCurrent.setForeground(Theme.TEXT_DARK);
        breadcrumb.add(bRoot);
        breadcrumb.add(slash);
        breadcrumb.add(breadcrumbCurrent);

        left.add(logoBox);
        left.add(breadcrumb);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Color.WHITE);

        JPanel userInfo = new JPanel(new BorderLayout(0, 1));
        userInfo.setBackground(Color.WHITE);
        JLabel userLbl = new JLabel(currentUser.getUsername());
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLbl.setForeground(Theme.TEXT_DARK);
        JLabel roleLbl = new JLabel("Senior Accountant");
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        roleLbl.setForeground(Theme.TEXT_MUTED);
        userInfo.add(userLbl, BorderLayout.NORTH);
        userInfo.add(roleLbl, BorderLayout.SOUTH);

        JPanel avatar = Theme.avatarCircle(currentUser.getUsername(), new Color(0x059669));

        JButton logout = new JButton("Logout");
        logout.setFont(Theme.FONT_SMALL);
        logout.setBackground(Theme.DANGER);
        logout.setForeground(Color.WHITE);
        logout.setBorderPainted(false);
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        right.add(userInfo);
        right.add(avatar);
        right.add(logout);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));

        JPanel logoArea = new JPanel();
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
        logoArea.setBackground(Color.WHITE);
        logoArea.setBorder(new EmptyBorder(20, 14, 16, 14));
        logoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        JLabel logoName = new JLabel("UPMS");
        logoName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoName.setForeground(Theme.PRIMARY);
        logoName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoSub = new JLabel("ACADEMIC ARCHITECT");
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        logoSub.setForeground(Theme.TEXT_MUTED);
        logoSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoArea.add(logoName);
        logoArea.add(Box.createVerticalStrut(3));
        logoArea.add(logoSub);
        sidebar.add(logoArea);

        JSeparator div = new JSeparator();
        div.setAlignmentX(Component.LEFT_ALIGNMENT);
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        div.setForeground(Theme.BORDER);
        sidebar.add(div);
        sidebar.add(Box.createVerticalStrut(10));

        String[] navItems = {
            "Overview", "Employee Management", "Attendance",
            "Payroll Processing", "Bonus Management", "Deductions", "Reports & Analytics"
        };

        for (String item : navItems) {
            JButton btn = sidebarBtn(item);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(2));
            btn.addActionListener(e -> handleNav(item, btn));
        }

        sidebar.add(Box.createVerticalGlue());

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusRow.setBackground(Color.WHITE);
        statusRow.setBorder(new EmptyBorder(10, 12, 16, 12));
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel dot = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SUCCESS);
                g2.fillOval(0, 2, 9, 9);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(10, 14));

        JLabel statusLbl = new JLabel("System Status: Operational");
        statusLbl.setFont(Theme.FONT_SMALL);
        statusLbl.setForeground(Theme.TEXT_MUTED);
        statusRow.add(dot);
        statusRow.add(statusLbl);
        sidebar.add(statusRow);

        return sidebar;
    }

    private JButton sidebarBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Theme.TEXT_MUTED);
        btn.setBackground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(new EmptyBorder(11, 12, 11, 12));
        btn.setPreferredSize(new Dimension(220, 42));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeBtn) { btn.setBackground(Theme.SIDEBAR_HOVER); btn.setForeground(Theme.PRIMARY); }
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeBtn) { btn.setBackground(Color.WHITE); btn.setForeground(Theme.TEXT_MUTED); }
            }
        });
        return btn;
    }

    private void handleNav(String section, JButton btn) {
        if (activeBtn != null) {
            activeBtn.setBackground(Color.WHITE);
            activeBtn.setForeground(Theme.TEXT_MUTED);
        }
        activeBtn = btn;
        btn.setBackground(Theme.PRIMARY);
        btn.setForeground(Color.WHITE);
        breadcrumbCurrent.setText(section);

        contentArea.removeAll();
        switch (section) {
            case "Overview":              contentArea.add(buildDashHome(), BorderLayout.CENTER); break;
            case "Employee Management":   contentArea.add(employeePanel, BorderLayout.CENTER); employeePanel.refresh(); break;
            case "Attendance":            contentArea.add(attendancePanel, BorderLayout.CENTER); attendancePanel.refresh(); break;
            case "Payroll Processing":    contentArea.add(payrollPanel, BorderLayout.CENTER); payrollPanel.refresh(); break;
            case "Bonus Management":      contentArea.add(bonusPanel, BorderLayout.CENTER); bonusPanel.refresh(); break;
            case "Deductions":            contentArea.add(deductionPanel, BorderLayout.CENTER); deductionPanel.refresh(); break;
            case "Reports & Analytics":   contentArea.add(reportPanel, BorderLayout.CENTER); reportPanel.refresh(); break;
        }
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void showDashboard() {
        contentArea.add(buildDashHome(), BorderLayout.CENTER);
    }

    // ── Dashboard home ────────────────────────────────────────────────────────

    private JPanel buildDashHome() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(Theme.CONTENT_BG);
        p.setBorder(new EmptyBorder(26, 28, 24, 28));

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Theme.CONTENT_BG);

        JPanel titleBlock = new JPanel(new BorderLayout(0, 4));
        titleBlock.setBackground(Theme.CONTENT_BG);
        JLabel title = new JLabel("Accountant Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Theme.TEXT_DARK);
        String month = new SimpleDateFormat("MMMM yyyy").format(new Date());
        JLabel sub = new JLabel("Reviewing payroll status for " + month + " cycles.");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(title, BorderLayout.NORTH);
        titleBlock.add(sub, BorderLayout.CENTER);

        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerBtns.setBackground(Theme.CONTENT_BG);
        headerBtns.add(Theme.primaryButton("Process New Payroll"));

        headerRow.add(titleBlock, BorderLayout.WEST);
        headerRow.add(headerBtns, BorderLayout.EAST);
        p.add(headerRow, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(Theme.CONTENT_BG);

        // Stat cards
        int totalPayrolls = 0;
        long pendingCount = 0;
        int empCount = 0;
        double totalPay = 0;
        List<Payroll> allPayrolls = new ArrayList<>();
        try {
            PayrollDAO payDao = new PayrollDAO();
            allPayrolls = payDao.getAll();
            totalPayrolls = allPayrolls.size();
            pendingCount = allPayrolls.stream().filter(pay -> "Pending".equals(pay.getPaymentStatus())).count();
            totalPay = allPayrolls.stream().mapToDouble(Payroll::getTotalSalary).sum();
            empCount = new EmployeeDAO().getAll().size();
        } catch (Exception ignored) {}

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setBackground(Theme.CONTENT_BG);
        cards.add(Theme.statCard("TOTAL MONTHLY PAYOUT",   String.format("%.0f", totalPay),   Theme.PRIMARY));
        cards.add(Theme.statCard("PENDING PAYROLLS",        String.valueOf(pendingCount),       Theme.WARNING));
        cards.add(Theme.statCard("TOTAL EMPLOYEES",         String.valueOf(empCount),           Theme.SUCCESS));
        cards.add(Theme.statCard("PAYROLL RECORDS",         String.valueOf(totalPayrolls),      new Color(0x7C3AED)));
        body.add(cards, BorderLayout.NORTH);

        // Payroll Trends + Pending Tasks
        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setBackground(Theme.CONTENT_BG);
        row2.add(buildPayrollTrends(allPayrolls));
        row2.add(buildPendingTasksAndActivity(allPayrolls));
        body.add(row2, BorderLayout.CENTER);

        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildPayrollTrends(List<Payroll> payrolls) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(18, 18, 18, 18)));

        JLabel title = new JLabel("Payroll Trends");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        JLabel sub = new JLabel("Comparison of gross payouts across 6 months");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);

        JPanel titleBlock = new JPanel(new BorderLayout(0, 3));
        titleBlock.setBackground(Color.WHITE);
        titleBlock.add(title, BorderLayout.NORTH);
        titleBlock.add(sub, BorderLayout.CENTER);
        card.add(titleBlock, BorderLayout.NORTH);

        // Build monthly totals from payroll data
        Map<String, Double> monthlyTotals = new LinkedHashMap<>();
        String[] lastMonths = getLastSixMonths();
        for (String m : lastMonths) monthlyTotals.put(m, 0.0);
        for (Payroll pay : payrolls) {
            String m = pay.getMonth();
            if (m != null) {
                for (String key : monthlyTotals.keySet()) {
                    if (m.contains(key.split(" ")[0])) {
                        monthlyTotals.put(key, monthlyTotals.get(key) + pay.getTotalSalary());
                        break;
                    }
                }
            }
        }

        String[] labels = monthlyTotals.keySet().toArray(new String[0]);
        double[] values = monthlyTotals.values().stream().mapToDouble(Double::doubleValue).toArray();
        card.add(new BarChartPanel(labels, values), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPendingTasksAndActivity(List<Payroll> payrolls) {
        JPanel outer = new JPanel(new BorderLayout(0, 14));
        outer.setBackground(Theme.CONTENT_BG);

        // Pending tasks card
        JPanel tasksCard = new JPanel(new BorderLayout(0, 10));
        tasksCard.setBackground(Theme.PRIMARY);
        tasksCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)));

        JLabel taskTitle = new JLabel("! Pending Tasks");
        taskTitle.setFont(Theme.FONT_HEADER);
        taskTitle.setForeground(Color.WHITE);
        tasksCard.add(taskTitle, BorderLayout.NORTH);

        JPanel taskList = new JPanel(new GridLayout(0, 1, 0, 8));
        taskList.setBackground(Theme.PRIMARY);

        long pending = payrolls.stream().filter(p -> "Pending".equals(p.getPaymentStatus())).count();
        String[][] tasks = {
            {"Process Current Payroll Cycle", pending > 0 ? "URGENT" : "DONE"},
            {"Review Pending Deductions", "PENDING"},
            {"Audit Q2 Disbursement", "DONE"},
            {"Tax Report Filing", "PENDING"}
        };
        for (String[] task : tasks) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(Theme.PRIMARY);
            JLabel name = new JLabel("✓ " + task[0]);
            name.setFont(Theme.FONT_BODY);
            name.setForeground(Color.WHITE);
            JLabel badge = Theme.statusBadge(task[1]);
            row.add(name, BorderLayout.CENTER);
            row.add(badge, BorderLayout.EAST);
            taskList.add(row);
        }
        tasksCard.add(taskList, BorderLayout.CENTER);

        JButton viewAll = new JButton("View All Tasks");
        viewAll.setFont(Theme.FONT_BODY);
        viewAll.setBackground(Color.WHITE);
        viewAll.setForeground(Theme.PRIMARY);
        viewAll.setFocusPainted(false);
        viewAll.setBorderPainted(false);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tasksCard.add(viewAll, BorderLayout.SOUTH);

        outer.add(tasksCard, BorderLayout.CENTER);
        return outer;
    }

    private String[] getLastSixMonths() {
        String[] names = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        Calendar cal = Calendar.getInstance();
        String[] result = new String[6];
        for (int i = 5; i >= 0; i--) {
            result[5 - i] = names[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR);
            cal.add(Calendar.MONTH, -1);
        }
        return result;
    }

    // ── Simple bar chart ──────────────────────────────────────────────────────

    private static class BarChartPanel extends JPanel {
        private final String[] labels;
        private final double[] values;

        BarChartPanel(String[] labels, double[] values) {
            this.labels = labels;
            this.values = values;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(0, 200));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values.length == 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padL = 10, padR = 10, padT = 16, padB = 30;
            int chartW = getWidth() - padL - padR;
            int chartH = getHeight() - padT - padB;
            double max = 1;
            for (double v : values) if (v > max) max = v;

            int n = values.length;
            int groupW = chartW / n;
            int barW = Math.max(8, groupW - 14);

            for (int i = 0; i < n; i++) {
                int barH = (int) (values[i] / max * chartH);
                int x = padL + i * groupW + (groupW - barW) / 2;
                int y = padT + chartH - barH;

                g2.setColor(Theme.PRIMARY);
                g2.fillRoundRect(x, y, barW, barH, 4, 4);

                // Label
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                String lbl = labels[i].split(" ")[0];
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, getHeight() - 8);
            }
            g2.dispose();
        }
    }
}
