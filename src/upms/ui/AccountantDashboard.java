package upms.ui;

import upms.dao.*;
import upms.model.User;
import upms.ui.panels.*;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class AccountantDashboard extends JFrame {
    private final User currentUser;
    private JPanel contentArea;
    private JButton activeBtn;

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

        setTitle("UPMS - Accountant Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);
        Theme.setLookAndFeel();
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Theme.CONTENT_BG);
        root.add(contentArea, BorderLayout.CENTER);

        setContentPane(root);
        showDashboard();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x22543D)); // Accountant: green theme
        bar.setPreferredSize(new Dimension(0, 54));
        bar.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel brand = new JLabel("UPMS  ·  Accountant Dashboard");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brand.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setBackground(new Color(0x22543D));

        JLabel user = new JLabel("👤 " + currentUser.getUsername());
        user.setFont(Theme.FONT_BODY); user.setForeground(new Color(0x9AE6B4));

        JButton logout = new JButton("Logout");
        logout.setFont(Theme.FONT_SMALL); logout.setBackground(Theme.DANGER);
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
            {"🏠", "Overview"},
            {"👤", "Employees"},
            {"📅", "Attendance"},
            {"💵", "Payroll Processing"},
            {"🎁", "Bonus Management"},
            {"📉", "Deductions"},
            {"📊", "Reports & Analytics"}
        };

        for (String[] item : items) {
            JButton btn = sidebarBtn(item[0] + "  " + item[1]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(2));
            btn.addActionListener(e -> handleNav(item[1], btn));
        }
        sidebar.add(Box.createVerticalGlue());
        JLabel ver = new JLabel("  UPMS v1.0");
        ver.setFont(Theme.FONT_SMALL); ver.setForeground(new Color(0x4A5568));
        sidebar.add(ver);
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
        btn.setBackground(new Color(0x2F855A));
        btn.setForeground(Color.WHITE);

        contentArea.removeAll();
        switch (section) {
            case "Overview":           contentArea.add(buildDashHome(), BorderLayout.CENTER); break;
            case "Employees":          contentArea.add(employeePanel, BorderLayout.CENTER); employeePanel.refresh(); break;
            case "Attendance":         contentArea.add(attendancePanel, BorderLayout.CENTER); attendancePanel.refresh(); break;
            case "Payroll Processing": contentArea.add(payrollPanel, BorderLayout.CENTER); payrollPanel.refresh(); break;
            case "Bonus Management":   contentArea.add(bonusPanel, BorderLayout.CENTER); bonusPanel.refresh(); break;
            case "Deductions":         contentArea.add(deductionPanel, BorderLayout.CENTER); deductionPanel.refresh(); break;
            case "Reports & Analytics":contentArea.add(reportPanel, BorderLayout.CENTER); reportPanel.refresh(); break;
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

        JLabel title = Theme.headerLabel("Accountant Overview");
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        p.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setBackground(Theme.CONTENT_BG);

        try {
            PayrollDAO payDao = new PayrollDAO();
            AttendanceDAO attDao = new AttendanceDAO();
            BonusDAO bonDao = new BonusDAO();
            DeductionDAO dedDao = new DeductionDAO();

            long pending = payDao.getAll().stream()
                .filter(pay -> "Pending".equals(pay.getPaymentStatus())).count();
            double totalPay = payDao.getAll().stream().mapToDouble(upms.model.Payroll::getTotalSalary).sum();

            cards.add(Theme.statCard("Total Payrolls",    String.valueOf(payDao.getAll().size()), Theme.ACCENT));
            cards.add(Theme.statCard("Pending Payrolls",  String.valueOf(pending),                Theme.WARNING));
            cards.add(Theme.statCard("Bonuses Recorded",  String.valueOf(bonDao.getAll().size()),  Theme.SUCCESS));
            cards.add(Theme.statCard("Total Disbursed",   String.format("%.0f", totalPay),         Theme.DANGER));
        } catch (Exception ex) {
            cards.add(Theme.statCard("Total Payrolls",  "N/A", Theme.ACCENT));
            cards.add(Theme.statCard("Pending",         "N/A", Theme.WARNING));
            cards.add(Theme.statCard("Bonuses",         "N/A", Theme.SUCCESS));
            cards.add(Theme.statCard("Disbursed",       "N/A", Theme.DANGER));
        }

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(Theme.CONTENT_BG);
        center.add(cards, BorderLayout.NORTH);

        JPanel tips = Theme.cardPanel("Payroll Processing Guide");
        String[] steps = {
            "1. Go to Attendance to record daily staff attendance.",
            "2. Visit Payroll Processing to generate monthly payroll.",
            "3. Add applicable bonuses via Bonus Management.",
            "4. Apply deductions (Tax, Loan, Absence) under Deductions.",
            "5. Update payment status after disbursement.",
            "6. Generate summary reports from Reports & Analytics."
        };
        JPanel tPanel = new JPanel(new GridLayout(steps.length, 1, 0, 6));
        tPanel.setBackground(Color.WHITE);
        for (String s : steps) {
            JLabel l = new JLabel(s);
            l.setFont(Theme.FONT_BODY); l.setForeground(Theme.TEXT_MUTED);
            tPanel.add(l);
        }
        tips.add(tPanel, BorderLayout.CENTER);
        center.add(tips, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }
}
