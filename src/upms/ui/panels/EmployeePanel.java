package upms.ui.panels;

import upms.dao.DepartmentDAO;
import upms.dao.EmployeeDAO;
import upms.model.Department;
import upms.model.Employee;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class EmployeePanel extends JPanel {
    private final EmployeeDAO empDAO = new EmployeeDAO();
    private final DepartmentDAO deptDAO = new DepartmentDAO();
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private Runnable dataChangeListener = () -> {};
    private static final String[] COLS = {"Emp ID", "Name", "Designation", "Email", "Phone", "Join Date", "Type", "Dept"};
    private static final int[] COL_WIDTHS = {80, 220, 160, 230, 130, 110, 120, 90};

    public EmployeePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        // Header row
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 6, 0));

        JPanel titleBlock = new JPanel(new BorderLayout(0, 3));
        titleBlock.setBackground(Theme.CONTENT_BG);
        JLabel title = Theme.headerLabel("Employee Directory");
        JLabel sub = new JLabel("Manage and curate institutional academic staff. Search, filter, and modify profiles.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(title, BorderLayout.NORTH);
        titleBlock.add(sub, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.CONTENT_BG);
        JButton addBtn  = Theme.primaryButton("+ Add Employee");
        JButton editBtn = Theme.primaryButton("Edit");
        JButton delBtn  = Theme.dangerButton("Delete");
        editBtn.setBackground(new Color(0x744210));
        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Card: search + table
        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(14, 16, 14, 16)));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchPanel.setBackground(Color.WHITE);
        JTextField search = Theme.styledField();
        search.setPreferredSize(new Dimension(280, 34));
        JButton searchBtn = Theme.primaryButton("Search");
        searchBtn.addActionListener(e -> filterTable(search.getText().trim()));
        search.addActionListener(e -> filterTable(search.getText().trim()));
        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(Theme.FONT_BODY);
        clearBtn.addActionListener(e -> { search.setText(""); filterTable(""); });
        searchPanel.add(search);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);
        tableCard.add(searchPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(42);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        configureTableColumns();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
        refresh();
    }

    /** Single-instance renderer — draws avatar + name directly, allocates nothing per paint. */
    private static class AvatarNameRenderer extends JPanel implements TableCellRenderer {
        private String name = "";
        private boolean selected;
        private Color avatarBg = Color.GRAY;
        private static final Font AVATAR_FONT = new Font("Segoe UI", Font.BOLD, 11);
        private static final Font NAME_FONT   = new Font("Segoe UI", Font.BOLD, 12);

        AvatarNameRenderer() { setOpaque(true); }

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean sel, boolean foc, int row, int col) {
            name      = value != null ? value.toString() : "";
            selected  = sel;
            avatarBg  = Theme.avatarColor(name);
            setBackground(sel ? new Color(0xEEF2FF) : Color.WHITE);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int circleSize = 30, x = 8, y = (getHeight() - circleSize) / 2;
            g2.setColor(avatarBg);
            g2.fillOval(x, y, circleSize, circleSize);

            String initials = initials(name);
            g2.setColor(Color.WHITE);
            g2.setFont(AVATAR_FONT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initials,
                x + (circleSize - fm.stringWidth(initials)) / 2,
                y + (circleSize + fm.getAscent() - fm.getDescent()) / 2);

            g2.setFont(NAME_FONT);
            g2.setColor(selected ? Theme.PRIMARY : Theme.TEXT_DARK);
            fm = g2.getFontMetrics();
            g2.drawString(name, x + circleSize + 8,
                (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }

        private static String initials(String n) {
            if (n == null || n.isBlank()) return "?";
            String[] p = n.trim().split("\\s+");
            return (p.length == 1
                ? p[0].substring(0, Math.min(2, p[0].length()))
                : "" + p[0].charAt(0) + p[p.length - 1].charAt(0)).toUpperCase();
        }

        @Override public Dimension getPreferredSize() { return new Dimension(200, 42); }
    }

    public void refresh() {
        new SwingWorker<List<Employee>, Void>() {
            @Override protected List<Employee> doInBackground() { return empDAO.getAll(); }
            @Override protected void done() {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    List<Employee> employees = get();
                    Object[][] rows = new Object[employees.size()][COLS.length];
                    for (int i = 0; i < employees.size(); i++) {
                        Employee e = employees.get(i);
                        rows[i] = new Object[]{
                            e.getEmpId(), e.getName(), e.getDesignation(), e.getEmail(),
                            e.getPhone(), e.getJoinDate() != null ? sdf.format(e.getJoinDate()) : "",
                            e.getEmploymentType(), e.getDeptId()
                        };
                    }
                    model.setDataVector(rows, COLS);
                    configureTableColumns();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private void configureTableColumns() {
        if (table.getColumnCount() != COL_WIDTHS.length) return;
        table.getColumnModel().getColumn(1).setCellRenderer(new AvatarNameRenderer());
        for (int col = 0; col < COL_WIDTHS.length; col++) {
            TableColumn column = table.getColumnModel().getColumn(col);
            column.setPreferredWidth(COL_WIDTHS[col]);
        }
    }

    public void setDataChangeListener(Runnable listener) {
        dataChangeListener = listener != null ? listener : () -> {};
    }

    private void notifyDataChanged() {
        refresh();
        dataChangeListener.run();
    }

    private void filterTable(String query) {
        String q = query.toLowerCase();
        if (q.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    Object value = entry.getValue(i);
                    if (value != null && value.toString().toLowerCase().contains(q)) return true;
                }
                return false;
            }
        });
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an employee to edit.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int modelRow = table.convertRowIndexToModel(row);
        showForm(empDAO.getById((String) model.getValueAt(modelRow, 0)));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an employee to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) model.getValueAt(modelRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete employee " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (empDAO.delete(id)) { JOptionPane.showMessageDialog(this, "Employee deleted successfully."); notifyDataChanged(); }
            else JOptionPane.showMessageDialog(this, "Cannot delete this employee because related records exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showForm(Employee existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "New Employee Entry" : "Edit Employee", true);
        dlg.setMinimumSize(new Dimension(580, 640));

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.PRIMARY, 3, true),
            new EmptyBorder(30, 36, 30, 36)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);

        // Dialog header
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel dlgTitle = new JLabel(existing == null ? "New Employee Entry" : "Edit Employee");
        dlgTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dlgTitle.setForeground(Theme.TEXT_DARK);
        p.add(dlgTitle, gc);

        gc.gridy = 1;
        JLabel dlgSub = new JLabel("Populate institutional record fields.");
        dlgSub.setFont(Theme.FONT_SMALL);
        dlgSub.setForeground(Theme.TEXT_MUTED);
        p.add(dlgSub, gc);
        gc.gridwidth = 1;

        List<Department> depts = deptDAO.getAll();
        String[] deptIds = new String[depts.size() + 1];
        String[] deptNames = new String[depts.size() + 1];
        deptIds[0] = null;
        deptNames[0] = "Unassigned";
        for (int i = 0; i < depts.size(); i++) {
            deptIds[i + 1] = depts.get(i).getDeptId();
            deptNames[i + 1] = depts.get(i).getDeptId() + " - " + depts.get(i).getDeptName();
        }

        JTextField idF    = Theme.styledField();
        JTextField nameF  = Theme.styledField();
        JTextField designF = Theme.styledField();
        JTextField emailF = Theme.styledField();
        JTextField phoneF = Theme.styledField();
        JTextField dateF  = Theme.styledField(); dateF.setToolTipText("yyyy-MM-dd");
        JComboBox<String> typeC = Theme.styledCombo("Full-Time", "Part-Time", "Contractual");
        JComboBox<String> deptC = new JComboBox<>(deptNames); deptC.setFont(Theme.FONT_BODY);

        if (existing != null) {
            idF.setText(existing.getEmpId()); idF.setEditable(false);
            nameF.setText(existing.getName()); designF.setText(existing.getDesignation());
            emailF.setText(existing.getEmail()); phoneF.setText(existing.getPhone());
            if (existing.getJoinDate() != null) dateF.setText(new SimpleDateFormat("yyyy-MM-dd").format(existing.getJoinDate()));
            typeC.setSelectedItem(existing.getEmploymentType());
            for (int i = 1; i < deptIds.length; i++) if (deptIds[i].equals(existing.getDeptId())) { deptC.setSelectedIndex(i); break; }
        } else {
            idF.setText(empDAO.nextId()); idF.setEditable(false);
        }

        String[] labels = {"EMPLOYEE ID", "FULL NAME", "DESIGNATION", "INSTITUTIONAL EMAIL", "PHONE NUMBER", "JOIN DATE", "EMPLOYMENT TYPE", "DEPARTMENT"};
        JComponent[] fields = {idF, nameF, designF, emailF, phoneF, dateF, typeC, deptC};

        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i + 2; gc.weightx = 0.3;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(Theme.TEXT_MUTED);
            p.add(lbl, gc);
            gc.gridx = 1; gc.weightx = 0.7;
            p.add(fields[i], gc);
        }

        // Data compliance note
        gc.gridx = 0; gc.gridy = labels.length + 2; gc.gridwidth = 2;
        JPanel notePanel = new JPanel(new BorderLayout());
        notePanel.setBackground(new Color(0xEFF6FF));
        notePanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xBFDBFE), 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        JLabel note = new JLabel("<html><body style='width: 430px'><b>Data Compliance</b><br>"
            + "Profile will be subject to automatic validation against the UPMS SQL unique key constraints for Email and Employee ID.</body></html>");
        note.setFont(Theme.FONT_SMALL);
        note.setForeground(new Color(0x1E40AF));
        notePanel.add(note);
        p.add(notePanel, gc);

        // Buttons
        gc.gridy = labels.length + 3; gc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        JButton cancel = new JButton("Discard"); cancel.setFont(Theme.FONT_BODY);
        JButton save = Theme.primaryButton(existing == null ? "Commit Entry" : "Save Changes");
        btnRow.add(cancel);
        btnRow.add(save);
        p.add(btnRow, gc);

        save.addActionListener(e -> {
            try {
                String empId = idF.getText().trim();
                String name  = nameF.getText().trim();
                String email = emailF.getText().trim();
                if (empId.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Employee ID is required.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                if (name.isEmpty())  { JOptionPane.showMessageDialog(dlg, "Full name is required.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                if (email.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Email is required.", "Error", JOptionPane.ERROR_MESSAGE); return; }

                Employee emp = new Employee();
                emp.setEmpId(empId); emp.setName(name); emp.setDesignation(designF.getText().trim());
                emp.setEmail(email); emp.setPhone(phoneF.getText().trim());
                String joinDate = dateF.getText().trim();
                emp.setJoinDate(joinDate.isEmpty() ? null : new SimpleDateFormat("yyyy-MM-dd").parse(joinDate));
                emp.setEmploymentType((String) typeC.getSelectedItem());
                emp.setDeptId(deptIds[deptC.getSelectedIndex()]);

                boolean ok = existing == null ? empDAO.insert(emp) : empDAO.update(emp);
                if (ok) {
                    JOptionPane.showMessageDialog(dlg, existing == null ? "Employee added successfully." : "Employee updated successfully.");
                    dlg.dispose();
                    notifyDataChanged();
                } else JOptionPane.showMessageDialog(dlg, empDAO.getLastErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dlg.dispose());

        JScrollPane formScroll = new JScrollPane(p);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setPreferredSize(new Dimension(580, 640));
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        dlg.setContentPane(formScroll);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
