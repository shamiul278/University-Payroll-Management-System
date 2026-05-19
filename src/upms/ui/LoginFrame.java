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
        setSize(820, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        Theme.setLookAndFeel();
        initUI();
    }

    private void initUI() {
        // Light gray page background
        JPanel root = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0xEEF2F7));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // Logo section above card
        container.add(buildLogoSection());
        container.add(Box.createVerticalStrut(22));

        // White login card
        container.add(buildCard());
        container.add(Box.createVerticalStrut(28));

        // Security badges
        container.add(buildSecurityBadges());
        container.add(Box.createVerticalStrut(16));

        // Footer
        container.add(buildFooter());

        root.add(container);
        setContentPane(root);
    }

    private JPanel buildLogoSection() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // Square navy icon
        JPanel icon = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY);
                g2.fillRoundRect(0, 0, 64, 64, 14, 14);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("UP", (64 - fm.stringWidth("UP")) / 2, 28);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                fm = g2.getFontMetrics();
                g2.drawString("MS", (64 - fm.stringWidth("MS")) / 2, 50);
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(64, 64));
        icon.setMaximumSize(new Dimension(64, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel("UPMS");
        name.setFont(new Font("Segoe UI", Font.BOLD, 26));
        name.setForeground(Theme.PRIMARY);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("ACADEMIC ARCHITECT");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(icon);
        p.add(Box.createVerticalStrut(10));
        p.add(name);
        p.add(Box.createVerticalStrut(3));
        p.add(sub);
        return p;
    }

    private JPanel buildCard() {
        // Outer panel provides the drop shadow
        JPanel shadow = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(6, 6, getWidth() - 6, getHeight() - 6, 16, 16);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 16, 16);
                g2.dispose();
            }
        };
        shadow.setOpaque(false);
        shadow.setPreferredSize(new Dimension(430, 360));
        shadow.setMaximumSize(new Dimension(430, 400));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(30, 38, 30, 38));

        // Title + role badges
        JLabel title = new JLabel("Secure Gateway");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel roleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        roleRow.setOpaque(false);
        roleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleRow.add(roleBadge("Admin"));
        JLabel amp = new JLabel("&");
        amp.setFont(Theme.FONT_SMALL);
        amp.setForeground(Theme.TEXT_MUTED);
        roleRow.add(amp);
        roleRow.add(roleBadge("Accountant"));

        form.add(title);
        form.add(Box.createVerticalStrut(8));
        form.add(roleRow);
        form.add(Box.createVerticalStrut(22));

        // Username
        form.add(fieldLabel("USERNAME"));
        form.add(Box.createVerticalStrut(5));
        userField = loginField("Enter your Institutional ID");
        form.add(userField);
        form.add(Box.createVerticalStrut(14));

        // Password
        form.add(fieldLabel("PASSWORD"));
        form.add(Box.createVerticalStrut(5));
        passField = new JPasswordField();
        passField.setFont(Theme.FONT_BODY);
        passField.setBackground(new Color(0xF8FAFC));
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(9, 12, 9, 12)));
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(passField);
        form.add(Box.createVerticalStrut(4));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(16));

        // Login button
        JButton loginBtn = new JButton("Login to UPMS  →");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBackground(Theme.PRIMARY);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());
        loginBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loginBtn.setBackground(Theme.ACCENT_HOVER); }
            public void mouseExited(MouseEvent e)  { loginBtn.setBackground(Theme.PRIMARY); }
        });
        form.add(loginBtn);

        passField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin(); }
        });
        userField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin(); }
        });

        shadow.add(form, BorderLayout.CENTER);
        return shadow;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(Theme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField loginField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setFont(Theme.FONT_BODY);
        tf.setForeground(Theme.TEXT_MUTED);
        tf.setBackground(new Color(0xF8FAFC));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(9, 12, 9, 12)));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (placeholder.equals(tf.getText())) { tf.setText(""); tf.setForeground(Theme.TEXT_DARK); }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(Theme.TEXT_MUTED); }
            }
        });
        return tf;
    }

    private JPanel roleBadge(String role) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xEEF2FF));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        JLabel lbl = new JLabel(role);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(Theme.PRIMARY);
        badge.add(lbl);
        return badge;
    }

    private JPanel buildSecurityBadges() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 32, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (String badge : new String[]{"ENCRYPTION", "ISO 27001", "COMPLIANCE"}) {
            JLabel lbl = new JLabel(badge);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lbl.setForeground(new Color(0xA0AEC0));
            p.add(lbl);
        }
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setOpaque(false);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lbl = new JLabel("UNIVERSITY PAYROLL MANAGEMENT SYSTEM");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(new Color(0xA0AEC0));
        p.add(lbl);
        return p;
    }

    private String getActualText(JTextField tf, String placeholder) {
        String t = tf.getText().trim();
        return placeholder.equals(t) ? "" : t;
    }

    private void doLogin() {
        String username = getActualText(userField, "Enter your Institutional ID");
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
