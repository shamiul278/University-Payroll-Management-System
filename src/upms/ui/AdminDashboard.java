package upms.ui;

import upms.dao.*;
import upms.model.User;
import upms.ui.panels.*;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class AdminDashboard extends JFrame {
    private final User currentUser;
    private JPanel contentArea;
    private JButton activeBtn;
    private JLabel breadcrumbCurrent;

    private final EmployeePanel   employeePanel;
    private final DepartmentPanel departmentPanel;
    private final SalaryPanel     salaryPanel;
    private final AttendancePanel attendancePanel;
    private final UserMgmtPanel   userMgmtPanel;
    private final ReportPanel     reportPanel;

    public AdminDashboard(User user) {
        this.currentUser = user;
        employeePanel   = new EmployeePanel();
        departmentPanel = new DepartmentPanel();
        salaryPanel     = new SalaryPanel();
        attendancePanel = new AttendancePanel();
        userMgmtPanel   = new UserMgmtPanel();
        reportPanel     = new ReportPanel();
        wireDataRefresh();

        setTitle("UPMS - Administrator Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);
        Theme.setLookAndFeel();
        initUI();
    }

    private void wireDataRefresh() {
        employeePanel.setDataChangeListener(this::refreshAll);
        departmentPanel.setDataChangeListener(this::refreshAll);
        salaryPanel.setDataChangeListener(this::refreshAll);
        attendancePanel.setDataChangeListener(this::refreshAll);
        userMgmtPanel.setDataChangeListener(this::refreshAll);
    }

    private void refreshAll() {
        employeePanel.refresh();
        departmentPanel.refresh();
        salaryPanel.refresh();
        attendancePanel.refresh();
        userMgmtPanel.refresh();
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

    // ── Top bar ─────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(0, 56));
        bar.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Left: breadcrumb
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

        // Right: user + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Color.WHITE);

        JPanel userInfo = new JPanel(new BorderLayout(0, 1));
        userInfo.setBackground(Color.WHITE);
        JLabel userLbl = new JLabel(currentUser.getUsername());
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLbl.setForeground(Theme.TEXT_DARK);
        JLabel roleLbl = new JLabel("Administrator");
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        roleLbl.setForeground(Theme.TEXT_MUTED);
        userInfo.add(userLbl, BorderLayout.NORTH);
        userInfo.add(roleLbl, BorderLayout.SOUTH);

        JPanel avatar = Theme.avatarCircle(currentUser.getUsername(), Theme.PRIMARY);

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

    // ── Sidebar ──────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));

        // Logo area
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

        JSeparator divider = new JSeparator();
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setForeground(Theme.BORDER);
        sidebar.add(divider);
        sidebar.add(Box.createVerticalStrut(10));

        String[] navItems = {
            "Dashboard", "Employee Management", "Department Setup",
            "Salary Configuration", "Attendance", "User Management", "Reports"
        };

        for (String item : navItems) {
            JButton btn = sidebarBtn(item);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(2));
            btn.addActionListener(e -> handleNav(item, btn));
        }

        sidebar.add(Box.createVerticalGlue());

        // Status at bottom
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
            case "Dashboard":            contentArea.add(buildDashHome(), BorderLayout.CENTER); break;
            case "Employee Management":  contentArea.add(employeePanel, BorderLayout.CENTER); employeePanel.refresh(); break;
            case "Department Setup":     contentArea.add(departmentPanel, BorderLayout.CENTER); departmentPanel.refresh(); break;
            case "Salary Configuration": contentArea.add(salaryPanel, BorderLayout.CENTER); salaryPanel.refresh(); break;
            case "Attendance":           contentArea.add(attendancePanel, BorderLayout.CENTER); attendancePanel.refresh(); break;
            case "User Management":      contentArea.add(userMgmtPanel, BorderLayout.CENTER); userMgmtPanel.refresh(); break;
            case "Reports":              contentArea.add(reportPanel, BorderLayout.CENTER); reportPanel.refresh(); break;
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
        JLabel title = new JLabel("System Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Theme.TEXT_DARK);
        JLabel sub = new JLabel("Institutional performance and payroll metrics at a glance.");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(title, BorderLayout.NORTH);
        titleBlock.add(sub, BorderLayout.CENTER);

        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerBtns.setBackground(Theme.CONTENT_BG);
        headerBtns.add(Theme.primaryButton("Export Audit"));

        headerRow.add(titleBlock, BorderLayout.WEST);
        headerRow.add(headerBtns, BorderLayout.EAST);
        p.add(headerRow, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(Theme.CONTENT_BG);

        // Stat cards
        int empCount = 0, deptCount = 0, payCount = 0;
        double totalBase = 0;
        try {
            empCount  = new EmployeeDAO().getAll().size();
            deptCount = new DepartmentDAO().getAll().size();
            payCount  = new PayrollDAO().getAll().size();
            totalBase = new SalaryDAO().getAll().stream()
                .mapToDouble(upms.model.Salary::getBasicSalary).sum();
        } catch (Exception ignored) {}

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setBackground(Theme.CONTENT_BG);
        cards.add(Theme.statCard("TOTAL EMPLOYEES",   String.valueOf(empCount),            Theme.PRIMARY));
        cards.add(Theme.statCard("DEPARTMENTS",        String.valueOf(deptCount),           Theme.SUCCESS));
        cards.add(Theme.statCard("PAYROLL RECORDS",    String.valueOf(payCount),            Theme.WARNING));
        cards.add(Theme.statCard("TOTAL BASE SALARY",  String.format("%.0f", totalBase),   Theme.DANGER));
        body.add(cards, BorderLayout.NORTH);

        // Priority Actions + Recent Activity
        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setBackground(Theme.CONTENT_BG);
        row2.add(buildPriorityActions());
        row2.add(buildRecentActivity());
        body.add(row2, BorderLayout.CENTER);

        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildPriorityActions() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(18, 18, 18, 18)));

        JLabel title = new JLabel("Priority Actions");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel items = new JPanel(new GridLayout(0, 1, 0, 10));
        items.setBackground(Color.WHITE);

        Object[][] actions = {
            {"Add New Employee",   "Onboard faculty or staff member"},
            {"Configure Salary",   "Update pay scales and structures"},
            {"Department Setup",   "Manage faculty departments"},
            {"Generate Reports",   "Export payroll summaries and audits"}
        };
        for (Object[] a : actions) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(new Color(0xF8FAFC));
            row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)));

            JPanel text = new JPanel(new BorderLayout(0, 2));
            text.setBackground(new Color(0xF8FAFC));
            JLabel nm = new JLabel((String) a[0]);
            nm.setFont(new Font("Segoe UI", Font.BOLD, 13));
            nm.setForeground(Theme.TEXT_DARK);
            JLabel desc = new JLabel((String) a[1]);
            desc.setFont(Theme.FONT_SMALL);
            desc.setForeground(Theme.TEXT_MUTED);
            text.add(nm, BorderLayout.NORTH);
            text.add(desc, BorderLayout.CENTER);

            JLabel arrow = new JLabel("→");
            arrow.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            arrow.setForeground(Theme.PRIMARY);

            row.add(text, BorderLayout.CENTER);
            row.add(arrow, BorderLayout.EAST);
            items.add(row);
        }
        card.add(items, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRecentActivity() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(18, 18, 18, 18)));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(Color.WHITE);
        JLabel title = new JLabel("Recent Stewardship Activity");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        JLabel viewAll = new JLabel("View Full Audit Trail →");
        viewAll.setFont(Theme.FONT_SMALL);
        viewAll.setForeground(Theme.PRIMARY);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(viewAll, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        JPanel list = new JPanel(new GridLayout(0, 1, 0, 8));
        list.setBackground(Color.WHITE);

        String[][] activities = {
            {"Salary structure updated for Department of Engineering",
             "Modified by Admin_01  •  14 minutes ago", "LEVEL_A_UPDATE"},
            {"New faculty onboarding: Prof. Elena Vance",
             "Credentials verified and profile indexed  •  2 hours ago", "ONBOARD"},
            {"Automatic Attendance Sync completed",
             "Cross-referenced 4,021 data points with 100% accuracy  •  5h ago", "SYNC"}
        };

        for (String[] act : activities) {
            JPanel row = new JPanel(new BorderLayout(10, 2));
            row.setBackground(Color.WHITE);
            row.setBorder(new EmptyBorder(6, 0, 6, 0));

            JPanel dot = new JPanel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.PRIMARY);
                    g2.fillOval(0, 5, 10, 10);
                    g2.dispose();
                }
            };
            dot.setOpaque(false);
            dot.setPreferredSize(new Dimension(12, 20));

            JPanel text = new JPanel(new BorderLayout(0, 3));
            text.setBackground(Color.WHITE);
            JLabel nm = new JLabel(act[0]);
            nm.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nm.setForeground(Theme.TEXT_DARK);
            JLabel detail = new JLabel(act[1]);
            detail.setFont(Theme.FONT_SMALL);
            detail.setForeground(Theme.TEXT_MUTED);
            JLabel tag = new JLabel(act[2]);
            tag.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            tag.setForeground(Theme.TEXT_MUTED);
            tag.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(1, 6, 1, 6)));
            text.add(nm, BorderLayout.NORTH);
            text.add(detail, BorderLayout.CENTER);
            text.add(tag, BorderLayout.SOUTH);

            row.add(dot, BorderLayout.WEST);
            row.add(text, BorderLayout.CENTER);
            list.add(row);
        }

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }
}
