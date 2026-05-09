package gui;

import database.UserDAO;
import models.User;
import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JDialog {

    private JTextField txtUsername, txtFullName, txtEmail, txtPhone;
    private JPasswordField txtPassword, txtConfirm;
    private JComboBox<String> cmbRole;
    private final UserDAO userDAO = new UserDAO();

    public RegisterFrame(JFrame parent) {
        super(parent, "Register New Account", true);
        setSize(480, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(null);

        JLabel title = new JLabel("📝  Create New Account", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT_BLUE);
        title.setBounds(20, 15, 440, 35);
        add(title);

        JPanel form = new JPanel(null);
        form.setBackground(UITheme.BG_CARD);
        form.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        form.setBounds(25, 58, 430, 370);
        add(form);

        // Fields
        int y = 15;
        JLabel lFull = lbl("Full Name *"); lFull.setBounds(15, y+5, 130, 22); form.add(lFull);
        txtFullName = UITheme.styledField(); txtFullName.setBounds(150, y, 265, 32); form.add(txtFullName);

        y += 45;
        JLabel lUser = lbl("Username *"); lUser.setBounds(15, y+5, 130, 22); form.add(lUser);
        txtUsername = UITheme.styledField(); txtUsername.setBounds(150, y, 265, 32); form.add(txtUsername);

        y += 45;
        JLabel lPass = lbl("Password *"); lPass.setBounds(15, y+5, 130, 22); form.add(lPass);
        txtPassword = UITheme.styledPassField(); txtPassword.setBounds(150, y, 265, 32); form.add(txtPassword);

        y += 45;
        JLabel lConf = lbl("Confirm Password *"); lConf.setBounds(15, y+5, 130, 22); form.add(lConf);
        txtConfirm = UITheme.styledPassField(); txtConfirm.setBounds(150, y, 265, 32); form.add(txtConfirm);

        y += 45;
        JLabel lEmail = lbl("Email"); lEmail.setBounds(15, y+5, 130, 22); form.add(lEmail);
        txtEmail = UITheme.styledField(); txtEmail.setBounds(150, y, 265, 32); form.add(txtEmail);

        y += 45;
        JLabel lPhone = lbl("Phone"); lPhone.setBounds(15, y+5, 130, 22); form.add(lPhone);
        txtPhone = UITheme.styledField(); txtPhone.setBounds(150, y, 265, 32); form.add(txtPhone);

        y += 45;
        JLabel lRole = lbl("Register as"); lRole.setBounds(15, y+5, 130, 22); form.add(lRole);
        cmbRole = UITheme.styledCombo(new String[]{"CITIZEN", "OFFICER"});
        cmbRole.setBounds(150, y, 265, 32); form.add(cmbRole);

        JLabel note = new JLabel("* Admin accounts must be created by existing admin");
        note.setFont(UITheme.FONT_SMALL);
        note.setForeground(UITheme.TEXT_SECONDARY);
        note.setBounds(15, y + 38, 400, 18);
        form.add(note);

        // Buttons
        JButton btnRegister = UITheme.primaryButton("✅  Create Account", UITheme.ACCENT_GREEN);
        btnRegister.setBounds(25, 436, 200, 40);
        btnRegister.addActionListener(e -> doRegister());
        add(btnRegister);

        JButton btnCancel = UITheme.primaryButton("Cancel", UITheme.TEXT_SECONDARY);
        btnCancel.setBounds(255, 436, 200, 40);
        btnCancel.addActionListener(e -> dispose());
        add(btnCancel);
    }

    private void doRegister() {
        String fullName  = txtFullName.getText().trim();
        String username  = txtUsername.getText().trim();
        String password  = new String(txtPassword.getPassword()).trim();
        String confirm   = new String(txtConfirm.getPassword()).trim();
        String email     = txtEmail.getText().trim();
        String phone     = txtPhone.getText().trim();
        String role      = (String) cmbRole.getSelectedItem();

        // Validation
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name, Username and Password are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this, "Username must be at least 3 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User newUser = new User(username, password, fullName, email, phone, role);
            boolean success = userDAO.registerUser(newUser);

            if (success) {
                JOptionPane.showMessageDialog(this,
                    "✅ Account created successfully!\n\n" +
                    "Username: " + username + "\nRole: " + role + "\n\nYou can now login.",
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "❌ Username '" + username + "' is already taken.\nPlease choose a different username.",
                    "Username Taken", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + ex.getMessage() + "\n\nEnsure MySQL is running and database is set up.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_NORMAL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }
}
