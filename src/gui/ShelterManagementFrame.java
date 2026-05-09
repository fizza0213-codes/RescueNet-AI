package gui;

import database.ShelterDAO;
import models.Shelter;
import models.User;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ShelterManagementFrame extends JFrame {

    private final User currentUser;
    private final ShelterDAO dao = new ShelterDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtName, txtLocation, txtCity, txtContact, txtInCharge, txtNotes;
    private JSpinner spnCapacity, spnOccupied;
    private JComboBox<String> cmbStatus;
    private JCheckBox chkMedical, chkFood, chkWater;
    private JLabel lblStats;

    private static final String[] COLS = {"ID","Name","City","Capacity","Occupied","Available","Status","Contact","In Charge","Medical","Food","Water"};

    public ShelterManagementFrame(User user) {
        this.currentUser = user;
        setTitle("Shelter Management — RescueNet AI");
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        loadData();
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        add(UITheme.moduleHeader("Shelter Management", UITheme.ACCENT_ORANGE, this::dispose), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildForm(), buildTable());
        split.setDividerLocation(210);
        split.setDividerSize(4);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UITheme.BG_DARK);
        outer.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(UITheme.sectionBorder("Add / Edit Shelter", UITheme.ACCENT_ORANGE));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 8, 4, 8);

        txtName     = UITheme.styledField();
        txtLocation = UITheme.styledField();
        txtCity     = UITheme.styledField();
        txtContact  = UITheme.styledField();
        txtInCharge = UITheme.styledField();
        txtNotes    = UITheme.styledField();
        spnCapacity = new JSpinner(new SpinnerNumberModel(100,0,10000,10));
        spnOccupied = new JSpinner(new SpinnerNumberModel(0,0,10000,1));
        styleSpinner(spnCapacity); styleSpinner(spnOccupied);
        cmbStatus   = UITheme.styledCombo(new String[]{"OPEN","FULL","CLOSED","DAMAGED"});
        chkMedical  = check("Medical Aid"); chkFood = check("Food"); chkWater = check("Water");
        chkFood.setSelected(true); chkWater.setSelected(true);

        row(form,g,0,0,"Name *:",txtName,"City *:",txtCity);
        row(form,g,0,1,"Location:",txtLocation,"Status:",cmbStatus);
        row(form,g,1,0,"Capacity:",spnCapacity,"Occupied:",spnOccupied);
        row(form,g,1,1,"In Charge:",txtInCharge,"Contact:",txtContact);

        g.gridx=0;g.gridy=2;g.weightx=0; form.add(lbl("Amenities:"),g);
        g.gridx=1;g.gridwidth=3;g.weightx=1;
        JPanel amen = new JPanel(new FlowLayout(FlowLayout.LEFT,16,0));
        amen.setOpaque(false); amen.add(chkMedical); amen.add(chkFood); amen.add(chkWater);
        form.add(amen,g); g.gridwidth=1;

        g.gridx=0;g.gridy=3;g.weightx=0; form.add(lbl("Notes:"),g);
        g.gridx=1;g.gridwidth=2;g.weightx=1; form.add(txtNotes,g); g.gridwidth=1;

        JPanel btns = new JPanel(new GridLayout(4,1,0,6));
        btns.setOpaque(false);
        JButton btnAdd  = UITheme.primaryButton("➕ Add",UITheme.ACCENT_GREEN);
        JButton btnUpd  = UITheme.primaryButton("✏️ Update",UITheme.ACCENT_BLUE);
        JButton btnDel  = UITheme.primaryButton("🗑 Delete",UITheme.ACCENT_RED);
        JButton btnClr  = UITheme.primaryButton("🔄 Clear",UITheme.BG_PANEL);
        btnAdd.addActionListener(e->addShelter());
        btnUpd.addActionListener(e->updateShelter());
        btnDel.addActionListener(e->deleteShelter());
        btnClr.addActionListener(e->clearForm());
        btns.add(btnAdd);btns.add(btnUpd);btns.add(btnDel);btns.add(btnClr);
        g.gridx=3;g.gridy=0;g.gridheight=4;g.fill=GridBagConstraints.BOTH;g.weightx=0;
        form.add(btns,g);
        g.gridheight=1;g.fill=GridBagConstraints.HORIZONTAL;

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildTable() {
        JPanel outer = new JPanel(new BorderLayout(0,5));
        outer.setBackground(UITheme.BG_DARK);
        outer.setBorder(BorderFactory.createEmptyBorder(5,10,10,10));

        tableModel = new DefaultTableModel(COLS,0){public boolean isCellEditable(int r,int c){return false;}};
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(java.awt.event.MouseEvent e){if(e.getClickCount()==2)fillFormFromTable();}
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        sp.getViewport().setBackground(UITheme.BG_CARD);

        lblStats = new JLabel("Loading...");
        lblStats.setFont(UITheme.FONT_SMALL);
        lblStats.setForeground(UITheme.TEXT_SECONDARY);

        outer.add(sp,BorderLayout.CENTER);
        outer.add(lblStats,BorderLayout.SOUTH);
        return outer;
    }

    private void addShelter() {
        try {
            if(txtName.getText().trim().isEmpty()||txtCity.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(this,"Name and City are required.","Validation",JOptionPane.WARNING_MESSAGE);return;
            }
            Shelter s = new Shelter(txtName.getText().trim(),txtLocation.getText().trim(),
                txtCity.getText().trim(),(Integer)spnCapacity.getValue(),(Integer)spnOccupied.getValue(),
                (String)cmbStatus.getSelectedItem(),txtContact.getText().trim(),txtInCharge.getText().trim(),
                chkMedical.isSelected(),chkFood.isSelected(),chkWater.isSelected(),txtNotes.getText().trim());
            dao.addShelter(s);
            loadData(); clearForm();
            JOptionPane.showMessageDialog(this,"✅ Shelter added!","Success",JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void updateShelter() {
        int row = table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a shelter first.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
        try {
            int id = (int)tableModel.getValueAt(row,0);
            Shelter s = new Shelter(id,txtName.getText().trim(),txtLocation.getText().trim(),
                txtCity.getText().trim(),(Integer)spnCapacity.getValue(),(Integer)spnOccupied.getValue(),
                (String)cmbStatus.getSelectedItem(),txtContact.getText().trim(),txtInCharge.getText().trim(),
                chkMedical.isSelected(),chkFood.isSelected(),chkWater.isSelected(),txtNotes.getText().trim());
            dao.updateShelter(s);
            loadData(); clearForm();
            JOptionPane.showMessageDialog(this,"✅ Shelter updated!","Updated",JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void deleteShelter() {
        int row = table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a shelter first.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
        int id=(int)tableModel.getValueAt(row,0);
        if(JOptionPane.showConfirmDialog(this,"Delete shelter ID "+id+"?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        try{dao.deleteShelter(id);loadData();clearForm();} catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void loadData() {
        SwingWorker<List<Shelter>,Void> w = new SwingWorker<>(){
            protected List<Shelter> doInBackground() throws Exception{return dao.getAllShelters();}
            protected void done(){
                try{
                    List<Shelter> list=get();
                    tableModel.setRowCount(0);
                    for(Shelter s:list){
                        tableModel.addRow(new Object[]{s.getShelterId(),s.getName(),s.getCity(),
                            s.getCapacity(),s.getOccupied(),s.getAvailable(),s.getStatus(),
                            s.getContact(),s.getInCharge(),
                            s.hasMedical()?"✅":"❌",s.hasFood()?"✅":"❌",s.hasWater()?"✅":"❌"});
                    }
                    int open=(int)list.stream().filter(s->s.getStatus().equals("OPEN")).count();
                    int totalCap=list.stream().mapToInt(Shelter::getCapacity).sum();
                    int totalOcc=list.stream().mapToInt(Shelter::getOccupied).sum();
                    lblStats.setText("  Total: "+list.size()+" shelters  |  Open: "+open+"  |  Capacity: "+totalCap+"  |  Occupied: "+totalOcc+"  |  Available: "+(totalCap-totalOcc));
                } catch(Exception ignored){}
            }
        };
        w.execute();
    }

    private void fillFormFromTable() {
        int row=table.getSelectedRow(); if(row<0)return;
        txtName.setText(tableModel.getValueAt(row,1).toString());
        txtCity.setText(tableModel.getValueAt(row,2).toString());
        spnCapacity.setValue(tableModel.getValueAt(row,3));
        spnOccupied.setValue(tableModel.getValueAt(row,4));
        setCombo(cmbStatus,tableModel.getValueAt(row,6).toString());
        txtContact.setText(tableModel.getValueAt(row,7).toString());
        txtInCharge.setText(tableModel.getValueAt(row,8).toString());
        chkMedical.setSelected(tableModel.getValueAt(row,9).toString().contains("✅"));
        chkFood.setSelected(tableModel.getValueAt(row,10).toString().contains("✅"));
        chkWater.setSelected(tableModel.getValueAt(row,11).toString().contains("✅"));
    }

    private void clearForm(){
        txtName.setText("");txtLocation.setText("");txtCity.setText("");
        txtContact.setText("");txtInCharge.setText("");txtNotes.setText("");
        spnCapacity.setValue(100);spnOccupied.setValue(0);
        cmbStatus.setSelectedIndex(0);chkMedical.setSelected(false);
        chkFood.setSelected(true);chkWater.setSelected(true);
        table.clearSelection();
    }

    // header delegated to UITheme.moduleHeader
    private void row(JPanel f,GridBagConstraints g,int row,int col,String l1,JComponent c1,String l2,JComponent c2){
        int bx=col*2;
        g.gridx=bx;g.gridy=row;g.weightx=0;f.add(lbl(l1),g);
        g.gridx=bx+1;g.weightx=1;f.add(c1,g);
        if(col==0){g.gridx=2;g.weightx=0;f.add(lbl(l2),g);g.gridx=3;g.weightx=1;f.add(c2,g);}
    }
    private void styleSpinner(JSpinner s){s.getEditor().getComponent(0).setBackground(UITheme.BG_INPUT);((JSpinner.DefaultEditor)s.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);s.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));}
    private void setCombo(JComboBox<String> c,String v){for(int i=0;i<c.getItemCount();i++)if(c.getItemAt(i).equalsIgnoreCase(v)){c.setSelectedIndex(i);return;}}
    private JCheckBox check(String t){JCheckBox c=new JCheckBox(t);c.setBackground(UITheme.BG_CARD);c.setForeground(UITheme.TEXT_PRIMARY);c.setFont(UITheme.FONT_NORMAL);return c;}
    private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(UITheme.FONT_NORMAL);l.setForeground(UITheme.TEXT_SECONDARY);return l;}
}
