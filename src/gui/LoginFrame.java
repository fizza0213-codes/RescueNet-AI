package gui;

import database.DBConnection;
import database.UserDAO;
import models.User;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField    txtUser;
    private JPasswordField txtPass;
    private JComboBox<String> cmbRole;
    private JLabel        lblStatus;
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("RescueNet AI — Login");
        setSize(480, 580);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
        checkDBStatus();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        setContentPane(root);

        // ── Left accent bar ─────────────────────────────────
        JPanel accent = new JPanel();
        accent.setBackground(UITheme.ACCENT_RED);
        accent.setPreferredSize(new Dimension(6, 0));
        root.add(accent, BorderLayout.WEST);

        // ── Main content ─────────────────────────────────────
        JPanel main = new JPanel(null);
        main.setBackground(UITheme.BG_DARK);
        root.add(main, BorderLayout.CENTER);

        int cx = 60, w = 340;

        // Logo
        JLabel logo = new JLabel("🆘", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.PLAIN, 52));
        logo.setBounds(cx + 110, 30, 120, 65);
        main.add(logo);

        // App name
        JLabel appName = new JLabel("RescueNet AI", SwingConstants.CENTER);
        appName.setFont(new Font("SansSerif", Font.BOLD, 24));
        appName.setForeground(UITheme.ACCENT_RED);
        appName.setBounds(cx, 95, w, 34);
        main.add(appName);

        JLabel sub = new JLabel("Disaster Emergency Management System", SwingConstants.CENTER);
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_SECONDARY);
        sub.setBounds(cx, 130, w, 20);
        main.add(sub);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER_COLOR);
        sep.setBounds(cx, 162, w, 2);
        main.add(sep);

        // Form card
        JPanel card = new JPanel(null);
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        card.setBounds(cx, 175, w, 270);
        main.add(card);

        // Card header
        JPanel cardHdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        cardHdr.setBackground(UITheme.BG_NAV);
        cardHdr.setBounds(0, 0, w, 42);
        card.add(cardHdr);
        JLabel signIn = new JLabel("Sign In to Your Account");
        signIn.setFont(UITheme.FONT_BOLD); signIn.setForeground(UITheme.TEXT_WHITE);
        cardHdr.add(signIn);

        // Username
        JLabel lUser = lbl("Username");
        lUser.setBounds(16, 58, 100, 22); card.add(lUser);
        txtUser = UITheme.styledField();
        txtUser.setBounds(16, 80, w - 32, 36); card.add(txtUser);

        // Password
        JLabel lPass = lbl("Password");
        lPass.setBounds(16, 125, 100, 22); card.add(lPass);
        txtPass = UITheme.styledPassField();
        txtPass.setBounds(16, 147, w - 32, 36); card.add(txtPass);
        txtPass.addActionListener(e -> doLogin());

        // Role
        JLabel lRole = lbl("Role");
        lRole.setBounds(16, 192, 100, 22); card.add(lRole);
        cmbRole = UITheme.styledCombo(new String[]{"ADMIN", "OFFICER", "CITIZEN"});
        cmbRole.setBounds(16, 214, w - 32, 36); card.add(cmbRole);

        // Buttons
        JButton btnLogin = UITheme.primaryButton("Sign In", UITheme.ACCENT_RED);
        btnLogin.setBounds(cx, 460, (w / 2) - 6, 44);
        btnLogin.setFont(UITheme.FONT_BOLD);
        btnLogin.addActionListener(e -> doLogin());
        main.add(btnLogin);

        JButton btnReg = UITheme.primaryButton("Create Account", UITheme.ACCENT_BLUE);
        btnReg.setBounds(cx + (w / 2) + 6, 460, (w / 2) - 6, 44);
        btnReg.setFont(UITheme.FONT_BOLD);
        btnReg.addActionListener(e -> new RegisterFrame(this));
        main.add(btnReg);

        // DB status
        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setBounds(cx, 516, w, 20);
        main.add(lblStatus);
    }

    private void checkDBStatus() {
        boolean ok = DBConnection.testConnection();
        lblStatus.setText(ok
            ? "✓ Database connected"
            : "⚠ Database offline — please setup MySQL");
        lblStatus.setForeground(ok ? UITheme.ACCENT_GREEN : UITheme.ACCENT_ORANGE);
    }

    private void doLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword()).trim();
        String role     = (String) cmbRole.getSelectedItem();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter your username and password.", "Required Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            User user = userDAO.login(username, password, role);
            if (user != null) {
                dispose();
                new MainDashboard(user);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid credentials. Please check your username, password and role.\n\n" +
                    "Default accounts:\n  admin / admin123  (ADMIN)\n  officer1 / pass123  (OFFICER)\n  citizen1 / pass123  (CITIZEN)",
                    "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Cannot connect to the database.\nPlease ensure MySQL is running and rescuenet_db.sql has been imported.\n\nError: " + ex.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }
}
