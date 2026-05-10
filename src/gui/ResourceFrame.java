package gui;

import database.RescueTeamDAO;
import models.RescueTeam;
import models.User;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class RescueTeamFrame extends JFrame {

    private final User currentUser;
    private final RescueTeamDAO dao = new RescueTeamDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtName, txtLeader, txtPhone, txtVehicle, txtVehicleNo, txtSpecial, txtLocation;
    private JSpinner spnMembers;
    private JComboBox<String> cmbStatus;
    private JLabel lblStats;

    private static final String[] COLS = {"ID","Team Name","Leader","Phone","Members","Vehicle","Plate","Specialization","Status","Location"};

    public RescueTeamFrame(User user) {
        this.currentUser = user;
        setTitle("Rescue Teams — RescueNet AI");
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        buildUI(); loadData(); setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        add(UITheme.moduleHeader("Rescue Team Management", UITheme.ACCENT_RED, this::dispose), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildForm(), buildTable());
        split.setDividerLocation(230);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setResizeWeight(0.0);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout(12, 0));
        outer.setBackground(UITheme.BG_DARK);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 6, 5, 6);

        txtName     = UITheme.styledField();
        txtLeader   = UITheme.styledField();
        txtPhone    = UITheme.styledField();
        txtVehicle  = UITheme.styledField();
        txtVehicleNo= UITheme.styledField();
        txtSpecial  = UITheme.styledField();
        txtLocation = UITheme.styledField();
        spnMembers  = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        styleSpinner(spnMembers);
        cmbStatus   = UITheme.styledCombo(new String[]{"AVAILABLE","ON_MISSION","OFF_DUTY","MAINTENANCE"});

        addRow(form, g, 0, "Team Name *",     txtName,     "Leader",         txtLeader);
        addRow(form, g, 1, "Leader Phone",    txtPhone,    "Members",        spnMembers);
        addRow(form, g, 2, "Vehicle",         txtVehicle,  "Plate No",       txtVehicleNo);
        addRow(form, g, 3, "Specialization",  txtSpecial,  "Status",         cmbStatus);

        // Current Location full-width row
        g.gridx = 0; g.gridy = 4; g.weightx = 0; g.gridwidth = 1;
        form.add(lbl("Current Location"), g);
        g.gridx = 1; g.gridwidth = 3; g.weightx = 1;
        form.add(txtLocation, g);
        g.gridwidth = 1;

        outer.add(form, BorderLayout.CENTER);

        // ── Buttons on right ──────────────────────────────────
        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        btnPanel.setBackground(UITheme.BG_DARK);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        btnPanel.setPreferredSize(new Dimension(155, 0));

        JButton btnAdd = UITheme.primaryButton("➕  Add Team",      UITheme.ACCENT_GREEN);
        JButton btnUpd = UITheme.primaryButton("✏️  Update Record", UITheme.ACCENT_BLUE);
        JButton btnDel = UITheme.primaryButton("🗑  Remove",        UITheme.ACCENT_RED);
        JButton btnClr = UITheme.primaryButton("✕  Clear Form",    UITheme.TEXT_SECONDARY);

        btnAdd.addActionListener(e -> addTeam());
        btnUpd.addActionListener(e -> updateTeam());
        btnDel.addActionListener(e -> deleteTeam());
        btnClr.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd); btnPanel.add(btnUpd);
        btnPanel.add(btnDel); btnPanel.add(btnClr);

        outer.add(btnPanel, BorderLayout.EAST);
        return outer;
    }

    private void addRow(JPanel form, GridBagConstraints g,
                        int row, String l1, JComponent c1, String l2, JComponent c2) {
        g.gridx = 0; g.gridy = row; g.weightx = 0; form.add(lbl(l1), g);
        g.gridx = 1; g.weightx = 1;                form.add(c1, g);
        g.gridx = 2; g.weightx = 0;                form.add(lbl(l2), g);
        g.gridx = 3; g.weightx = 1;                form.add(c2, g);
    }

    private JPanel buildTable() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBackground(UITheme.BG_DARK);
        outer.setBorder(BorderFactory.createEmptyBorder(4, 14, 14, 14));

        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {50, 130, 140, 110, 70, 110, 80, 130, 100, 150};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Color rows by status
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String status = tableModel.getRowCount() > row && tableModel.getValueAt(row, 8) != null
                        ? tableModel.getValueAt(row, 8).toString() : "";
                if (!sel) {
                    if (status.equals("ON_MISSION"))   { setBackground(new Color(255, 245, 215)); }
                    else if (status.equals("OFF_DUTY")) { setBackground(new Color(240, 240, 245)); }
                    else                                { setBackground(UITheme.BG_CARD); }
                } else { setBackground(new Color(210, 225, 255)); }
                setForeground(UITheme.TEXT_PRIMARY);
                setFont(UITheme.FONT_NORMAL);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) fillForm();
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        sp.getViewport().setBackground(UITheme.BG_CARD);

        lblStats = new JLabel("  Loading...");
        lblStats.setFont(UITheme.FONT_SMALL);
        lblStats.setForeground(UITheme.TEXT_SECONDARY);
        lblStats.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));

        outer.add(sp, BorderLayout.CENTER);
        outer.add(lblStats, BorderLayout.SOUTH);
        return outer;
    }

    // ── CRUD (logic unchanged) ────────────────────────────────

    private void addTeam() {
        try {
            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Team Name is required.", "Validation", JOptionPane.WARNING_MESSAGE); return;
            }
            RescueTeam t = new RescueTeam(txtName.getText().trim(), txtLeader.getText().trim(),
                    txtPhone.getText().trim(), (Integer)spnMembers.getValue(), txtVehicle.getText().trim(),
                    txtVehicleNo.getText().trim(), txtSpecial.getText().trim(),
                    (String)cmbStatus.getSelectedItem(), txtLocation.getText().trim());
            dao.addTeam(t); loadData(); clearForm();
            JOptionPane.showMessageDialog(this, "Team added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void updateTeam() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a team first.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        try {
            int id = (int)tableModel.getValueAt(row, 0);
            RescueTeam t = new RescueTeam(id, txtName.getText().trim(), txtLeader.getText().trim(),
                    txtPhone.getText().trim(), (Integer)spnMembers.getValue(), txtVehicle.getText().trim(),
                    txtVehicleNo.getText().trim(), txtSpecial.getText().trim(),
                    (String)cmbStatus.getSelectedItem(), txtLocation.getText().trim());
            dao.updateTeam(t); loadData(); clearForm();
            JOptionPane.showMessageDialog(this, "Team updated successfully.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void deleteTeam() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a team first.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        int id = (int)tableModel.getValueAt(row, 0);
        String name = tableModel.getValueAt(row, 1).toString();
        if (JOptionPane.showConfirmDialog(this, "Remove team \"" + name + "\" (ID: " + id + ")?\n\nThis cannot be undone.",
                "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try { dao.deleteTeam(id); loadData(); clearForm(); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void loadData() {
        SwingWorker<List<RescueTeam>, Void> w = new SwingWorker<>() {
            protected List<RescueTeam> doInBackground() throws Exception { return dao.getAllTeams(); }
            protected void done() {
                try {
                    List<RescueTeam> list = get();
                    tableModel.setRowCount(0);
                    for (RescueTeam t : list) {
                        tableModel.addRow(new Object[]{
                                t.getTeamId(), t.getTeamName(), t.getLeader(), t.getLeaderPhone(),
                                t.getMembers(), t.getVehicle(), t.getVehicleNo(),
                                t.getSpecialization(), t.getStatus(), t.getCurrentLocation()
                        });
                    }
                    long avail = list.stream().filter(t -> t.getStatus().equals("AVAILABLE")).count();
                    long mission = list.stream().filter(t -> t.getStatus().equals("ON_MISSION")).count();
                    lblStats.setText("  Total: " + list.size() + " teams  |  Available: " + avail
                            + "  |  On Mission: " + mission + "  |  Other: " + (list.size() - avail - mission));
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void fillForm() {
        int row = table.getSelectedRow(); if (row < 0) return;
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        txtLeader.setText(tableModel.getValueAt(row, 2).toString());
        txtPhone.setText(tableModel.getValueAt(row, 3).toString());
        spnMembers.setValue(tableModel.getValueAt(row, 4));
        txtVehicle.setText(tableModel.getValueAt(row, 5).toString());
        txtVehicleNo.setText(tableModel.getValueAt(row, 6).toString());
        txtSpecial.setText(tableModel.getValueAt(row, 7).toString());
        setC(cmbStatus, tableModel.getValueAt(row, 8).toString());
        txtLocation.setText(tableModel.getValueAt(row, 9).toString());
    }

    private void clearForm() {
        txtName.setText(""); txtLeader.setText(""); txtPhone.setText("");
        txtVehicle.setText(""); txtVehicleNo.setText(""); txtSpecial.setText(""); txtLocation.setText("");
        spnMembers.setValue(5); cmbStatus.setSelectedIndex(0); table.clearSelection();
    }

    // ── Helpers ───────────────────────────────────────────────
    private void styleSpinner(JSpinner s) {
        s.getEditor().getComponent(0).setBackground(UITheme.BG_INPUT);
        ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);
        s.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
    }
    private void setC(JComboBox<String> c, String v) {
        for (int i = 0; i < c.getItemCount(); i++)
            if (c.getItemAt(i).equalsIgnoreCase(v)) { c.setSelectedIndex(i); return; }
    }
    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(UITheme.FONT_NORMAL); l.setForeground(UITheme.TEXT_SECONDARY); return l;
    }
}
