package upms.ui.panels;

import upms.dao.BonusDAO;
import upms.model.Bonus;
import upms.ui.util.Theme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class BonusPanel extends JPanel {
    private final BonusDAO dao = new BonusDAO();
    private JTable table;
    private DefaultTableModel model;
    private Runnable dataChangeListener = () -> {};
    private static final String[] COLS = {"Bonus ID","Type","Amount"};

    public BonusPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CONTENT_BG);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(Theme.headerLabel("Bonus Management"), BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Theme.CONTENT_BG);
        JButton addBtn  = Theme.primaryButton("+ Add Bonus");
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
        for (Bonus b : dao.getAll())
            model.addRow(new Object[]{b.getBonusId(), b.getType(), String.format("%.2f", b.getAmount())});
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
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a bonus to edit."); return; }
        String id = (String) model.getValueAt(row, 0);
        for (Bonus b : dao.getAll()) if (b.getBonusId().equals(id)) { showForm(b); return; }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a bonus to delete."); return; }
        String id = (String) model.getValueAt(row, 0);
        int linkedPayrolls = dao.countPayrollLinks(id);
        String message = "Delete bonus " + id + "?";
        if (linkedPayrolls == 1) message += "\nThis bonus will be removed from 1 payroll record.";
        else if (linkedPayrolls > 1) message += "\nThis bonus will be removed from " + linkedPayrolls + " payroll records.";
        int c = JOptionPane.showConfirmDialog(this, message, "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            if (dao.delete(id)) {
                String success = "Bonus deleted successfully.";
                if (linkedPayrolls == 1) success += "\n1 payroll total was updated.";
                else if (linkedPayrolls > 1) success += "\n" + linkedPayrolls + " payroll totals were updated.";
                JOptionPane.showMessageDialog(this, success);
                notifyDataChanged();
            }
            else JOptionPane.showMessageDialog(this, "Unable to delete bonus.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showForm(Bonus existing) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add Bonus" : "Edit Bonus", true);
        dlg.setSize(360, 220);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(8, 4, 8, 4);

        JTextField idF     = Theme.styledField();
        JComboBox<String> typeC = Theme.styledCombo("Performance","Festival");
        JTextField amtF    = Theme.styledField();

        if (existing != null) {
            idF.setText(existing.getBonusId()); idF.setEditable(false);
            typeC.setSelectedItem(existing.getType());
            amtF.setText(String.valueOf(existing.getAmount()));
        } else {
            idF.setText(dao.nextId()); idF.setEditable(false);
        }

        String[] labels = {"Bonus ID","Type","Amount"};
        JComponent[] fields = {idF, typeC, amtF};
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
                    JOptionPane.showMessageDialog(dlg, "Bonus amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Bonus b = new Bonus(idF.getText().trim(), amount, (String)typeC.getSelectedItem());
                boolean ok = existing == null ? dao.insert(b) : dao.update(b);
                if (ok) {
                    String success = existing == null ? "Bonus added successfully." : "Bonus updated successfully.";
                    if (existing != null) {
                        int linkedPayrolls = dao.countPayrollLinks(existing.getBonusId());
                        if (linkedPayrolls == 1) success += "\n1 linked payroll total was updated.";
                        else if (linkedPayrolls > 1) success += "\n" + linkedPayrolls + " linked payroll totals were updated.";
                    }
                    JOptionPane.showMessageDialog(dlg, success);
                    dlg.dispose();
                    notifyDataChanged();
                }
                else JOptionPane.showMessageDialog(dlg, "Unable to save bonus.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.setContentPane(p); dlg.setVisible(true);
    }
}
