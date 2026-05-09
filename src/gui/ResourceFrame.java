package gui;

import database.ResourceDAO;
import models.Resource;
import models.User;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ResourceFrame extends JFrame {

    private final User currentUser;
    private final ResourceDAO dao = new ResourceDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtName, txtUnit, txtLocation, txtDonatedBy;
    private JSpinner spnQuantity;
    private JComboBox<String> cmbCategory, cmbStatus;
    private JLabel lblStats;

    private static final String[] COLS = {"ID","Name","Category","Quantity","Unit","Location","Status","Donated By"};

    public ResourceFrame(User user) {
        this.currentUser = user;
        setTitle("Resource Management — RescueNet AI");
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI(); loadData(); setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        add(hdr(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildForm(), buildTable());
        split.setDividerLocation(175); split.setDividerSize(4); split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel outer=new JPanel(new BorderLayout()); outer.setBackground(UITheme.BG_DARK);
        outer.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));
        JPanel form=new JPanel(new GridBagLayout()); form.setBackground(UITheme.BG_CARD);
        form.setBorder(UITheme.sectionBorder("Add / Edit Resource",UITheme.ACCENT_PURPLE));
        GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(4,8,4,8);

        txtName     =UITheme.styledField(); txtUnit=UITheme.styledField();
        txtLocation =UITheme.styledField(); txtDonatedBy=UITheme.styledField();
        spnQuantity =new JSpinner(new SpinnerNumberModel(0,0,100000,1)); styleSpinner(spnQuantity);
        cmbCategory =UITheme.styledCombo(new String[]{"MEDICAL","FOOD","WATER","CLOTHING","EQUIPMENT","TRANSPORT","OTHER"});
        cmbStatus   =UITheme.styledCombo(new String[]{"AVAILABLE","IN_USE","DEPLETED","RESERVED"});

        r(form,g,0,0,"Name *:",txtName,"Category:",cmbCategory);
        r(form,g,0,1,"Quantity:",spnQuantity,"Unit:",txtUnit);
        r(form,g,1,0,"Location:",txtLocation,"Status:",cmbStatus);
        g.gridx=0;g.gridy=2;g.weightx=0; form.add(lbl("Donated By:"),g);
        g.gridx=1;g.gridwidth=3;g.weightx=1; form.add(txtDonatedBy,g); g.gridwidth=1;

        JPanel btns=new JPanel(new GridLayout(4,1,0,6)); btns.setOpaque(false);
        JButton bA=UITheme.primaryButton("➕ Add",UITheme.ACCENT_GREEN);
        JButton bU=UITheme.primaryButton("✏️ Update",UITheme.ACCENT_BLUE);
        JButton bD=UITheme.primaryButton("🗑 Delete",UITheme.ACCENT_RED);
        JButton bC=UITheme.primaryButton("🔄 Clear",UITheme.BG_PANEL);
        bA.addActionListener(e->addResource()); bU.addActionListener(e->updateResource());
        bD.addActionListener(e->deleteResource()); bC.addActionListener(e->clearForm());
        btns.add(bA);btns.add(bU);btns.add(bD);btns.add(bC);
        g.gridx=3;g.gridy=0;g.gridheight=3;g.fill=GridBagConstraints.BOTH;g.weightx=0;
        form.add(btns,g); g.gridheight=1;g.fill=GridBagConstraints.HORIZONTAL;
        outer.add(form,BorderLayout.CENTER); return outer;
    }

    private JPanel buildTable() {
        JPanel outer=new JPanel(new BorderLayout(0,5)); outer.setBackground(UITheme.BG_DARK);
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

    private void addResource() {
        try{
            if(txtName.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Name required.","Validation",JOptionPane.WARNING_MESSAGE);return;}
            Resource r=new Resource(txtName.getText().trim(),(String)cmbCategory.getSelectedItem(),
                (Integer)spnQuantity.getValue(),txtUnit.getText().trim(),txtLocation.getText().trim(),
                (String)cmbStatus.getSelectedItem(),txtDonatedBy.getText().trim());
            dao.addResource(r); loadData(); clearForm();
            JOptionPane.showMessageDialog(this,"✅ Resource added!","Success",JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void updateResource() {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a resource first.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
        try{
            int id=(int)tableModel.getValueAt(row,0);
            Resource r=new Resource(id,txtName.getText().trim(),(String)cmbCategory.getSelectedItem(),
                (Integer)spnQuantity.getValue(),txtUnit.getText().trim(),txtLocation.getText().trim(),
                (String)cmbStatus.getSelectedItem(),txtDonatedBy.getText().trim());
            dao.updateResource(r); loadData(); clearForm();
            JOptionPane.showMessageDialog(this,"✅ Resource updated!","Updated",JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void deleteResource() {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a resource first.","No Selection",JOptionPane.WARNING_MESSAGE);return;}
        int id=(int)tableModel.getValueAt(row,0);
        if(JOptionPane.showConfirmDialog(this,"Delete resource ID "+id+"?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{dao.deleteResource(id);loadData();clearForm();}catch(Exception ex){JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
    }

    private void loadData() {
        SwingWorker<List<Resource>,Void> w=new SwingWorker<>(){
            protected List<Resource> doInBackground() throws Exception{return dao.getAllResources();}
            protected void done(){
                try{
                    List<Resource> list=get(); tableModel.setRowCount(0);
                    for(Resource r:list)tableModel.addRow(new Object[]{r.getResourceId(),r.getName(),r.getCategory(),r.getQuantity(),r.getUnit(),r.getLocation(),r.getStatus(),r.getDonatedBy()});
                    int total=list.stream().mapToInt(Resource::getQuantity).sum();
                    lblStats.setText("  Items: "+list.size()+"  |  Total Quantity: "+total+"  |  Categories: Medical, Food, Water, Clothing, Equipment");
                }catch(Exception ignored){}
            }
        };
        w.execute();
    }

    private void fillForm(){
        int row=table.getSelectedRow(); if(row<0)return;
        txtName.setText(tableModel.getValueAt(row,1).toString());
        setC(cmbCategory,tableModel.getValueAt(row,2).toString());
        spnQuantity.setValue(tableModel.getValueAt(row,3));
        txtUnit.setText(tableModel.getValueAt(row,4).toString());
        txtLocation.setText(tableModel.getValueAt(row,5).toString());
        setC(cmbStatus,tableModel.getValueAt(row,6).toString());
        txtDonatedBy.setText(tableModel.getValueAt(row,7).toString());
    }

    private void clearForm(){txtName.setText("");txtUnit.setText("");txtLocation.setText("");txtDonatedBy.setText("");spnQuantity.setValue(0);cmbCategory.setSelectedIndex(0);cmbStatus.setSelectedIndex(0);table.clearSelection();}
    private JPanel hdr(){JPanel h=new JPanel(new BorderLayout());h.setBackground(UITheme.BG_HEADER);h.setBorder(BorderFactory.createMatteBorder(0,0,2,0,UITheme.ACCENT_PURPLE));h.setPreferredSize(new Dimension(0,50));JLabel l=new JLabel("  📦  Resource Inventory Management");l.setFont(UITheme.FONT_HEADER);l.setForeground(UITheme.ACCENT_PURPLE);JButton b=UITheme.primaryButton("← Back",new Color(45,30,80));b.setFont(UITheme.FONT_SMALL);b.addActionListener(e->dispose());b.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));h.add(l,BorderLayout.WEST);h.add(b,BorderLayout.EAST);return h;}
    private void r(JPanel f,GridBagConstraints g,int row,int col,String l1,JComponent c1,String l2,JComponent c2){int bx=col*2;g.gridx=bx;g.gridy=row;g.weightx=0;f.add(lbl(l1),g);g.gridx=bx+1;g.weightx=1;f.add(c1,g);if(col==0){g.gridx=2;g.weightx=0;f.add(lbl(l2),g);g.gridx=3;g.weightx=1;f.add(c2,g);}}
    private void styleSpinner(JSpinner s){s.getEditor().getComponent(0).setBackground(UITheme.BG_INPUT);((JSpinner.DefaultEditor)s.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);s.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));}
    private void setC(JComboBox<String> c,String v){for(int i=0;i<c.getItemCount();i++)if(c.getItemAt(i).equalsIgnoreCase(v)){c.setSelectedIndex(i);return;}}
    private JLabel lbl(String t){JLabel l=new JLabel(t);l.setFont(UITheme.FONT_NORMAL);l.setForeground(UITheme.TEXT_SECONDARY);return l;}
}
