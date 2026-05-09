package gui;

import database.*;
import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainDashboard extends JFrame {

    private final User currentUser;
    private JLabel lblTime;
    private JLabel lblVictims, lblShelters, lblTeams, lblResources;

    private final VictimDAO    victimDAO    = new VictimDAO();
    private final ShelterDAO   shelterDAO   = new ShelterDAO();
    private final RescueTeamDAO teamDAO     = new RescueTeamDAO();
    private final ResourceDAO  resourceDAO  = new ResourceDAO();

    public MainDashboard(User user) {
        this.currentUser = user;
        setTitle("RescueNet AI — " + user.getRole() + " Dashboard");
        setSize(1150, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(960, 620));
        buildUI();
        loadLiveStats();
        startClock();
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildNavBar(), BorderLayout.WEST);
        add(buildHome(),   BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────
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
        JLabel tagline = new JLabel("Emergency Management System");
        tagline.setFont(UITheme.FONT_SMALL);
        tagline.setForeground(new Color(160, 175, 210));
        left.add(logo); left.add(appName); left.add(tagline);

        // Right: clock + user + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        right.setOpaque(false);
        lblTime = new JLabel();
        lblTime.setFont(UITheme.FONT_SMALL);
        lblTime.setForeground(new Color(160, 175, 210));

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
            BorderFactory.createEmptyBorder(5, 14, 5, 14)
        ));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> { dispose(); new LoginFrame(); });

        right.add(lblTime);
        right.add(new JSeparator(JSeparator.VERTICAL));
        right.add(userLabel);
        right.add(btnLogout);

        h.add(left, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ── Left nav ──────────────────────────────────────────────
    private JPanel buildNavBar() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(UITheme.BG_NAV);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(35, 50, 90)));
        nav.setPreferredSize(new Dimension(210, 0));

        nav.add(Box.createVerticalStrut(16));
        nav.add(navSection("OVERVIEW"));
        nav.add(navBtn("  🏠  Dashboard",         UITheme.ACCENT_RED,    this::showHome));
        nav.add(Box.createVerticalStrut(4));
        nav.add(navSection("FIELD OPERATIONS"));
        nav.add(navBtn("  👥  Victim Registry",    UITheme.ACCENT_BLUE,   this::openVictims));
        nav.add(navBtn("  🏠  Shelter Management", UITheme.ACCENT_ORANGE, this::openShelters));
        nav.add(navBtn("  🚑  Rescue Teams",       new Color(190, 40, 40), this::openTeams));
        nav.add(navBtn("  📦  Resources",          UITheme.ACCENT_PURPLE, this::openResources));
        nav.add(Box.createVerticalStrut(4));
        nav.add(navSection("SUPPORT"));
        nav.add(navBtn("  🤖  AI Assistant",       UITheme.ACCENT_GREEN,  this::openChatbot));
        nav.add(navBtn("  📊  Reports",            new Color(14,128,140), this::openReports));
        if (currentUser.getRole().equals("ADMIN")) {
            nav.add(Box.createVerticalStrut(4));
            nav.add(navSection("ADMINISTRATION"));
            nav.add(navBtn("  👤  User Management", new Color(90, 90, 110), this::openUserMgmt));
        }
        nav.add(Box.createVerticalGlue());

        JLabel dbLabel = new JLabel("  ● System Online", SwingConstants.LEFT);
        dbLabel.setFont(UITheme.FONT_SMALL);
        dbLabel.setForeground(UITheme.ACCENT_GREEN);
        dbLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dbLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 12, 0));
        nav.add(dbLabel);

        return nav;
    }

    private JButton navBtn(String text, Color accent, Runnable action) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(210, 42));
        b.setMinimumSize(new Dimension(210, 42));
        b.setPreferredSize(new Dimension(210, 42));
        b.setBackground(UITheme.BG_NAV);
        b.setForeground(new Color(190, 205, 235));
        b.setFont(UITheme.FONT_NORMAL);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(35, 55, 100));
                b.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                b.setBackground(UITheme.BG_NAV);
                b.setForeground(new Color(190, 205, 235));
            }
        });
        b.addActionListener(e -> action.run());
        return b;
    }

    private JLabel navSection(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(100, 120, 170));
        l.setMaximumSize(new Dimension(210, 26));
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ── Home dashboard ────────────────────────────────────────
    private JPanel buildHome() {
        JPanel home = new JPanel(new BorderLayout(0, 16));
        home.setBackground(UITheme.BG_DARK);
        home.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        // Welcome banner
        JPanel banner = new JPanel(new BorderLayout(0, 4));
        banner.setBackground(UITheme.BG_CARD);
        banner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.ACCENT_BLUE),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        JLabel welcome = new JLabel("Welcome back, " + currentUser.getFullName());
        welcome.setFont(UITheme.FONT_HEADER);
        welcome.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Use the left navigation to access any module. Live statistics are shown below.");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        banner.add(welcome, BorderLayout.NORTH);
        banner.add(sub, BorderLayout.SOUTH);

        // Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        lblVictims   = new JLabel("—"); lblShelters = new JLabel("—");
        lblTeams     = new JLabel("—"); lblResources = new JLabel("—");
        stats.add(statCard("Total Victims Registered", lblVictims,   UITheme.ACCENT_BLUE));
        stats.add(statCard("Open Shelters",             lblShelters,  UITheme.ACCENT_GREEN));
        stats.add(statCard("Teams Available",           lblTeams,     UITheme.ACCENT_ORANGE));
        stats.add(statCard("Resource Items",            lblResources, UITheme.ACCENT_PURPLE));

        // Quick-action grid
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);
        grid.add(moduleCard("👥", "Victim Registry",     "Register and track disaster victims",      UITheme.ACCENT_BLUE,   this::openVictims));
        grid.add(moduleCard("🤖", "AI Assistant",        "Get real-time disaster guidance",          UITheme.ACCENT_GREEN,  this::openChatbot));
        grid.add(moduleCard("🏠", "Shelter Management",  "Track shelter capacity and occupancy",     UITheme.ACCENT_ORANGE, this::openShelters));
        grid.add(moduleCard("🚑", "Rescue Teams",        "Coordinate field rescue operations",       new Color(180,40,40),  this::openTeams));
        grid.add(moduleCard("📦", "Resource Inventory",  "Manage supplies and equipment",            UITheme.ACCENT_PURPLE, this::openResources));
        grid.add(moduleCard("📊", "Reports & Analytics", "View statistics and generate reports",     new Color(14,128,140), this::openReports));

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
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
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
            BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        JLabel iconLbl = new JLabel(icon + "  ");
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 22));
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
                    Math.min(accent.getRed()/8   + 245, 255),
                    Math.min(accent.getGreen()/8 + 245, 255),
                    Math.min(accent.getBlue()/8  + 245, 255)));
                b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accent),
                    BorderFactory.createEmptyBorder(16, 18, 16, 18)));
            }
            public void mouseExited(MouseEvent e) {
                b.setBackground(UITheme.BG_CARD);
                b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(16, 18, 16, 18)));
            }
        });
        b.addActionListener(e -> action.run());
        return b;
    }

    // ── Status bar ────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(22, 36, 71));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(35, 55, 100)));
        bar.setPreferredSize(new Dimension(0, 28));

        JLabel left = new JLabel("  ● RescueNet AI  |  " + DBConnection.getStatusText());
        left.setFont(UITheme.FONT_SMALL);
        left.setForeground(UITheme.ACCENT_GREEN);

        JLabel right = new JLabel("Emergency: Rescue 1122  |  Police 15  |  Ambulance 115  |  NDMA 051-9205436  ");
        right.setFont(UITheme.FONT_SMALL);
        right.setForeground(new Color(140, 160, 200));

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Stats loader ──────────────────────────────────────────
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

    // ── Navigation ────────────────────────────────────────────
    private void showHome()       { /* already home */ loadLiveStats(); }
    private void openVictims()    { new VictimManagementFrame(currentUser); }
    private void openChatbot()    { new ChatbotFrame(currentUser); }
    private void openShelters()   { new ShelterManagementFrame(currentUser); }
    private void openTeams()      { new RescueTeamFrame(currentUser); }
    private void openResources()  { new ResourceFrame(currentUser); }
    private void openReports()    { new ReportsFrame(currentUser); }
    private void openUserMgmt()   { new UserManagementFrame(currentUser); }
}
