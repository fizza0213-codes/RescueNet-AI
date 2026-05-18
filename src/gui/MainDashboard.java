package gui;

import database.*;
import models.User;
import alerts.AlertManager;
import analytics.AnalyticsDashboard;
import map.MapFrame;
import realtime.RealTimeDataEngine;
import realtime.RealTimeDataEngine.DataSnapshot;
import realtime.RealTimeDataEngine.AlertEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainDashboard extends JFrame {

    private final User currentUser;
    private JLabel lblTime;
    private JLabel lblVictims, lblShelters, lblTeams, lblResources;
    private JLabel lblAlertBadge;
    private JLabel lblLastUpdated;
    private JPanel alertBannerPanel;
    private JLabel alertBannerText;
    private boolean alertBannerVisible = false;

    private final VictimDAO    victimDAO    = new VictimDAO();
    private final ShelterDAO   shelterDAO   = new ShelterDAO();
    private final RescueTeamDAO teamDAO     = new RescueTeamDAO();
    private final ResourceDAO  resourceDAO  = new ResourceDAO();

    // Real-time engine (singleton)
    private final RealTimeDataEngine rtEngine = RealTimeDataEngine.getInstance();
    private final AlertManager       alertMgr = AlertManager.getInstance();

    public MainDashboard(User user) {
        this.currentUser = user;
        setTitle("RescueNet AI — " + user.getRole() + " Dashboard");
        setSize(1200, 760);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 650));
        buildUI();
        wireRealTime();
        loadLiveStats();
        startClock();
        setVisible(true);

        // Register this frame as parent for toasts
        alertMgr.setParentFrame(this);

        // Start real-time engine
        rtEngine.start();
    }

    // ── UI assembly ───────────────────────────────────────────────────────────
    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(),    BorderLayout.NORTH);
        add(buildNavBar(),    BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(UITheme.BG_DARK);
        alertBannerPanel = buildAlertBanner();
        center.add(alertBannerPanel, BorderLayout.NORTH);
        center.add(buildHome(),      BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(UITheme.BG_HEADER);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, UITheme.ACCENT_RED));
        h.setPreferredSize(new Dimension(0, 58));

        // Left: logo + name
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        left.setOpaque(false);
        JLabel logo = new JLabel("🆘");
        logo.setFont(new Font("SansSerif", Font.PLAIN, 30));
        JLabel appName = new JLabel("RescueNet AI");
        appName.setFont(new Font("SansSerif", Font.BOLD, 20));
        appName.setForeground(Color.WHITE);
        JLabel tagline = new JLabel("Emergency Management System  ·  v2.0  LIVE");
        tagline.setFont(UITheme.FONT_SMALL);
        tagline.setForeground(new Color(160, 175, 210));
        left.add(logo); left.add(appName); left.add(tagline);

        // Right: last-updated, clock, alert bell, user, logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        right.setOpaque(false);

        lblLastUpdated = new JLabel("● LIVE");
        lblLastUpdated.setFont(UITheme.FONT_SMALL);
        lblLastUpdated.setForeground(UITheme.ACCENT_GREEN);

        lblTime = new JLabel();
        lblTime.setFont(UITheme.FONT_SMALL);
        lblTime.setForeground(new Color(160, 175, 210));

        // Alert bell with badge
        JButton btnAlerts = new JButton("🔔");
        btnAlerts.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btnAlerts.setBackground(UITheme.BG_HEADER);
        btnAlerts.setForeground(Color.WHITE);
        btnAlerts.setFocusPainted(false);
        btnAlerts.setBorderPainted(false);
        btnAlerts.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAlerts.setToolTipText("Open Alert Center");
        btnAlerts.addActionListener(e -> alertMgr.openAlertCenter());

        lblAlertBadge = new JLabel("0");
        lblAlertBadge.setFont(new Font("SansSerif", Font.BOLD, 9));
        lblAlertBadge.setForeground(Color.WHITE);
        lblAlertBadge.setBackground(UITheme.ACCENT_RED);
        lblAlertBadge.setOpaque(true);
        lblAlertBadge.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        lblAlertBadge.setVisible(false);

        JPanel bellPanel = new JPanel(null);
        bellPanel.setOpaque(false);
        bellPanel.setPreferredSize(new Dimension(40, 30));
        btnAlerts.setBounds(0, 2, 28, 26);
        lblAlertBadge.setBounds(16, 0, 22, 14);
        bellPanel.add(btnAlerts); bellPanel.add(lblAlertBadge);

        JLabel userLabel = new JLabel("👤  " + currentUser.getFullName() + "  ·  " + currentUser.getRole());
        userLabel.setFont(UITheme.FONT_BOLD);
        userLabel.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Sign Out");
        btnLogout.setBackground(new Color(194, 30, 30));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(UITheme.FONT_SMALL);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(140, 20, 20)),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            rtEngine.stop();
            dispose();
            new LoginFrame();
        });

        right.add(lblLastUpdated);
        right.add(lblTime);
        right.add(new JSeparator(JSeparator.VERTICAL));
        right.add(bellPanel);
        right.add(userLabel);
        right.add(btnLogout);

        h.add(left, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ── Left nav ──────────────────────────────────────────────────────────────
    private JPanel buildNavBar() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(UITheme.BG_NAV);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(35, 50, 90)));
        nav.setPreferredSize(new Dimension(215, 0));

        nav.add(Box.createVerticalStrut(16));
        nav.add(navSection("OVERVIEW"));
        nav.add(navBtn("  🏠  Dashboard",           UITheme.ACCENT_RED,    this::showHome));

        nav.add(Box.createVerticalStrut(4));
        nav.add(navSection("FIELD OPERATIONS"));
        nav.add(navBtn("  👥  Victim Registry",     UITheme.ACCENT_BLUE,   this::openVictims));
        nav.add(navBtn("  🏠  Shelter Management",  UITheme.ACCENT_ORANGE, this::openShelters));
        nav.add(navBtn("  🚑  Rescue Teams",         new Color(190, 40, 40), this::openTeams));
        nav.add(navBtn("  📦  Resources",            UITheme.ACCENT_PURPLE, this::openResources));

        nav.add(Box.createVerticalStrut(4));
        nav.add(navSection("INTELLIGENCE"));
        nav.add(navBtn("  🗺   Live Map",             new Color(14, 128, 140), this::openMap));       // NEW
        nav.add(navBtn("  📊  Analytics",             new Color(22, 130, 180),  this::openAnalytics)); // NEW
        nav.add(navBtn("  🔔  Alert Center",          UITheme.ACCENT_RED,    this::openAlertCenter)); // NEW

        nav.add(Box.createVerticalStrut(4));
        nav.add(navSection("SUPPORT"));
        nav.add(navBtn("  🤖  AI Assistant",          UITheme.ACCENT_GREEN,  this::openChatbot));
        nav.add(navBtn("  📋  Reports",               new Color(14,128,140), this::openReports));

        if (currentUser.getRole().equals("ADMIN")) {
            nav.add(Box.createVerticalStrut(4));
            nav.add(navSection("ADMINISTRATION"));
            nav.add(navBtn("  👤  User Management",  new Color(90, 90, 110), this::openUserMgmt));
        }

        nav.add(Box.createVerticalGlue());

        // Live status indicator
        JLabel dbLabel = new JLabel("  ● System Online  |  Live Mode", SwingConstants.LEFT);
        dbLabel.setFont(UITheme.FONT_SMALL);
        dbLabel.setForeground(UITheme.ACCENT_GREEN);
        dbLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dbLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 12, 0));
        nav.add(dbLabel);

        return nav;
    }

    private JButton navBtn(String text, Color accent, Runnable action) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(215, 42));
        b.setMinimumSize(new Dimension(215, 42));
        b.setPreferredSize(new Dimension(215, 42));
        b.setBackground(UITheme.BG_NAV);
        b.setForeground(new Color(190, 205, 235));
        b.setFont(UITheme.FONT_NORMAL);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(35, 55, 100)); b.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) {
                b.setBackground(UITheme.BG_NAV); b.setForeground(new Color(190, 205, 235)); }
        });
        b.addActionListener(e -> action.run());
        return b;
    }

    private JLabel navSection(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(100, 120, 170));
        l.setMaximumSize(new Dimension(215, 26));
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Alert banner ──────────────────────────────────────────────────────────
    private JPanel buildAlertBanner() {
        JPanel banner = new JPanel(new BorderLayout(10, 0));
        banner.setBackground(new Color(140, 20, 20));
        banner.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        alertBannerText = new JLabel("🔴  No active alerts");
        alertBannerText.setFont(UITheme.FONT_BOLD);
        alertBannerText.setForeground(Color.WHITE);

        JButton dismiss = new JButton("✕ Dismiss");
        dismiss.setBackground(new Color(120, 20, 20));
        dismiss.setForeground(Color.WHITE);
        dismiss.setFont(UITheme.FONT_SMALL);
        dismiss.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        dismiss.setFocusPainted(false);
        dismiss.addActionListener(e -> {
            alertBannerPanel.setVisible(false);
            alertBannerVisible = false;
        });

        banner.add(alertBannerText, BorderLayout.CENTER);
        banner.add(dismiss, BorderLayout.EAST);
        banner.setVisible(false); // hidden by default
        return banner;
    }

    // ── Home panel ────────────────────────────────────────────────────────────
    private JPanel buildHome() {
        JPanel home = new JPanel(new BorderLayout(0, 16));
        home.setBackground(UITheme.BG_DARK);
        home.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        // Welcome banner
        JPanel banner = new JPanel(new BorderLayout(0, 4));
        banner.setBackground(UITheme.BG_CARD);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.ACCENT_BLUE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        JLabel welcome = new JLabel("Welcome back, " + currentUser.getFullName());
        welcome.setFont(UITheme.FONT_HEADER); welcome.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Live statistics below  •  Map, Analytics & Alerts now active  •  Real-time monitoring enabled");
        sub.setFont(UITheme.FONT_SMALL); sub.setForeground(UITheme.TEXT_SECONDARY);
        banner.add(welcome, BorderLayout.NORTH);
        banner.add(sub,     BorderLayout.SOUTH);

        // Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        lblVictims   = new JLabel("—"); lblShelters = new JLabel("—");
        lblTeams     = new JLabel("—"); lblResources = new JLabel("—");
        stats.add(statCard("Total Victims Registered", lblVictims,   UITheme.ACCENT_BLUE));
        stats.add(statCard("Open Shelters",             lblShelters,  UITheme.ACCENT_GREEN));
        stats.add(statCard("Teams Available",           lblTeams,     UITheme.ACCENT_ORANGE));
        stats.add(statCard("Resource Items",            lblResources, UITheme.ACCENT_PURPLE));

        // Quick-action grid (2 rows × 4 cols)
        JPanel grid = new JPanel(new GridLayout(2, 4, 14, 14));
        grid.setOpaque(false);
        grid.add(moduleCard("👥", "Victim Registry",    "Register & track disaster victims",        UITheme.ACCENT_BLUE,    this::openVictims));
        grid.add(moduleCard("🗺",  "Live Map",           "Real-time victim & team map tracking",     new Color(14,128,140),  this::openMap));      // NEW
        grid.add(moduleCard("🏠", "Shelter Management", "Track shelter capacity & occupancy",        UITheme.ACCENT_ORANGE,  this::openShelters));
        grid.add(moduleCard("🚑", "Rescue Teams",       "Coordinate field rescue operations",        new Color(180,40,40),   this::openTeams));
        grid.add(moduleCard("📦", "Resource Inventory", "Manage supplies & equipment",               UITheme.ACCENT_PURPLE,  this::openResources));
        grid.add(moduleCard("📊", "Analytics",          "Advanced charts & disaster intelligence",   new Color(22,130,180),  this::openAnalytics)); // NEW
        grid.add(moduleCard("🔔", "Alert Center",       "Emergency SMS / email notifications",       UITheme.ACCENT_RED,     this::openAlertCenter)); // NEW
        grid.add(moduleCard("🤖", "AI Assistant",       "Get real-time disaster guidance",           UITheme.ACCENT_GREEN,   this::openChatbot));

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(stats, BorderLayout.NORTH);
        center.add(grid,  BorderLayout.CENTER);

        home.add(banner, BorderLayout.NORTH);
        home.add(center, BorderLayout.CENTER);
        return home;
    }

    private JPanel statCard(String label, JLabel valueLabel, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL); lbl.setForeground(UITheme.TEXT_SECONDARY);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 38));
        valueLabel.setForeground(accent);
        p.add(lbl, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private JButton moduleCard(String icon, String title, String desc, Color accent, Runnable action) {
        JButton b = new JButton();
        b.setLayout(new BorderLayout(0, 4));
        b.setBackground(UITheme.BG_CARD);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        JLabel iconLbl = new JLabel(icon + "  ");
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 20));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_BOLD);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);
        top.add(iconLbl); top.add(titleLbl);

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(UITheme.FONT_SMALL);
        descLbl.setForeground(UITheme.TEXT_SECONDARY);

        JLabel arrow = new JLabel("→");
        arrow.setFont(UITheme.FONT_BOLD);
        arrow.setForeground(accent);

        b.add(top,     BorderLayout.NORTH);
        b.add(descLbl, BorderLayout.CENTER);
        b.add(arrow,   BorderLayout.SOUTH);

        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(
                        Math.min(accent.getRed()/8 + 245, 255),
                        Math.min(accent.getGreen()/8 + 245, 255),
                        Math.min(accent.getBlue()/8 + 245, 255)));
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accent),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16)));
            }
            public void mouseExited(MouseEvent e) {
                b.setBackground(UITheme.BG_CARD);
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16)));
            }
        });
        b.addActionListener(e -> action.run());
        return b;
    }

    // ── Status bar ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(22, 36, 71));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(35, 55, 100)));
        bar.setPreferredSize(new Dimension(0, 28));

        JLabel left = new JLabel("  ● RescueNet AI v2.0  |  " + database.DBConnection.getStatusText()
                + "  |  Real-time Engine: ACTIVE");
        left.setFont(UITheme.FONT_SMALL);
        left.setForeground(UITheme.ACCENT_GREEN);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        rightPanel.setOpaque(false);

        JButton notifLog = new JButton("📋 Notification Log");
        notifLog.setFont(UITheme.FONT_SMALL);
        notifLog.setForeground(new Color(140, 170, 220));
        notifLog.setBackground(new Color(22, 36, 71));
        notifLog.setBorderPainted(false);
        notifLog.setFocusPainted(false);
        notifLog.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        notifLog.addActionListener(e ->
                alerts.AlertManager.NotificationSimulator.showLogDialog(this));

        JLabel right = new JLabel("Emergency: Rescue 1122  |  Police 15  |  Ambulance 115  |  NDMA 051-9205436  ");
        right.setFont(UITheme.FONT_SMALL);
        right.setForeground(new Color(140, 160, 200));

        rightPanel.add(notifLog);
        rightPanel.add(right);

        bar.add(left,  BorderLayout.WEST);
        bar.add(rightPanel, BorderLayout.EAST);
        return bar;
    }

    // ── Real-time wiring ──────────────────────────────────────────────────────
    private void wireRealTime() {
        // Subscribe to data snapshots → update stat cards
        rtEngine.addDataListener((DataSnapshot snap) -> {
            lblVictims.setText(String.valueOf(snap.victims));
            lblShelters.setText(String.valueOf(snap.shelters));
            lblTeams.setText(String.valueOf(snap.teams));
            lblResources.setText(String.valueOf(snap.resources));
            lblLastUpdated.setText("● Updated " + snap.timestamp);
        });

        // Subscribe to alerts → show toast + update badge
        rtEngine.addAlertListener((AlertEvent event) -> {
            alertMgr.receiveAlert(event);
            // Update alert badge count
            int unread = alertMgr.getUnreadCount();
            lblAlertBadge.setText(String.valueOf(unread));
            lblAlertBadge.setVisible(unread > 0);

            // Show inline banner for CRITICAL
            if (event.severity == AlertEvent.Severity.CRITICAL) {
                alertBannerText.setText("🔴  CRITICAL: " + event.message
                        + "  [" + event.timestamp + "]");
                alertBannerPanel.setVisible(true);
                alertBannerVisible = true;
            }
        });

        // Badge listener for manual alert.markAllRead()
        alertMgr.addBadgeListener(() -> {
            int unread = alertMgr.getUnreadCount();
            lblAlertBadge.setText(String.valueOf(unread));
            lblAlertBadge.setVisible(unread > 0);
        });
    }

    // ── DB stats loader (initial load) ────────────────────────────────────────
    private void loadLiveStats() {
        SwingWorker<int[], Void> w = new SwingWorker<>() {
            protected int[] doInBackground() {
                try {
                    return new int[]{
                            victimDAO.getTotalCount(), shelterDAO.getOpenCount(),
                            teamDAO.getAvailableCount(), resourceDAO.getTotalItems()};
                } catch (Exception e) { return new int[]{0,0,0,0}; }
            }
            protected void done() {
                try {
                    int[] s = get();
                    lblVictims.setText(String.valueOf(s[0]));
                    lblShelters.setText(String.valueOf(s[1]));
                    lblTeams.setText(String.valueOf(s[2]));
                    lblResources.setText(String.valueOf(s[3]));
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void startClock() {
        Timer t = new Timer(1000, e -> lblTime.setText(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm:ss"))));
        t.start();
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    private void showHome()           { loadLiveStats(); }
    private void openVictims()        { new VictimManagementFrame(currentUser); }
    private void openChatbot()        { new ChatbotFrame(currentUser); }
    private void openShelters()       { new ShelterManagementFrame(currentUser); }
    private void openTeams()          { new RescueTeamFrame(currentUser); }
    private void openResources()      { new ResourceFrame(currentUser); }
    private void openReports()        { new ReportsFrame(currentUser); }
    private void openUserMgmt()       { new UserManagementFrame(currentUser); }

    // ── NEW actions ───────────────────────────────────────────────────────────
    private void openMap()            { new MapFrame(); }
    private void openAnalytics()      { new AnalyticsDashboard(); }
    private void openAlertCenter()    { alertMgr.openAlertCenter(); }
}
