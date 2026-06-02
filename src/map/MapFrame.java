package map;

import database.VictimDAO;
import database.ShelterDAO;
import database.RescueTeamDAO;
import models.Victim;
import models.Shelter;
import models.RescueTeam;
import gui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class MapFrame extends JFrame {

    private final LiveMapPanel mapPanel;
    private final VictimDAO    victimDAO    = new VictimDAO();
    private final ShelterDAO   shelterDAO   = new ShelterDAO();
    private final RescueTeamDAO teamDAO     = new RescueTeamDAO();

    private JLabel statusLabel;
    private JToggleButton btnVictims, btnShelters, btnTeams;

    public MapFrame() {
        setTitle("RescueNet AI — Live Disaster Map  |  OpenStreetMap");
        setSize(1200, 750);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 500));
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        mapPanel = new LiveMapPanel();

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildToolbar(), BorderLayout.WEST);
        add(new JScrollPane(mapPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                mapPanel.stopTimers();
            }
        });

        // Auto-load data
        SwingUtilities.invokeLater(this::loadAllData);
        setVisible(true);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(UITheme.BG_HEADER);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(14, 128, 140)));
        h.setPreferredSize(new Dimension(0, 50));

        JLabel title = new JLabel("  🗺  RescueNet AI — Live Disaster Map");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Powered by OpenStreetMap  |  Live simulation active  ");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(new Color(140, 180, 220));

        h.add(title, BorderLayout.WEST);
        h.add(sub,   BorderLayout.EAST);
        return h;
    }

    // ── Left toolbar ──────────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel tb = new JPanel();
        tb.setLayout(new BoxLayout(tb, BoxLayout.Y_AXIS));
        tb.setBackground(UITheme.BG_NAV);
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(35, 55, 90)));
        tb.setPreferredSize(new Dimension(170, 0));

        tb.add(Box.createVerticalStrut(10));
        tb.add(section("DATA"));
        tb.add(toolBtn("⬇  Load All Data",     new Color(25, 130, 200), this::loadAllData));
        tb.add(toolBtn("🔄  Refresh Map",       new Color(25, 130, 200), this::loadAllData));

        tb.add(Box.createVerticalStrut(8));
        tb.add(section("LAYERS"));
        btnVictims  = toggleBtn("👥 Victims",  true);
        btnShelters = toggleBtn("🏠 Shelters", true);
        btnTeams    = toggleBtn("🚑 Teams",    true);
        tb.add(btnVictims);
        tb.add(btnShelters);
        tb.add(btnTeams);

        tb.add(Box.createVerticalStrut(8));
        tb.add(section("NAVIGATION"));
        tb.add(toolBtn("🔍+  Zoom In",         new Color(60, 90, 130), () -> mapPanel.zoomIn()));
        tb.add(toolBtn("🔍−  Zoom Out",         new Color(60, 90, 130), () -> mapPanel.zoomOut()));
        tb.add(toolBtn("🇵🇰  Pakistan View",    new Color(0,  120, 60),  () -> mapPanel.centreOnPakistan()));
        tb.add(toolBtn("🏙  Lahore View",       new Color(0,  120, 60),  () -> mapPanel.centreOnLahore()));

        tb.add(Box.createVerticalStrut(8));
        tb.add(section("ROUTE FINDER"));
        tb.add(toolBtn("🏠  Nearest Shelter",  new Color(25, 140, 70),  () -> mapPanel.showNearestShelterRoute()));
        tb.add(toolBtn("🚑  Nearest Team",      new Color(180, 80, 0),   () -> mapPanel.showNearestTeamRoute()));
        tb.add(toolBtn("✕  Clear Route",        new Color(120, 30, 30),  () -> mapPanel.clearRoute()));

        tb.add(Box.createVerticalGlue());

        JLabel note = new JLabel("<html><center>Drag to pan<br>Scroll to zoom<br>Click pin for info<br>Dbl-click to zoom in</center></html>");
        note.setFont(new Font("SansSerif", Font.PLAIN, 10));
        note.setForeground(new Color(100, 130, 180));
        note.setAlignmentX(Component.CENTER_ALIGNMENT);
        note.setBorder(BorderFactory.createEmptyBorder(0, 4, 10, 4));
        tb.add(note);

        return tb;
    }

    private JButton toolBtn(String text, Color accent, Runnable action) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(170, 36));
        b.setMinimumSize(new Dimension(170, 36));
        b.setPreferredSize(new Dimension(170, 36));
        b.setBackground(UITheme.BG_NAV);
        b.setForeground(new Color(190, 205, 235));
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(accent.darker());
                b.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(UITheme.BG_NAV);
                b.setForeground(new Color(190, 205, 235));
            }
        });
        b.addActionListener(e -> action.run());
        return b;
    }

    private JToggleButton toggleBtn(String text, boolean selected) {
        JToggleButton b = new JToggleButton(text, selected);
        b.setMaximumSize(new Dimension(170, 32));
        b.setPreferredSize(new Dimension(170, 32));
        b.setBackground(selected ? new Color(30, 60, 110) : UITheme.BG_NAV);
        b.setForeground(new Color(190, 205, 235));
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> {
            b.setBackground(b.isSelected() ? new Color(30, 60, 110) : UITheme.BG_NAV);
            updateLayerVisibility();
        });
        return b;
    }

    private JLabel section(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(100, 120, 170));
        l.setMaximumSize(new Dimension(170, 24));
        l.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Status bar ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(22, 36, 71));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(35, 55, 100)));
        bar.setPreferredSize(new Dimension(0, 26));

        statusLabel = new JLabel("  ● Loading map data...");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(25, 200, 120));

        JLabel right = new JLabel("Map data © OpenStreetMap contributors  ");
        right.setFont(new Font("SansSerif", Font.PLAIN, 10));
        right.setForeground(new Color(100, 130, 180));

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(right,       BorderLayout.EAST);
        return bar;
    }

    // ── Data loading ──────────────────────────────────────────────────────────
    private void loadAllData() {
        statusLabel.setText("  ⏳ Loading from database...");
        statusLabel.setForeground(new Color(255, 200, 50));

        SwingWorker<Void, String> w = new SwingWorker<>() {
            int vCount = 0, sCount = 0, tCount = 0;

            protected Void doInBackground() {
                mapPanel.clearAll();
                try {
                    List<Victim> vs = victimDAO.getAllVictims();
                    vCount = vs.size();
                    publish("Loading " + vCount + " victims...");
                    for (Victim v : vs) mapPanel.addVictim(v);
                } catch (Exception e) { publish("⚠ Victim load error: " + e.getMessage()); }

                try {
                    List<Shelter> ss = shelterDAO.getAllShelters();
                    sCount = ss.size();
                    publish("Loading " + sCount + " shelters...");
                    for (Shelter s : ss) mapPanel.addShelter(s);
                } catch (Exception e) { publish("⚠ Shelter load error: " + e.getMessage()); }

                try {
                    List<RescueTeam> ts = teamDAO.getAllTeams();
                    tCount = ts.size();
                    publish("Loading " + tCount + " teams...");
                    for (RescueTeam t : ts) mapPanel.addTeam(t);
                } catch (Exception e) { publish("⚠ Team load error: " + e.getMessage()); }

                return null;
            }

            protected void process(java.util.List<String> msgs) {
                if (!msgs.isEmpty()) statusLabel.setText("  ⏳ " + msgs.get(msgs.size()-1));
            }

            protected void done() {
                statusLabel.setText(String.format(
                        "  ● Live  |  %d Victims  •  %d Shelters  •  %d Teams  |  Simulation Active",
                        vCount, sCount, tCount));
                statusLabel.setForeground(new Color(25, 200, 120));
            }
        };
        w.execute();
    }

    private void updateLayerVisibility() {
        // The LiveMapPanel visibility is controlled via boolean flags
        // (extending this hook if needed — layers are always drawn in sample)
    }
}
