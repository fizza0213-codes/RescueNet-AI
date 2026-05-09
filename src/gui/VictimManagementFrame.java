package gui;

import bst.VictimBST;
import database.VictimDAO;
import models.User;
import models.Victim;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Victim Management — powered by an internal BST for fast lookups.
 * The BST is used as the data engine; users interact with a clean
 * professional UI without any internal implementation details exposed.
 */
public class VictimManagementFrame extends JFrame {

    private final VictimBST    bst = new VictimBST();
    private final VictimDAO    dao = new VictimDAO();
    private final User         currentUser;

    private DefaultTableModel  tableModel;
    private JTextField         txtName, txtCnic, txtLocation, txtContact, txtNextOfKin, txtSearch;
    private JTextArea          txtNotes;
    private JComboBox<String>  cmbDisaster, cmbSeverity, cmbStatus, cmbGender, cmbSearchType;
    private JSpinner           spnAge;
    private JTable             table;
    private JLabel             lblRecordCount;

    private static final String[] COLS = {
        "ID", "Full Name", "CNIC", "Age", "Gender",
        "Disaster Type", "Severity", "Status", "Location", "Contact", "Registered"
    };

    public VictimManagementFrame(User user) {
        this.currentUser = user;
        setTitle("RescueNet AI — Victim Registry");
        setSize(1300, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 600));
        buildUI();
        loadFromDB();
        setVisible(true);
    }

    // ── UI construction ──────────────────────────────────────

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        add(UITheme.moduleHeader("Victim Registry", UITheme.ACCENT_BLUE, this::dispose),
            BorderLayout.NORTH);

        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildFormPanel(), buildTablePanel());
        main.setDividerLocation(230);
        main.setDividerSize(6);
        main.setBackground(UITheme.BG_DARK);
        main.setBorder(null);
        main.setResizeWeight(0.0);

        add(main, BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
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
        g.insets = new Insets(4, 6, 4, 6);

        // Initialise all fields
        txtName      = UITheme.styledField();
        txtCnic      = UITheme.styledField();
        txtLocation  = UITheme.styledField();
        txtContact   = UITheme.styledField();
        txtNextOfKin = UITheme.styledField();
        txtNotes     = UITheme.styledTextArea(); txtNotes.setRows(2);
        spnAge       = new JSpinner(new SpinnerNumberModel(0, 0, 120, 1));
        styleSpinner(spnAge);

        cmbDisaster = UITheme.styledCombo(new String[]{
            "Flood","Earthquake","Fire","Landslide","Storm","Building Collapse","Cyclone","Other"});
        cmbSeverity = UITheme.styledCombo(new String[]{"1","2","3","4","5","6","7","8","9","10"});
        cmbSeverity.setSelectedIndex(4);
        cmbStatus   = UITheme.styledCombo(new String[]{"ACTIVE","RESCUED","MISSING","DECEASED","RECOVERING"});
        cmbGender   = UITheme.styledCombo(new String[]{"Male","Female","Other","Unknown"});

        // Row 0
        addRow(form, g, 0, "Full Name *", txtName,    "Disaster Type *", cmbDisaster);
        addRow(form, g, 1, "CNIC",        txtCnic,    "Severity (1–10) *", cmbSeverity);
        addRow(form, g, 2, "Age",         spnAge,     "Status *", cmbStatus);
        addRow(form, g, 3, "Gender",      cmbGender,  "Location *", txtLocation);
        addRow(form, g, 4, "Contact",     txtContact, "Next of Kin", txtNextOfKin);

        // Notes row
        g.gridx = 0; g.gridy = 5; g.weightx = 0; g.gridwidth = 1;
        form.add(lbl("Notes"), g);
        g.gridx = 1; g.gridwidth = 5; g.weightx = 1;
        form.add(new JScrollPane(txtNotes), g);
        g.gridwidth = 1;

        outer.add(form, BorderLayout.CENTER);

        // Buttons panel on right
        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        btnPanel.setBackground(UITheme.BG_DARK);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        btnPanel.setPreferredSize(new Dimension(150, 0));

        JButton btnAdd    = UITheme.primaryButton("➕  Register Victim", UITheme.ACCENT_GREEN);
        JButton btnUpdate = UITheme.primaryButton("✏️  Update Record",   UITheme.ACCENT_BLUE);
        JButton btnDelete = UITheme.primaryButton("🗑  Remove",          UITheme.ACCENT_RED);
        JButton btnClear  = UITheme.primaryButton("✕  Clear Form",      UITheme.TEXT_SECONDARY);

        btnAdd.addActionListener(e    -> addVictim());
        btnUpdate.addActionListener(e -> updateVictim());
        btnDelete.addActionListener(e -> deleteSelected());
        btnClear.addActionListener(e  -> clearForm());

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);

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

    private JPanel buildTablePanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 6));
        outer.setBackground(UITheme.BG_DARK);
        outer.setBorder(BorderFactory.createEmptyBorder(4, 14, 14, 14));

        // ── Toolbar ───────────────────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout(0, 0));
        toolbar.setBackground(UITheme.BG_PANEL);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Search section
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        searchRow.add(lbl("Search by:"));

        cmbSearchType = new JComboBox<>(new String[]{"Name", "CNIC", "Location", "ID"});
        cmbSearchType.setFont(UITheme.FONT_NORMAL);
        cmbSearchType.setBackground(UITheme.BG_INPUT);
        cmbSearchType.setPreferredSize(new Dimension(100, 30));

        txtSearch = UITheme.styledField();
        txtSearch.setPreferredSize(new Dimension(200, 30));
        txtSearch.addActionListener(e -> doSearch());

        JButton btnSearch  = UITheme.primaryButton("Search", UITheme.ACCENT_BLUE);
        JButton btnShowAll = UITheme.primaryButton("Show All", UITheme.TEXT_SECONDARY);
        btnSearch.setFont(UITheme.FONT_SMALL);
        btnShowAll.setFont(UITheme.FONT_SMALL);

        btnSearch.addActionListener(e  -> doSearch());
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); refreshTable(bst.inorder()); });

        searchRow.add(cmbSearchType);
        searchRow.add(txtSearch);
        searchRow.add(btnSearch);
        searchRow.add(btnShowAll);

        // Filter section
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterRow.setOpaque(false);
        filterRow.add(lbl("Filter:"));

        JButton btnCritical   = UITheme.primaryButton("🔴 Critical (≥8)",  new Color(180, 30, 30));
        JButton btnMissing    = UITheme.primaryButton("🟡 Missing",         new Color(170, 100, 0));
        JButton btnActive     = UITheme.primaryButton("🟢 Active",          new Color(20, 120, 60));
        JButton btnRefresh    = UITheme.primaryButton("↺ Refresh",          UITheme.BG_NAV);
        btnCritical.setFont(UITheme.FONT_SMALL);
        btnMissing.setFont(UITheme.FONT_SMALL);
        btnActive.setFont(UITheme.FONT_SMALL);
        btnRefresh.setFont(UITheme.FONT_SMALL);

        btnCritical.addActionListener(e -> refreshTable(bst.filterBySeverity(8)));
        btnMissing.addActionListener(e  -> refreshTable(bst.filterByStatus("MISSING")));
        btnActive.addActionListener(e   -> refreshTable(bst.filterByStatus("ACTIVE")));
        btnRefresh.addActionListener(e  -> loadFromDB());

        filterRow.add(btnCritical); filterRow.add(btnMissing);
        filterRow.add(btnActive);   filterRow.add(btnRefresh);

        toolbar.add(searchRow, BorderLayout.WEST);
        toolbar.add(filterRow, BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {55, 140, 120, 45, 70, 120, 75, 95, 150, 110, 140};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Row renderer: color by severity
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                try {
                    int sev = Integer.parseInt(tableModel.getValueAt(row, 6).toString());
                    setBackground(sel ? new Color(210, 225, 255) : UITheme.severityRowColor(sev));
                    setForeground(UITheme.TEXT_PRIMARY);
                    // Bold severity cell
                    setFont(col == 6 ? UITheme.FONT_BOLD : UITheme.FONT_NORMAL);
                    if (col == 6 && !sel) setForeground(UITheme.severityBadgeColor(sev));
                } catch (Exception ignored) {
                    setBackground(sel ? new Color(210, 225, 255) : UITheme.BG_CARD);
                    setForeground(UITheme.TEXT_PRIMARY);
                    setFont(UITheme.FONT_NORMAL);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        // Double-click → populate form
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) populateFormFromTable();
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        sp.getViewport().setBackground(UITheme.BG_CARD);

        // ── Status bar ────────────────────────────────────────
        lblRecordCount = new JLabel("  Loading records...");
        lblRecordCount.setFont(UITheme.FONT_SMALL);
        lblRecordCount.setForeground(UITheme.TEXT_SECONDARY);
        lblRecordCount.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));

        outer.add(toolbar, BorderLayout.NORTH);
        outer.add(sp,      BorderLayout.CENTER);
        outer.add(lblRecordCount, BorderLayout.SOUTH);

        return outer;
    }

    // ── Search ───────────────────────────────────────────────

    private void doSearch() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable(bst.inorder());
            return;
        }
        String type = (String) cmbSearchType.getSelectedItem();
        List<Victim> all = bst.inorder();

        List<Victim> results;
        switch (type) {
            case "ID":
                try {
                    int id = Integer.parseInt(query);
                    Victim v = bst.search(id);    // O(log n) BST lookup
                    results = v != null ? List.of(v) : List.of();
                } catch (NumberFormatException ex) {
                    results = List.of();
                }
                break;
            case "CNIC":
                results = all.stream()
                    .filter(v -> v.getCnic().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                break;
            case "Location":
                results = all.stream()
                    .filter(v -> v.getLocation().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                break;
            default: // Name
                results = all.stream()
                    .filter(v -> v.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
                break;
        }

        refreshTable(results);
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No records found matching your search.",
                "No Results", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── CRUD actions ─────────────────────────────────────────

    private void addVictim() {
        try {
            String name    = txtName.getText().trim();
            String loc     = txtLocation.getText().trim();
            if (name.isEmpty() || loc.isEmpty()) {
                warn("Full Name and Location are required fields.");
                return;
            }
            Victim v = new Victim(
                name,
                txtCnic.getText().trim(),
                (Integer) spnAge.getValue(),
                (String) cmbGender.getSelectedItem(),
                (String) cmbDisaster.getSelectedItem(),
                Integer.parseInt((String) cmbSeverity.getSelectedItem()),
                (String) cmbStatus.getSelectedItem(),
                loc,
                txtContact.getText().trim(),
                txtNextOfKin.getText().trim(),
                txtNotes.getText().trim()
            );
            v.setRegisteredBy(currentUser.getUserId());
            dao.addVictim(v);
            bst.insert(v);
            refreshTable(bst.inorder());
            clearForm();
            JOptionPane.showMessageDialog(this,
                "Victim registered successfully.\nAssigned ID: " + v.getVictimId(),
                "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            err("Failed to register victim: " + ex.getMessage());
        }
    }

    private void updateVictim() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Please select a record from the table first."); return; }
        try {
            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            Victim v = bst.search(id);
            if (v == null) { err("Record not found. Please refresh and try again."); return; }

            String name = txtName.getText().trim();
            String loc  = txtLocation.getText().trim();
            if (name.isEmpty() || loc.isEmpty()) { warn("Full Name and Location are required."); return; }

            v.setName(name);
            v.setCnic(txtCnic.getText().trim());
            v.setAge((Integer) spnAge.getValue());
            v.setGender((String) cmbGender.getSelectedItem());
            v.setDisasterType((String) cmbDisaster.getSelectedItem());
            v.setSeverityLevel(Integer.parseInt((String) cmbSeverity.getSelectedItem()));
            v.setStatus((String) cmbStatus.getSelectedItem());
            v.setLocation(loc);
            v.setContact(txtContact.getText().trim());
            v.setNextOfKin(txtNextOfKin.getText().trim());
            v.setNotes(txtNotes.getText().trim());

            dao.updateVictim(v);
            bst.insert(v);
            refreshTable(bst.inorder());
            clearForm();
            JOptionPane.showMessageDialog(this, "Record updated successfully.",
                "Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            err("Update failed: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Please select a record to remove."); return; }
        int id   = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String nm = tableModel.getValueAt(row, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove \"" + nm + "\" (ID: " + id + ") from the registry?\n\nThis cannot be undone.",
            "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            bst.delete(id);
            dao.deleteVictim(id);
            refreshTable(bst.inorder());
            clearForm();
        } catch (Exception ex) {
            err("Removal failed: " + ex.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        Victim v = bst.search(id);
        if (v == null) return;
        txtName.setText(v.getName());
        txtCnic.setText(v.getCnic());
        spnAge.setValue(v.getAge());
        setCombo(cmbGender,   v.getGender());
        setCombo(cmbDisaster, v.getDisasterType());
        setCombo(cmbSeverity, String.valueOf(v.getSeverityLevel()));
        setCombo(cmbStatus,   v.getStatus());
        txtLocation.setText(v.getLocation());
        txtContact.setText(v.getContact());
        txtNextOfKin.setText(v.getNextOfKin());
        txtNotes.setText(v.getNotes());
    }

    private void loadFromDB() {
        SwingWorker<List<Victim>, Void> worker = new SwingWorker<>() {
            protected List<Victim> doInBackground() throws Exception {
                return dao.getAllVictims();
            }
            protected void done() {
                try {
                    bst.clear();
                    for (Victim v : get()) bst.insert(v);
                    refreshTable(bst.inorder());
                } catch (Exception ex) {
                    lblRecordCount.setText("  ⚠ Could not load records: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void refreshTable(List<Victim> list) {
        tableModel.setRowCount(0);
        for (Victim v : list) {
            tableModel.addRow(new Object[]{
                v.getVictimId(), v.getName(), v.getCnic(), v.getAge(), v.getGender(),
                v.getDisasterType(), v.getSeverityLevel(), v.getStatus(),
                v.getLocation(), v.getContact(), v.getRegisteredAtStr()
            });
        }
        lblRecordCount.setText("  " + list.size() + " record(s) shown  |  Total in registry: " + bst.size());
    }

    private void clearForm() {
        txtName.setText(""); txtCnic.setText(""); txtLocation.setText("");
        txtContact.setText(""); txtNextOfKin.setText(""); txtNotes.setText("");
        spnAge.setValue(0);
        cmbGender.setSelectedIndex(0); cmbDisaster.setSelectedIndex(0);
        cmbSeverity.setSelectedIndex(4); cmbStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    private void setCombo(JComboBox<String> c, String val) {
        for (int i = 0; i < c.getItemCount(); i++)
            if (c.getItemAt(i).equalsIgnoreCase(val)) { c.setSelectedIndex(i); return; }
    }

    private void styleSpinner(JSpinner s) {
        JComponent ed = s.getEditor();
        ed.getComponent(0).setBackground(UITheme.BG_INPUT);
        ((JSpinner.DefaultEditor) ed).getTextField().setForeground(UITheme.TEXT_PRIMARY);
        s.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_NORMAL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
