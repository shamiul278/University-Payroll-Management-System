package upms.ui.panels;

import upms.dao.DeductionDAO;
import upms.model.Deduction;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class DeductionPanel extends JPanel {
    private final DeductionDAO dao = new DeductionDAO();
    private JTable table;
    private DefaultTableModel model;
    private Runnable dataChangeListener = () -> {};
    private static final String[] COLS = {"Deduction ID","Reason","Amount"};

    public DeductionPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(Theme.headerLabel("Deduction Management"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.CONTENT_BG);
        JButton addBtn  = Theme.primaryButton("+ Add Deduction");
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
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(right);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));
        JPanel card = Theme.cardPanel(null);
        card.add(scroll, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        for (Deduction d : dao.getAll())
            model.addRow(new Object[]{d.getDeductionId(), d.getReason(), String.format("%.2f", d.getAmount())});
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
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a deduction to edit."); return; }
        String id = (String) model.getValueAt(row, 0);
        for (Deduction d : dao.getAll()) if (d.getDeductionId().equals(id)) { showForm(d); return; }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a deduction to delete."); return; }
        String id = (String) model.getValueAt(row, 0);
        int linkedPayrolls = dao.countPayrollLinks(id);
        String message = "Delete deduction " + id + "?";
        if (linkedPayrolls == 1) message += "\nThis deduction will be removed from 1 payroll record.";
        else if (linkedPayrolls > 1) message += "\nThis deduction will be removed from " + linkedPayrolls + " payroll records.";
        int c = JOptionPane.showConfirmDialog(this, message, "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            if (dao.delete(id)) {
                String success = "Deduction deleted successfully.";
                if (linkedPayrolls == 1) success += "\n1 payroll total was updated.";
                else if (linkedPayrolls > 1) success += "\n" + linkedPayrolls + " payroll totals were updated.";
                JOptionPane.showMessageDialog(this, success);
                notifyDataChanged();
            }
            else JOptionPane.showMessageDialog(this, "Unable to delete deduction.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showForm(Deduction existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add Deduction" : "Edit Deduction", true);
        dlg.setSize(380, 220);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(8, 4, 8, 4);

        JTextField idF  = Theme.styledField();
        JComboBox<String> reasonC = Theme.styledCombo("Tax","Loan Repayment","Absence Penalty");
        JTextField amtF = Theme.styledField();

        if (existing != null) {
            idF.setText(existing.getDeductionId()); idF.setEditable(false);
            reasonC.setSelectedItem(existing.getReason());
            amtF.setText(String.valueOf(existing.getAmount()));
        } else {
            idF.setText(dao.nextId()); idF.setEditable(false);
        }

        String[] labels = {"Deduction ID","Reason","Amount"};
        JComponent[] fields = {idF, reasonC, amtF};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.4; p.add(Theme.label(labels[i]), gc);
            gc.gridx = 1; gc.weightx = 0.6; p.add(fields[i], gc);
        }

        JButton save = Theme.primaryButton(existing == null ? "Add" : "Save");
        JButton cancel = new JButton("Cancel"); cancel.setFont(Theme.FONT_BODY);
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btnRow.setBackground(Color.WHITE);
        btnRow.add(cancel); btnRow.add(save);
        p.add(btnRow, gc);

        save.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amtF.getText().trim());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(dlg, "Deduction amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Deduction d = new Deduction(idF.getText().trim(), amount, (String)reasonC.getSelectedItem());
                boolean ok = existing == null ? dao.insert(d) : dao.update(d);
                if (ok) {
                    String success = existing == null ? "Deduction added successfully." : "Deduction updated successfully.";
                    if (existing != null) {
                        int linkedPayrolls = dao.countPayrollLinks(existing.getDeductionId());
                        if (linkedPayrolls == 1) success += "\n1 linked payroll total was updated.";
                        else if (linkedPayrolls > 1) success += "\n" + linkedPayrolls + " linked payroll totals were updated.";
                    }
                    JOptionPane.showMessageDialog(dlg, success);
                    dlg.dispose();
                    notifyDataChanged();
                }
                else JOptionPane.showMessageDialog(dlg, "Unable to save deduction.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(p); dlg.setVisible(true);
    }
}
