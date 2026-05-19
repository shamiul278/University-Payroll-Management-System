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
    private Runnable dataChangeListener = () -> {};
    private static final String[] COLS = {"Emp ID", "Name", "Designation", "Email", "Phone", "Join Date", "Type", "Dept"};

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
        clearBtn.addActionListener(e -> { search.setText(""); refresh(); });
        searchPanel.add(search);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);
        tableCard.add(searchPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(42);

        // Avatar renderer for Name column (index 1)
        table.getColumnModel().getColumn(1).setCellRenderer(new AvatarNameRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
        refresh();
    }

    /** Renders the Name column with a colored avatar circle + text. */
    private static class AvatarNameRenderer extends DefaultTableCellRenderer {
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

    public void refresh() {
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Employee e : empDAO.getAll()) {
            model.addRow(new Object[]{
                e.getEmpId(), e.getName(), e.getDesignation(), e.getEmail(),
                e.getPhone(), e.getJoinDate() != null ? sdf.format(e.getJoinDate()) : "",
                e.getEmploymentType(), e.getDeptId()
            });
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
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String q = query.toLowerCase();
        for (Employee e : empDAO.getAll()) {
            if (e.getName().toLowerCase().contains(q)
                    || e.getEmpId().toLowerCase().contains(q)
                    || e.getEmail().toLowerCase().contains(q)) {
                model.addRow(new Object[]{
                    e.getEmpId(), e.getName(), e.getDesignation(), e.getEmail(),
                    e.getPhone(), e.getJoinDate() != null ? sdf.format(e.getJoinDate()) : "",
                    e.getEmploymentType(), e.getDeptId()
                });
            }
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an employee to edit.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        showForm(empDAO.getById((String) model.getValueAt(row, 0)));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an employee to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete employee " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (empDAO.delete(id)) { JOptionPane.showMessageDialog(this, "Employee deleted successfully."); notifyDataChanged(); }
            else JOptionPane.showMessageDialog(this, "Cannot delete this employee because related records exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showForm(Employee existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "New Employee Entry" : "Edit Employee", true);
        dlg.setSize(500, 540);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));
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
        JLabel note = new JLabel("<html><b>Data Compliance</b><br>Profile will be subject to automatic validation against the UPMS SQL unique key constraints for Email and Employee ID.</html>");
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
                emp.setJoinDate(new SimpleDateFormat("yyyy-MM-dd").parse(dateF.getText().trim()));
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

        dlg.setContentPane(new JScrollPane(p));
        dlg.setVisible(true);
    }
}
