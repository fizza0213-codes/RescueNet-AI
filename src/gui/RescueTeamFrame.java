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
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI(); loadData(); setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(hdr("🚑  Rescue Team Management", new Color(180,50,50)), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildForm(), buildTable());
        split.setDividerLocation(190); split.setDividerSize(4); split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UITheme.BG_DARK);
        outer.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(UITheme.sectionBorder("Add / Edit Rescue Team", new Color(180,50,50)));
        GridBagConstraints g = new GridBagConstraints();
        g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(4,8,4,8);

        txtName    = UITheme.styledField(); txtLeader  = UITheme.styledField();
        txtPhone   = UITheme.styledField(); txtVehicle = UITheme.styledField();
        txtVehicleNo= UITheme.styledField(); txtSpecial = UITheme.styledField();
        txtLocation= UITheme.styledField();
        spnMembers = new JSpinner(new SpinnerNumberModel(5,1,100,1));
        styleSpinner(spnMembers);
        cmbStatus  = UITheme.styledCombo(new String[]{"AVAILABLE","ON_MISSION","OFF_DUTY","MAINTENANCE"});

        r(form,g,0,0,"Team Name *:",txtName,"Leader:",txtLeader);
        r(form,g,0,1,"Leader Phone:",txtPhone,"Members:",spnMembers);
        r(form,g,1,0,"Vehicle:",txtVehicle,"Plate No:",txtVehicleNo);
        r(form,g,1,1,"Specialization:",txtSpecial,"Status:",cmbStatus);
        g.gridx=0;g.gridy=2;g.weightx=0; form.add(lbl("Current Location:"),g);
        g.gridx=1;g.gridwidth=3;g.weightx=1; form.add(txtLocation,g); g.gridwidth=1;

        JPanel btns=new JPanel(new GridLayout(4,1,0,6)); btns.setOpaque(false);
        JButton bA=UITheme.primaryButton("➕ Add",UITheme.ACCENT_GREEN);
        JButton bU=UITheme.primaryButton("✏️ Update",UITheme.ACCENT_BLUE);
        JButton bD=UITheme.primaryButton("🗑 Delete",UITheme.ACCENT_RED);
        JButton bC=UITheme.primaryButton("🔄 Clear",UITheme.BG_PANEL);
        bA.addActionListener(e->addTeam()); bU.addActionListener(e->updateTeam());
        bD.addActionListener(e->deleteTeam()); bC.addActionListener(e->clearForm());
        btns.add(bA);btns.add(bU);btns.add(bD);btns.add(bC);
        g.gridx=3;g.gridy=0;g.gridheight=3;g.fill=GridBagConstraints.BOTH;g.weightx=0;
        form.add(btns,g); g.gridheight=1;g.fill=GridBagConstraints.HORIZONTAL;
        outer.add(form, BorderLayout.CENTER); return outer;
    }

    private JPanel buildTable() {
        JPanel outer=new JPanel(new BorderLayout(0,5));
        outer.setBackground(UITheme.BG_DARK);
        outer.setBorder(BorderFactory.createEmptyBorder(5,10,10,10));
        tableModel=new DefaultTableModel(COLS,0){public boolean isCellEditable(int r,int c){return false;}};
        table=new JTable(tableModel); UITheme.styleTable(table);
        table.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(java.awt.event.MouseEvent e){if(e.getClickCount()==2)fillForm();}
        });
        JScrollPane sp=new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        sp.getViewport().setBackground(UITheme.BG_CARD);
        lblStats=new JLabel("Loading..."); lblStats.setFont(UITheme.FONT_SMALL); lblStats.setForeground(UITheme.TEXT_SECONDARY);
        outer.add(sp,BorderLayout.CENTER); outer.add(lblStats,BorderLayout.SOUTH); return outer;
    }

    private void addTeam() {
        try {
            if(txtName.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Team Name required.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            RescueTeam t=new RescueTeam(txtName.getText().trim(),txtLeader.getText().trim(),txtPhone.getText().trim(),
                (Integer)spnMembers.getValue(),txtVehicle.getText().trim(),txtVehicleNo.getText().trim(),
                txtSpecial.getText().trim(),(String)cmbStatus.getSelectedItem(),txtLocation.getText().trim());
            dao.addTeam(t); loadData(); clearForm();
            JOptionPane.showMessageDialog(this,"✅ Team added!","Success",JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void updateTeam() {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a team first.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
        try {
            int id=(int)tableModel.getValueAt(row,0);
            RescueTeam t=new RescueTeam(id,txtName.getText().trim(),txtLeader.getText().trim(),txtPhone.getText().trim(),
                (Integer)spnMembers.getValue(),txtVehicle.getText().trim(),txtVehicleNo.getText().trim(),
                txtSpecial.getText().trim(),(String)cmbStatus.getSelectedItem(),txtLocation.getText().trim());
            dao.updateTeam(t); loadData(); clearForm();
            JOptionPane.showMessageDialog(this,"✅ Team updated!","Updated",JOptionPane.INFORMATION_MESSAGE);
        } catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void deleteTeam() {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a team first.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
        int id=(int)tableModel.getValueAt(row,0);
        if(JOptionPane.showConfirmDialog(this,"Delete team ID "+id+"?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{dao.deleteTeam(id);loadData();clearForm();} catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void loadData() {
        SwingWorker<List<RescueTeam>,Void> w=new SwingWorker<>(){
            protected List<RescueTeam> doInBackground() throws Exception{return dao.getAllTeams();}
            protected void done(){
                try {
                    List<RescueTeam> list=get(); tableModel.setRowCount(0);
                    for(RescueTeam t:list){tableModel.addRow(new Object[]{t.getTeamId(),t.getTeamName(),t.getLeader(),t.getLeaderPhone(),t.getMembers(),t.getVehicle(),t.getVehicleNo(),t.getSpecialization(),t.getStatus(),t.getCurrentLocation()});}
                    long avail=list.stream().filter(t->t.getStatus().equals("AVAILABLE")).count();
                    lblStats.setText("  Total: "+list.size()+" teams  |  Available: "+avail+"  |  On Mission: "+(list.size()-avail));
                } catch(Exception ignored){}
            }
        };
        w.execute();
    }

    private void fillForm() {
        int row=table.getSelectedRow(); if(row<0)return;
        txtName.setText(tableModel.getValueAt(row,1).toString());
        txtLeader.setText(tableModel.getValueAt(row,2).toString());
        txtPhone.setText(tableModel.getValueAt(row,3).toString());
        spnMembers.setValue(tableModel.getValueAt(row,4));
        txtVehicle.setText(tableModel.getValueAt(row,5).toString());
        txtVehicleNo.setText(tableModel.getValueAt(row,6).toString());
        txtSpecial.setText(tableModel.getValueAt(row,7).toString());
        setC(cmbStatus,tableModel.getValueAt(row,8).toString());
        txtLocation.setText(tableModel.getValueAt(row,9).toString());
    }

    private void clearForm(){txtName.setText("");txtLeader.setText("");txtPhone.setText("");txtVehicle.setText("");txtVehicleNo.setText("");txtSpecial.setText("");txtLocation.setText("");spnMembers.setValue(5);cmbStatus.setSelectedIndex(0);table.clearSelection();}
    private void r(JPanel f,GridBagConstraints g,int row,int col,String l1,JComponent c1,String l2,JComponent c2){int bx=col*2;g.gridx=bx;g.gridy=row;g.weightx=0;f.add(lbl(l1),g);g.gridx=bx+1;g.weightx=1;f.add(c1,g);if(col==0){g.gridx=2;g.weightx=0;f.add(lbl(l2),g);g.gridx=3;g.weightx=1;f.add(c2,g);}}
    private JPanel hdr(String t,Color c){JPanel h=new JPanel(new BorderLayout());h.setBackground(UITheme.BG_HEADER);h.setBorder(BorderFactory.createMatteBorder(0,0,2,0,c));h.setPreferredSize(new Dimension(0,50));JLabel l=new JLabel("  "+t);l.setFont(UITheme.FONT_HEADER);l.setForeground(c);JButton b=UITheme.primaryButton("← Back",UITheme.BG_HEADER);b.setFont(UITheme.FONT_SMALL);b.addActionListener(e->dispose());b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));h.add(l,BorderLayout.WEST);h.add(b,BorderLayout.EAST);return h;}
    private void styleSpinner(JSpinner s){s.getEditor().getComponent(0).setBackground(UITheme.BG_INPUT);((JSpinner.DefaultEditor)s.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);s.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));}
    private void setC(JComboBox<String> c,String v){for(int i=0;i<c.getItemCount();i++)if(c.getItemAt(i).equalsIgnoreCase(v)){c.setSelectedIndex(i);return;}}
    private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(UITheme.FONT_NORMAL);l.setForeground(UITheme.TEXT_SECONDARY);return l;}
}
