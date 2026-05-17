package upms.ui;

import upms.dao.*;
import upms.model.User;
import upms.ui.panels.*;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class AdminDashboard extends JFrame {
    private final User currentUser;
    private JPanel contentArea;
    private JButton activeBtn;

    private final EmployeePanel employeePanel;
    private final DepartmentPanel departmentPanel;
    private final SalaryPanel salaryPanel;
    private final AttendancePanel attendancePanel;
    private final UserMgmtPanel userMgmtPanel;
    private final ReportPanel reportPanel;

    public AdminDashboard(User user) {
        this.currentUser = user;
        employeePanel  = new EmployeePanel();
        departmentPanel = new DepartmentPanel();
        salaryPanel    = new SalaryPanel();
        attendancePanel = new AttendancePanel();
        userMgmtPanel  = new UserMgmtPanel();
        reportPanel    = new ReportPanel();
        wireDataRefresh();

        setTitle("UPMS - Administrator Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);
        Theme.setLookAndFeel();
        initUI();
    }

    private void wireDataRefresh() {
        employeePanel.setDataChangeListener(this::refreshLoadedTables);
        departmentPanel.setDataChangeListener(this::refreshLoadedTables);
        salaryPanel.setDataChangeListener(this::refreshLoadedTables);
        attendancePanel.setDataChangeListener(this::refreshLoadedTables);
        userMgmtPanel.setDataChangeListener(this::refreshLoadedTables);
    }

    private void refreshLoadedTables() {
        employeePanel.refresh();
        departmentPanel.refresh();
        salaryPanel.refresh();
        attendancePanel.refresh();
        userMgmtPanel.refresh();
        reportPanel.refresh();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());

        // Top bar
        JPanel topBar = buildTopBar();
        root.add(topBar, BorderLayout.NORTH);

        // Sidebar
        JPanel sidebar = buildSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // Content area
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Theme.CONTENT_BG);
        root.add(contentArea, BorderLayout.CENTER);

        setContentPane(root);
        showDashboard();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.PRIMARY);
        bar.setPreferredSize(new Dimension(0, 54));
        bar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel brand = new JLabel("UPMS  ·  Administrator Dashboard");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brand.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setBackground(Theme.PRIMARY);

        JLabel user = new JLabel("👤 " + currentUser.getUsername());
        user.setFont(Theme.FONT_BODY); user.setForeground(new Color(0xBEE3F8));

        JButton logout = new JButton("Logout");
        logout.setFont(Theme.FONT_SMALL); logout.setBackground(new Color(0xE53E3E));
        logout.setForeground(Color.WHITE); logout.setBorderPainted(false);
        logout.setFocusPainted(false); logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        right.add(user); right.add(logout);
        bar.add(brand, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(16, 0, 16, 0));

        String[][] items = {
            {"🏠", "Dashboard"},
            {"👤", "Employee Mgmt"},
            {"🏢", "Department Setup"},
            {"💰", "Salary Config"},
            {"📅", "Attendance"},
            {"🔑", "User Management"},
            {"📊", "Reports"}
        };

        for (String[] item : items) {
            JButton btn = sidebarBtn(item[0] + "  " + item[1]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(2));
            btn.addActionListener(e -> handleNav(item[1], btn));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel version = new JLabel("  UPMS v1.0");
        version.setFont(Theme.FONT_SMALL); version.setForeground(new Color(0x4A5568));
        sidebar.add(version);
        return sidebar;
    }

    private JButton sidebarBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_SIDEBAR);
        btn.setForeground(new Color(0xA0AEC0));
        btn.setBackground(Theme.SIDEBAR_BG);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeBtn) { btn.setBackground(Theme.SIDEBAR_HOVER); btn.setForeground(Color.WHITE); }
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeBtn) { btn.setBackground(Theme.SIDEBAR_BG); btn.setForeground(new Color(0xA0AEC0)); }
            }
        });
        return btn;
    }

    private void handleNav(String section, JButton btn) {
        if (activeBtn != null) {
            activeBtn.setBackground(Theme.SIDEBAR_BG);
            activeBtn.setForeground(new Color(0xA0AEC0));
        }
        activeBtn = btn;
        btn.setBackground(Theme.ACCENT);
        btn.setForeground(Color.WHITE);

        contentArea.removeAll();
        switch (section) {
            case "Dashboard":      contentArea.add(buildDashHome(), BorderLayout.CENTER); break;
            case "Employee Mgmt":  contentArea.add(employeePanel, BorderLayout.CENTER); employeePanel.refresh(); break;
            case "Department Setup": contentArea.add(departmentPanel, BorderLayout.CENTER); departmentPanel.refresh(); break;
            case "Salary Config":  contentArea.add(salaryPanel, BorderLayout.CENTER); salaryPanel.refresh(); break;
            case "Attendance":     contentArea.add(attendancePanel, BorderLayout.CENTER); attendancePanel.refresh(); break;
            case "User Management": contentArea.add(userMgmtPanel, BorderLayout.CENTER); userMgmtPanel.refresh(); break;
            case "Reports":        contentArea.add(reportPanel, BorderLayout.CENTER); reportPanel.refresh(); break;
        }
        contentArea.revalidate(); contentArea.repaint();
    }

    private void showDashboard() {
        contentArea.add(buildDashHome(), BorderLayout.CENTER);
    }

    private JPanel buildDashHome() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.CONTENT_BG);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = Theme.headerLabel("System Overview");
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        p.add(title, BorderLayout.NORTH);

        // Stat cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setBackground(Theme.CONTENT_BG);

        try {
            EmployeeDAO empDao = new EmployeeDAO();
            DepartmentDAO deptDao = new DepartmentDAO();
            PayrollDAO payDao = new PayrollDAO();
            SalaryDAO salDao = new SalaryDAO();

            int empCount  = empDao.getAll().size();
            int deptCount = deptDao.getAll().size();
            int payCount  = payDao.getAll().size();
            List<upms.model.Salary> sals = salDao.getAll();
            double totalBase = sals.stream().mapToDouble(upms.model.Salary::getBasicSalary).sum();

            cards.add(Theme.statCard("Total Employees",   String.valueOf(empCount),   Theme.ACCENT));
            cards.add(Theme.statCard("Departments",       String.valueOf(deptCount),  Theme.SUCCESS));
            cards.add(Theme.statCard("Payroll Records",   String.valueOf(payCount),   Theme.WARNING));
            cards.add(Theme.statCard("Total Base Salary", String.format("%.0f", totalBase), Theme.DANGER));
        } catch (Exception ex) {
            cards.add(Theme.statCard("Employees",  "N/A", Theme.ACCENT));
            cards.add(Theme.statCard("Departments","N/A", Theme.SUCCESS));
            cards.add(Theme.statCard("Payrolls",   "N/A", Theme.WARNING));
            cards.add(Theme.statCard("Base Salary","N/A", Theme.DANGER));
        }

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(Theme.CONTENT_BG);
        center.add(cards, BorderLayout.NORTH);

        JPanel info = Theme.cardPanel("Quick Actions");
        info.setBackground(Color.WHITE);
        String[] tips = {
            "→  Go to Employee Mgmt to add or update staff records.",
            "→  Use Department Setup to manage faculty departments.",
            "→  Configure salary structures under Salary Config.",
            "→  Track daily attendance in the Attendance module.",
            "→  Generate reports for payroll summaries and audits."
        };
        JPanel tipPanel = new JPanel(new GridLayout(tips.length, 1, 0, 6));
        tipPanel.setBackground(Color.WHITE);
        for (String t : tips) {
            JLabel l = new JLabel(t);
            l.setFont(Theme.FONT_BODY);
            l.setForeground(Theme.TEXT_MUTED);
            tipPanel.add(l);
        }
        info.add(tipPanel, BorderLayout.CENTER);
        center.add(info, BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        return p;
    }
}
