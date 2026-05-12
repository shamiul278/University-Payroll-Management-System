package upms.ui.panels;

import upms.dao.EmployeeDAO;
import upms.dao.UserDAO;
import upms.model.Employee;
import upms.model.User;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class UserMgmtPanel extends JPanel {
    private final UserDAO userDAO = new UserDAO();
    private final EmployeeDAO empDAO = new EmployeeDAO();
    private JTable table;
    private DefaultTableModel model;
    private static final String[] COLS = {"User ID","Username","Role","Employee ID"};

    public UserMgmtPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(Theme.headerLabel("User Management"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.CONTENT_BG);
        JButton addBtn  = Theme.primaryButton("+ Add User");
        JButton editBtn = Theme.primaryButton("✏ Edit");
        JButton delBtn  = Theme.dangerButton("🗑 Delete");
        editBtn.setBackground(new Color(0x744210));
        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(delBtn);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(COLS, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        Theme.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));
        JPanel card = Theme.cardPanel(null);
        card.add(scroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        for (User u : userDAO.getAll())
            model.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getRole(), u.getEmpId()});
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user to edit."); return; }
        String id = (String) model.getValueAt(row, 0);
        for (User u : userDAO.getAll()) if (u.getUserId().equals(id)) { showForm(u); return; }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user to delete."); return; }
        String id = (String) model.getValueAt(row, 0);
        int c = JOptionPane.showConfirmDialog(this, "Delete user " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION && userDAO.delete(id)) { JOptionPane.showMessageDialog(this, "Deleted."); refresh(); }
    }

    private void showForm(User existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add User" : "Edit User", true);
        dlg.setSize(400, 280);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(8, 4, 8, 4);

        List<Employee> emps = empDAO.getAll();
        String[] empIds   = emps.stream().map(Employee::getEmpId).toArray(String[]::new);
        String[] empNames = emps.stream().map(e -> e.getEmpId() + " - " + e.getName()).toArray(String[]::new);

        JTextField idF   = Theme.styledField();
        JTextField userF = Theme.styledField();
        JPasswordField passF = new JPasswordField(); passF.setFont(Theme.FONT_BODY);
        passF.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Theme.BORDER,1,true),new EmptyBorder(6,10,6,10)));
        JComboBox<String> roleC = Theme.styledCombo("Administrator","Accountant");
        JComboBox<String> empC  = new JComboBox<>(empNames); empC.setFont(Theme.FONT_BODY);

        if (existing != null) {
            idF.setText(existing.getUserId()); idF.setEditable(false);
            userF.setText(existing.getUsername());
            passF.setText(existing.getPassword());
            roleC.setSelectedItem(existing.getRole());
            for (int i = 0; i < empIds.length; i++) if (empIds[i].equals(existing.getEmpId())) { empC.setSelectedIndex(i); break; }
        } else {
            idF.setText(userDAO.nextId()); idF.setEditable(false);
        }

        String[] labels = {"User ID","Username","Password","Role","Employee"};
        JComponent[] fields = {idF, userF, passF, roleC, empC};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.35; p.add(Theme.label(labels[i]), gc);
            gc.gridx = 1; gc.weightx = 0.65; p.add(fields[i], gc);
        }

        JButton save = Theme.primaryButton(existing == null ? "Add User" : "Save");
        JButton cancel = new JButton("Cancel"); cancel.setFont(Theme.FONT_BODY);
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btnRow.setBackground(Color.WHITE);
        btnRow.add(cancel); btnRow.add(save);
        p.add(btnRow, gc);

        save.addActionListener(e -> {
            User u = new User(idF.getText().trim(), userF.getText().trim(),
                new String(passF.getPassword()), (String)roleC.getSelectedItem(),
                empIds[empC.getSelectedIndex()]);
            boolean ok = existing == null ? userDAO.insert(u) : userDAO.update(u);
            if (ok) { JOptionPane.showMessageDialog(dlg, "Saved."); dlg.dispose(); refresh(); }
            else JOptionPane.showMessageDialog(dlg, "Failed. Username may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(p); dlg.setVisible(true);
    }
}
