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
import java.util.*;
import java.util.List;

public class EmployeePanel extends JPanel {
    private final EmployeeDAO empDAO = new EmployeeDAO();
    private final DepartmentDAO deptDAO = new DepartmentDAO();
    private JTable table;
    private DefaultTableModel model;
    private static final String[] COLS = {"Emp ID","Name","Designation","Email","Phone","Join Date","Type","Dept"};

    public EmployeePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(Theme.headerLabel("Employee Management"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.CONTENT_BG);
        JButton addBtn = Theme.primaryButton("+ Add Employee");
        JButton editBtn = Theme.primaryButton("✏ Edit");
        JButton delBtn  = Theme.dangerButton("🗑 Delete");

        editBtn.setBackground(new Color(0x744210));
        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());

        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(delBtn);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setBackground(Theme.CONTENT_BG);
        searchPanel.setBorder(new EmptyBorder(0, 0, 12, 0));
        JTextField search = Theme.styledField();
        search.setPreferredSize(new Dimension(280, 34));
        search.putClientProperty("JTextField.placeholderText", "Search by name or ID...");
        JButton searchBtn = Theme.primaryButton("Search");
        searchBtn.addActionListener(e -> filterTable(search.getText().trim()));
        search.addActionListener(e -> filterTable(search.getText().trim()));
        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(Theme.FONT_BODY);
        clearBtn.addActionListener(e -> { search.setText(""); refresh(); });
        searchPanel.add(search); searchPanel.add(Box.createHorizontalStrut(8));
        searchPanel.add(searchBtn); searchPanel.add(Box.createHorizontalStrut(4)); searchPanel.add(clearBtn);
        add(searchPanel, BorderLayout.CENTER);

        // Table
        model = new DefaultTableModel(COLS, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel tableCard = Theme.cardPanel(null);
        tableCard.add(searchPanel, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        // Replace center with tableCard
        remove(getComponent(1)); // remove searchPanel from CENTER
        add(tableCard, BorderLayout.CENTER);

        refresh();
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

    private void filterTable(String query) {
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String q = query.toLowerCase();
        for (Employee e : empDAO.getAll()) {
            if (e.getName().toLowerCase().contains(q) || e.getEmpId().toLowerCase().contains(q)
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
        String id = (String) model.getValueAt(row, 0);
        showForm(empDAO.getById(id));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an employee to delete.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete employee " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (empDAO.delete(id)) { JOptionPane.showMessageDialog(this, "Employee deleted."); refresh(); }
            else JOptionPane.showMessageDialog(this, "Cannot delete (may have related records).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showForm(Employee existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add Employee" : "Edit Employee", true);
        dlg.setSize(480, 520);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(6, 4, 6, 4);

        List<Department> depts = deptDAO.getAll();
        String[] deptIds = depts.stream().map(Department::getDeptId).toArray(String[]::new);
        String[] deptNames = depts.stream().map(d -> d.getDeptId() + " - " + d.getDeptName()).toArray(String[]::new);

        JTextField idF = Theme.styledField();
        JTextField nameF = Theme.styledField();
        JTextField designF = Theme.styledField();
        JTextField emailF = Theme.styledField();
        JTextField phoneF = Theme.styledField();
        JTextField dateF = Theme.styledField(); dateF.setToolTipText("yyyy-MM-dd");
        JComboBox<String> typeC = Theme.styledCombo("Full-Time","Part-Time","Contractual");
        JComboBox<String> deptC = new JComboBox<>(deptNames);
        deptC.setFont(Theme.FONT_BODY);

        if (existing != null) {
            idF.setText(existing.getEmpId()); idF.setEditable(false);
            nameF.setText(existing.getName()); designF.setText(existing.getDesignation());
            emailF.setText(existing.getEmail()); phoneF.setText(existing.getPhone());
            if (existing.getJoinDate() != null) dateF.setText(new SimpleDateFormat("yyyy-MM-dd").format(existing.getJoinDate()));
            typeC.setSelectedItem(existing.getEmploymentType());
            for (int i = 0; i < deptIds.length; i++) if (deptIds[i].equals(existing.getDeptId())) { deptC.setSelectedIndex(i); break; }
        } else {
            idF.setText(empDAO.nextId()); idF.setEditable(false);
        }

        String[] labels = {"Employee ID","Full Name","Designation","Email","Phone","Join Date (yyyy-MM-dd)","Employment Type","Department"};
        JComponent[] fields = {idF, nameF, designF, emailF, phoneF, dateF, typeC, deptC};

        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.3;
            p.add(Theme.label(labels[i]), gc);
            gc.gridx = 1; gc.weightx = 0.7;
            p.add(fields[i], gc);
        }

        JButton save = Theme.primaryButton(existing == null ? "Add Employee" : "Save Changes");
        JButton cancel = new JButton("Cancel");
        cancel.setFont(Theme.FONT_BODY);

        gc.gridx = 0; gc.gridy = labels.length; gc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(cancel); btnRow.add(save);
        p.add(btnRow, gc);

        save.addActionListener(e -> {
            try {
                Employee emp = new Employee();
                emp.setEmpId(idF.getText().trim());
                emp.setName(nameF.getText().trim());
                emp.setDesignation(designF.getText().trim());
                emp.setEmail(emailF.getText().trim());
                emp.setPhone(phoneF.getText().trim());
                emp.setJoinDate(new SimpleDateFormat("yyyy-MM-dd").parse(dateF.getText().trim()));
                emp.setEmploymentType((String) typeC.getSelectedItem());
                emp.setDeptId(deptIds[deptC.getSelectedIndex()]);

                boolean ok = existing == null ? empDAO.insert(emp) : empDAO.update(emp);
                if (ok) { JOptionPane.showMessageDialog(dlg, "Saved successfully."); dlg.dispose(); refresh(); }
                else JOptionPane.showMessageDialog(dlg, "Failed to save. Check for duplicate email/ID.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dlg.dispose());

        dlg.setContentPane(new JScrollPane(p));
        dlg.setVisible(true);
    }
}
