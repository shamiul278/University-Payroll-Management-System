package upms.ui;

import upms.dao.UserDAO;
import upms.model.User;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField userField;
    private JPasswordField passField;
    private JLabel errorLabel;
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("UPMS - University Payroll Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.SIDEBAR_BG);

        // Left branding panel
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(Theme.PRIMARY);
        left.setPreferredSize(new Dimension(360, 580));

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(Theme.PRIMARY);

        JLabel logo = new JLabel("UPMS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 48));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("ACADEMIC ARCHITECT");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(0xBEE3F8));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel divider = new JPanel();
        divider.setBackground(new Color(0x4A90D9));
        divider.setMaximumSize(new Dimension(60, 2));
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("<html><center>University Payroll<br>Management System</center></html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(new Color(0xBEE3F8));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);

        brand.add(Box.createVerticalStrut(20));
        brand.add(logo);
        brand.add(Box.createVerticalStrut(8));
        brand.add(sub);
        brand.add(Box.createVerticalStrut(16));
        brand.add(divider);
        brand.add(Box.createVerticalStrut(16));
        brand.add(tagline);
        brand.add(Box.createVerticalStrut(30));

        // Feature bullets
        String[] features = {"Automated Payroll Processing", "Role-Based Access Control", "Attendance Tracking", "Comprehensive Reports"};
        for (String f : features) {
            JLabel fl = new JLabel("  ✓  " + f);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            fl.setForeground(new Color(0x90CDF4));
            fl.setAlignmentX(Component.CENTER_ALIGNMENT);
            brand.add(fl);
            brand.add(Box.createVerticalStrut(6));
        }

        left.add(brand);

        // Right login panel
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 50, 20, 50));
        form.setMaximumSize(new Dimension(400, 500));

        JLabel title = new JLabel("Secure Gateway");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Theme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to access UPMS");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(28));

        // Username
        JLabel userLbl = Theme.label("USERNAME");
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        userLbl.setForeground(Theme.TEXT_MUTED);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userLbl);
        form.add(Box.createVerticalStrut(6));

        userField = new JTextField();
        userField.setFont(Theme.FONT_BODY);
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        userField.setPreferredSize(new Dimension(300, 40));
        userField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        userField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userField);
        form.add(Box.createVerticalStrut(16));

        // Password
        JLabel passLbl = Theme.label("PASSWORD");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passLbl.setForeground(Theme.TEXT_MUTED);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passLbl);
        form.add(Box.createVerticalStrut(6));

        passField = new JPasswordField();
        passField.setFont(Theme.FONT_BODY);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passField.setPreferredSize(new Dimension(300, 40));
        passField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passField);
        form.add(Box.createVerticalStrut(6));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(18));

        // Login button
        JButton loginBtn = new JButton("Login to UPMS  →");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBackground(Theme.ACCENT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());
        form.add(loginBtn);

        // Enter key support
        passField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin(); }
        });
        userField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin(); }
        });

        form.add(Box.createVerticalStrut(20));
        JLabel hint = new JLabel("Admin: admin/admin123  |  Accountant: accountant/acc123");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(hint);

        right.add(form);

        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void doLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }
        User user = userDAO.login(username, password);
        if (user == null) {
            errorLabel.setText("Invalid username or password.");
            return;
        }
        dispose();
        if ("Administrator".equals(user.getRole())) {
            new AdminDashboard(user).setVisible(true);
        } else {
            new AccountantDashboard(user).setVisible(true);
        }
    }
}
