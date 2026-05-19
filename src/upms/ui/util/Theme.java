package upms.ui.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class Theme {
    // Core palette matching Figma design
    public static final Color PRIMARY        = new Color(0x1B2B6B);
    public static final Color PRIMARY_LIGHT  = new Color(0x2D3F8A);
    public static final Color ACCENT         = new Color(0x1B2B6B);
    public static final Color ACCENT_HOVER   = new Color(0x2D3F8A);
    public static final Color SIDEBAR_BG     = Color.WHITE;
    public static final Color SIDEBAR_HOVER  = new Color(0xEEF2FF);
    public static final Color CONTENT_BG     = new Color(0xF0F2F8);
    public static final Color WHITE          = Color.WHITE;
    public static final Color TEXT_DARK      = new Color(0x1A202C);
    public static final Color TEXT_MUTED     = new Color(0x64748B);
    public static final Color SUCCESS        = new Color(0x16A34A);
    public static final Color WARNING        = new Color(0xD97706);
    public static final Color DANGER         = new Color(0xDC2626);
    public static final Color BORDER         = new Color(0xE2E8F0);
    public static final Color TABLE_HEADER   = new Color(0xF8FAFC);
    public static final Color TABLE_ALT      = new Color(0xFAFCFF);

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SIDEBAR = new Font("Segoe UI", Font.PLAIN, 13);

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(WHITE);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(PRIMARY); }
        });
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER);
        btn.setForeground(WHITE);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SUCCESS);
        btn.setForeground(WHITE);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    public static JTextField styledField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBackground(new Color(0xF8FAFC));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(7, 12, 7, 12)));
        return tf;
    }

    public static JComboBox<String> styledCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(WHITE);
        return cb;
    }

    public static JPanel cardPanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(16, 18, 16, 18)));
        if (title != null && !title.isEmpty()) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(FONT_HEADER);
            lbl.setForeground(TEXT_DARK);
            lbl.setBorder(new EmptyBorder(0, 0, 12, 0));
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(38);
        table.setGridColor(new Color(0xF1F5F9));
        table.setSelectionBackground(new Color(0xEEF2FF));
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
    }

    public static JLabel headerLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    public static JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    public static JPanel statCard(String title, String value, Color accentColor) {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(WHITE);
        p.setBorder(new LineBorder(BORDER, 1, true));

        JPanel topAccent = new JPanel();
        topAccent.setBackground(accentColor);
        topAccent.setPreferredSize(new Dimension(0, 4));

        JPanel inner = new JPanel(new BorderLayout(0, 6));
        inner.setBackground(WHITE);
        inner.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(TEXT_DARK);

        inner.add(titleLbl, BorderLayout.NORTH);
        inner.add(valLbl, BorderLayout.CENTER);

        p.add(topAccent, BorderLayout.NORTH);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    public static void setLookAndFeel() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        UIManager.put("Table.alternateRowColor", TABLE_ALT);
    }

    /** Circular avatar panel with initials. */
    public static JPanel avatarCircle(String name, Color bg) {
        String initials = getInitials(name);
        JPanel panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initials, x, y);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(34, 34));
        panel.setMinimumSize(new Dimension(34, 34));
        panel.setMaximumSize(new Dimension(34, 34));
        return panel;
    }

    public static Color avatarColor(String name) {
        Color[] colors = {
            new Color(0x4F46E5), new Color(0x0891B2), new Color(0x059669),
            new Color(0xD97706), new Color(0xDC2626), new Color(0x7C3AED),
            new Color(0xDB2777), new Color(0x2563EB)
        };
        if (name == null || name.isEmpty()) return colors[0];
        return colors[Math.abs(name.hashCode()) % colors.length];
    }

    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    /** Colored pill label for status values. */
    public static JLabel statusBadge(String status) {
        Color bg, fg;
        switch (status) {
            case "Present": case "Disbursed": case "ACTIVE": case "COMPLETED":
                bg = new Color(0xDCFCE7); fg = new Color(0x166534); break;
            case "Absent": case "URGENT":
                bg = new Color(0xFEE2E2); fg = new Color(0x991B1B); break;
            case "Leave": case "Pending": case "PENDING":
                bg = new Color(0xFEF9C3); fg = new Color(0x854D0E); break;
            case "Processed":
                bg = new Color(0xDBEAFE); fg = new Color(0x1E40AF); break;
            default:
                bg = new Color(0xF1F5F9); fg = TEXT_MUTED; break;
        }
        final Color finalBg = bg;
        JLabel lbl = new JLabel(status) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(finalBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(fg);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setBorder(new EmptyBorder(3, 10, 3, 10));
        lbl.setOpaque(false);
        return lbl;
    }
}
