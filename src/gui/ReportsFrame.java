package gui;

import database.*;
import models.User;
import models.Victim;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ReportsFrame extends JFrame {

    private final User currentUser;
    private final VictimDAO victimDAO       = new VictimDAO();
    private final ShelterDAO shelterDAO     = new ShelterDAO();
    private final RescueTeamDAO teamDAO     = new RescueTeamDAO();
    private final ResourceDAO resourceDAO   = new ResourceDAO();
    private JTextArea txtReport;
    private JLabel lblTitle;

    public ReportsFrame(User user) {
        this.currentUser = user;
        setTitle("Reports & Analytics — RescueNet AI");
        setSize(900, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        generateSummaryReport();
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(30, 120, 140)));
        header.setPreferredSize(new Dimension(0, 50));
        lblTitle = new JLabel("  📊  Reports & Analytics");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_WHITE);
        JButton back = UITheme.primaryButton("← Back", new Color(14,128,140));
        back.setFont(UITheme.FONT_SMALL);
        back.addActionListener(e -> dispose());
        back.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        header.add(lblTitle, BorderLayout.WEST);
        header.add(back, BorderLayout.EAST);

        // Report buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnPanel.setBackground(UITheme.BG_CARD);
        btnPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));

        JButton btnSummary  = UITheme.primaryButton("📋 Full Summary",  new Color(30, 120, 140));
        JButton btnVictims  = UITheme.primaryButton("👥 Victim Report",  UITheme.ACCENT_BLUE);
        JButton btnShelters = UITheme.primaryButton("🏠 Shelter Report", UITheme.ACCENT_ORANGE);
        JButton btnTeams    = UITheme.primaryButton("🚑 Team Report",    new Color(180, 50, 50));
        JButton btnResources= UITheme.primaryButton("📦 Resource Report",UITheme.ACCENT_PURPLE);
        JButton btnCritical = UITheme.primaryButton("🔴 Critical Victims",UITheme.ACCENT_RED);

        btnSummary.addActionListener(e  -> generateSummaryReport());
        btnVictims.addActionListener(e  -> generateVictimReport());
        btnShelters.addActionListener(e -> generateShelterReport());
        btnTeams.addActionListener(e    -> generateTeamReport());
        btnResources.addActionListener(e-> generateResourceReport());
        btnCritical.addActionListener(e -> generateCriticalVictimsReport());

        btnPanel.add(btnSummary); btnPanel.add(btnVictims); btnPanel.add(btnShelters);
        btnPanel.add(btnTeams);   btnPanel.add(btnResources); btnPanel.add(btnCritical);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(header, BorderLayout.NORTH);
        topWrapper.add(btnPanel, BorderLayout.SOUTH);
        add(topWrapper, BorderLayout.NORTH);

        // Report text area
        txtReport = new JTextArea();
        txtReport.setBackground(new Color(8, 15, 12));
        txtReport.setForeground(new Color(180, 220, 180));
        txtReport.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtReport.setEditable(false);
        txtReport.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JScrollPane sp = new JScrollPane(txtReport);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        sp.getViewport().setBackground(new Color(8, 15, 12));
        add(sp, BorderLayout.CENTER);

        // Print button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(UITheme.BG_DARK);
        JButton btnPrint = UITheme.primaryButton("🖨️ Print Report", new Color(40, 60, 40));
        btnPrint.addActionListener(e -> {
            try { txtReport.print(); } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage());
            }
        });
        bottom.add(btnPrint);
        add(bottom, BorderLayout.SOUTH);
    }

    private void generateSummaryReport() {
        SwingWorker<String, Void> w = new SwingWorker<>() {
            protected String doInBackground() throws Exception {
                StringBuilder sb = new StringBuilder();
                sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
                sb.append("║            RESCUENET AI — SYSTEM SUMMARY REPORT                ║\n");
                sb.append("╚══════════════════════════════════════════════════════════════════╝\n\n");
                sb.append("Generated:  ").append(new java.util.Date()).append("\n");
                sb.append("Generated By: ").append(currentUser.getFullName()).append(" [").append(currentUser.getRole()).append("]\n\n");

                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("                         VICTIM STATISTICS\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                int total = victimDAO.getTotalCount();
                int active  = victimDAO.getCountByStatus("ACTIVE");
                int rescued = victimDAO.getCountByStatus("RESCUED");
                int missing = victimDAO.getCountByStatus("MISSING");
                int deceased= victimDAO.getCountByStatus("DECEASED");
                sb.append(String.format("  Total Victims Registered  : %d\n", total));
                sb.append(String.format("  Active (Needs Rescue)     : %d\n", active));
                sb.append(String.format("  Rescued                   : %d\n", rescued));
                sb.append(String.format("  Missing                   : %d\n", missing));
                sb.append(String.format("  Deceased                  : %d\n", deceased));
                if (total > 0)
                    sb.append(String.format("  Rescue Success Rate       : %.1f%%\n", (rescued * 100.0 / total)));

                sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("                         SHELTER STATISTICS\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append(String.format("  Open Shelters             : %d\n", shelterDAO.getOpenCount()));
                sb.append(String.format("  Total Capacity            : %d\n", shelterDAO.getTotalCapacity()));
                sb.append(String.format("  Currently Occupied        : %d\n", shelterDAO.getTotalOccupied()));
                int cap=shelterDAO.getTotalCapacity(); int occ=shelterDAO.getTotalOccupied();
                if(cap>0) sb.append(String.format("  Occupancy Rate            : %.1f%%\n",(occ*100.0/cap)));

                sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("                         RESCUE TEAM STATISTICS\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append(String.format("  Total Rescue Teams        : %d\n", teamDAO.getAllTeams().size()));
                sb.append(String.format("  Teams Available           : %d\n", teamDAO.getAvailableCount()));

                sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append("                         RESOURCE STATISTICS\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                sb.append(String.format("  Total Resource Items      : %d\n", resourceDAO.getTotalItems()));

                sb.append("\n═══════════════════════════════════════════════════════════════════\n");
                sb.append("              RescueNet AI — Powered by BST + Gemini AI\n");
                sb.append("═══════════════════════════════════════════════════════════════════\n");
                return sb.toString();
            }
            protected void done() {
                try { txtReport.setText(get()); txtReport.setCaretPosition(0); }
                catch(Exception ignored){}
            }
        };
        w.execute();
    }

    private void generateVictimReport() {
        SwingWorker<String,Void> w=new SwingWorker<>(){
            protected String doInBackground() throws Exception{
                StringBuilder sb=new StringBuilder();
                sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                  COMPLETE VICTIM REPORT                        ║\n");
                sb.append("╚══════════════════════════════════════════════════════════════════╝\n\n");
                sb.append("Generated: ").append(new java.util.Date()).append("\n\n");
                List<Victim> list=victimDAO.getAllVictims();
                sb.append(String.format("%-6s %-20s %-15s %5s %-12s %-20s %-15s\n","ID","Name","Disaster","Sev","Status","Location","Registered"));
                sb.append("─".repeat(100)).append("\n");
                for(Victim v:list){
                    sb.append(String.format("%-6d %-20s %-15s %5d %-12s %-20s %-15s\n",
                        v.getVictimId(),truncate(v.getName(),20),truncate(v.getDisasterType(),15),
                        v.getSeverityLevel(),v.getStatus(),truncate(v.getLocation(),20),v.getRegisteredAtStr()));
                }
                sb.append("\nTotal: ").append(list.size()).append(" victims\n");
                return sb.toString();
            }
            protected void done(){try{txtReport.setText(get());txtReport.setCaretPosition(0);}catch(Exception ignored){}}
        };
        w.execute();
    }

    private void generateCriticalVictimsReport() {
        SwingWorker<String,Void> w=new SwingWorker<>(){
            protected String doInBackground() throws Exception{
                StringBuilder sb=new StringBuilder();
                sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
                sb.append("║              🔴 CRITICAL VICTIMS REPORT (Severity ≥ 8)         ║\n");
                sb.append("╚══════════════════════════════════════════════════════════════════╝\n\n");
                List<Victim> list=victimDAO.getAllVictims();
                list=list.stream().filter(v->v.getSeverityLevel()>=8).sorted((a,b)->b.getSeverityLevel()-a.getSeverityLevel()).toList();
                sb.append("PRIORITY ORDER (Highest severity first)\n");
                sb.append("═".repeat(80)).append("\n\n");
                int rank=1;
                for(Victim v:list){
                    sb.append("RANK #").append(rank++).append(" — ⚠️ SEVERITY: ").append(v.getSeverityLevel()).append("/10 (").append(v.getSeverityLabel()).append(")\n");
                    sb.append("  Name    : ").append(v.getName()).append("\n");
                    sb.append("  Disaster: ").append(v.getDisasterType()).append("  |  Status: ").append(v.getStatus()).append("\n");
                    sb.append("  Location: ").append(v.getLocation()).append("\n");
                    sb.append("  Contact : ").append(v.getContact()).append("  |  Next of Kin: ").append(v.getNextOfKin()).append("\n");
                    if(!v.getNotes().isEmpty()) sb.append("  Notes   : ").append(v.getNotes()).append("\n");
                    sb.append("─".repeat(70)).append("\n\n");
                }
                sb.append("Total critical cases: ").append(list.size()).append("\n");
                return sb.toString();
            }
            protected void done(){try{txtReport.setText(get());txtReport.setCaretPosition(0);}catch(Exception ignored){}}
        };
        w.execute();
    }

    private void generateShelterReport() {
        SwingWorker<String,Void> w=new SwingWorker<>(){
            protected String doInBackground() throws Exception{
                StringBuilder sb=new StringBuilder();
                sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                     SHELTER STATUS REPORT                      ║\n");
                sb.append("╚══════════════════════════════════════════════════════════════════╝\n\n");
                var list=shelterDAO.getAllShelters();
                for(var s:list){
                    sb.append("Shelter: ").append(s.getName()).append(" [").append(s.getStatus()).append("]\n");
                    sb.append("  City: ").append(s.getCity()).append("  |  Location: ").append(s.getLocation()).append("\n");
                    sb.append("  Capacity: ").append(s.getCapacity()).append("  |  Occupied: ").append(s.getOccupied()).append("  |  Available: ").append(s.getAvailable()).append("\n");
                    sb.append(String.format("  Occupancy: %.1f%%\n",s.getOccupancyPct()));
                    sb.append("  Amenities: ").append(s.getAmenitiesStr()).append("\n");
                    sb.append("  In Charge: ").append(s.getInCharge()).append("  |  Contact: ").append(s.getContact()).append("\n");
                    sb.append("─".repeat(70)).append("\n\n");
                }
                return sb.toString();
            }
            protected void done(){try{txtReport.setText(get());txtReport.setCaretPosition(0);}catch(Exception ignored){}}
        };
        w.execute();
    }

    private void generateTeamReport() {
        SwingWorker<String,Void> w=new SwingWorker<>(){
            protected String doInBackground() throws Exception{
                StringBuilder sb=new StringBuilder();
                sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                  RESCUE TEAMS STATUS REPORT                    ║\n");
                sb.append("╚══════════════════════════════════════════════════════════════════╝\n\n");
                var list=teamDAO.getAllTeams();
                for(var t:list){
                    sb.append("Team: ").append(t.getTeamName()).append(" [").append(t.getStatus()).append("]\n");
                    sb.append("  Leader: ").append(t.getLeader()).append("  |  Phone: ").append(t.getLeaderPhone()).append("\n");
                    sb.append("  Members: ").append(t.getMembers()).append("  |  Vehicle: ").append(t.getVehicle()).append(" (").append(t.getVehicleNo()).append(")\n");
                    sb.append("  Specialization: ").append(t.getSpecialization()).append("\n");
                    sb.append("  Location: ").append(t.getCurrentLocation()).append("\n");
                    sb.append("─".repeat(70)).append("\n\n");
                }
                return sb.toString();
            }
            protected void done(){try{txtReport.setText(get());txtReport.setCaretPosition(0);}catch(Exception ignored){}}
        };
        w.execute();
    }

    private void generateResourceReport() {
        SwingWorker<String,Void> w=new SwingWorker<>(){
            protected String doInBackground() throws Exception{
                StringBuilder sb=new StringBuilder();
                sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                   RESOURCE INVENTORY REPORT                    ║\n");
                sb.append("╚══════════════════════════════════════════════════════════════════╝\n\n");
                sb.append(String.format("%-6s %-25s %-12s %10s %-10s %-20s %-12s\n","ID","Name","Category","Quantity","Unit","Location","Status"));
                sb.append("─".repeat(100)).append("\n");
                var list=resourceDAO.getAllResources();
                for(var r:list){
                    sb.append(String.format("%-6d %-25s %-12s %10d %-10s %-20s %-12s\n",
                        r.getResourceId(),truncate(r.getName(),25),r.getCategory(),r.getQuantity(),r.getUnit(),truncate(r.getLocation(),20),r.getStatus()));
                }
                return sb.toString();
            }
            protected void done(){try{txtReport.setText(get());txtReport.setCaretPosition(0);}catch(Exception ignored){}}
        };
        w.execute();
    }

    private String truncate(String s,int max){return s.length()>max?s.substring(0,max-2)+"..":s;}
}
