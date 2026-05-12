package upms.ui.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class Theme {
    public static final Color PRIMARY       = new Color(0x1E3A5F);
    public static final Color PRIMARY_LIGHT = new Color(0x2C5282);
    public static final Color ACCENT        = new Color(0x3182CE);
    public static final Color ACCENT_HOVER  = new Color(0x2B6CB0);
    public static final Color SIDEBAR_BG    = new Color(0x1A2744);
    public static final Color SIDEBAR_HOVER = new Color(0x2D3F6B);
    public static final Color CONTENT_BG    = new Color(0xF7FAFC);
    public static final Color WHITE         = Color.WHITE;
    public static final Color TEXT_DARK     = new Color(0x1A202C);
    public static final Color TEXT_MUTED    = new Color(0x718096);
    public static final Color SUCCESS       = new Color(0x48BB78);
    public static final Color WARNING       = new Color(0xED8936);
    public static final Color DANGER        = new Color(0xE53E3E);
    public static final Color BORDER        = new Color(0xE2E8F0);
    public static final Color TABLE_HEADER  = new Color(0xEBF4FF);
    public static final Color TABLE_ALT     = new Color(0xF8FAFC);

    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SIDEBAR = new Font("Segoe UI", Font.PLAIN, 13);

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT); btn.setForeground(WHITE);
        btn.setFont(FONT_BODY); btn.setFocusPainted(false);
        btn.setBorderPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(ACCENT); }
        });
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER); btn.setForeground(WHITE);
        btn.setFont(FONT_BODY); btn.setFocusPainted(false);
        btn.setBorderPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SUCCESS); btn.setForeground(WHITE);
        btn.setFont(FONT_BODY); btn.setFocusPainted(false);
        btn.setBorderPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    public static JTextField styledField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    public static JComboBox<String> styledCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY); cb.setBackground(WHITE);
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
            lbl.setFont(FONT_HEADER); lbl.setForeground(TEXT_DARK);
            lbl.setBorder(new EmptyBorder(0, 0, 12, 0));
            p.add(lbl, BorderLayout.NORTH);
        }
        return p;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(0xBEE3F8));
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 2, 0, ACCENT));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
    }

    public static JLabel headerLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE); lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    public static JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY); lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    public static JPanel statCard(String title, String value, Color color) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(18, 20, 18, 20)));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setBackground(WHITE);
        JPanel dot = new JPanel();
        dot.setBackground(color);
        dot.setPreferredSize(new Dimension(6, 6));

        JLabel titleLbl = new JLabel("  " + title);
        titleLbl.setFont(FONT_SMALL); titleLbl.setForeground(TEXT_MUTED);
        top.add(dot); top.add(titleLbl);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLbl.setForeground(color);

        p.add(top, BorderLayout.NORTH);
        p.add(valLbl, BorderLayout.CENTER);
        return p;
    }

    public static void setLookAndFeel() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("Table.alternateRowColor", TABLE_ALT);
    }
}
