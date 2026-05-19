package upms.ui.panels;

import upms.dao.DepartmentDAO;
import upms.model.Department;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class DepartmentPanel extends JPanel {
    private final DepartmentDAO dao = new DepartmentDAO();
    private JTable table;
    private DefaultTableModel model;
    private Runnable dataChangeListener = () -> {};
    private static final String[] COLS = {"Dept ID", "Department Name", "Building", "Contact No"};

    public DepartmentPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(Theme.headerLabel("Department Setup"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.CONTENT_BG);
        JButton addBtn  = Theme.primaryButton("+ Add Department");
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));
        JPanel card = Theme.cardPanel(null);
        card.add(scroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        for (Department d : dao.getAll())
            model.addRow(new Object[]{d.getDeptId(), d.getDeptName(), d.getBuilding(), d.getContactNo()});
    }

    public void setDataChangeListener(Runnable listener) {
        dataChangeListener = listener != null ? listener : () -> {};
    }

    private void notifyDataChanged() {
        refresh();
        dataChangeListener.run();
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a department to edit."); return; }
        showForm(dao.getById((String) model.getValueAt(row, 0)));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a department to delete."); return; }
        String id = (String) model.getValueAt(row, 0);
        int employeeCount = dao.countEmployees(id);
        String message = "Delete department " + id + "?";
        if (employeeCount == 1) message += "\n1 employee will become unassigned.";
        else if (employeeCount > 1) message += "\n" + employeeCount + " employees will become unassigned.";

        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.delete(id)) {
                String success = "Department deleted successfully.";
                if (employeeCount == 1) success += "\n1 employee is now unassigned.";
                else if (employeeCount > 1) success += "\n" + employeeCount + " employees are now unassigned.";
                JOptionPane.showMessageDialog(this, success);
                notifyDataChanged();
            }
            else JOptionPane.showMessageDialog(this, "Unable to delete department.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showForm(Department existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add Department" : "Edit Department", true);
        dlg.setSize(420, 300);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(8, 4, 8, 4);

        JTextField idF    = Theme.styledField();
        JTextField nameF  = Theme.styledField();
        JTextField bldgF  = Theme.styledField();
        JTextField contF  = Theme.styledField();

        if (existing != null) {
            idF.setText(existing.getDeptId()); idF.setEditable(false);
            nameF.setText(existing.getDeptName()); bldgF.setText(existing.getBuilding());
            contF.setText(existing.getContactNo());
        } else {
            idF.setText(dao.nextId()); idF.setEditable(false);
        }

        String[] labels = {"Department ID", "Department Name", "Building", "Contact No"};
        JTextField[] fields = {idF, nameF, bldgF, contF};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.35; p.add(Theme.label(labels[i]), gc);
            gc.gridx = 1; gc.weightx = 0.65; p.add(fields[i], gc);
        }

        JButton save = Theme.primaryButton(existing == null ? "Add" : "Save");
        JButton cancel = new JButton("Cancel"); cancel.setFont(Theme.FONT_BODY);
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(Color.WHITE); btnRow.add(cancel); btnRow.add(save);
        p.add(btnRow, gc);

        save.addActionListener(e -> {
            Department d = new Department(idF.getText().trim(), nameF.getText().trim(),
                bldgF.getText().trim(), contF.getText().trim());
            boolean ok = existing == null ? dao.insert(d) : dao.update(d);
            if (ok) {
                JOptionPane.showMessageDialog(dlg, existing == null ? "Department added successfully." : "Department updated successfully.");
                dlg.dispose();
                notifyDataChanged();
            }
            else JOptionPane.showMessageDialog(dlg, "Unable to save department.", "Error", JOptionPane.ERROR_MESSAGE);
        });
        cancel.addActionListener(e -> dlg.dispose());

        dlg.setContentPane(p); dlg.setVisible(true);
    }
}
