package gui;

import database.UserDAO;
import models.User;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class UserManagementFrame extends JFrame {

    private final User currentUser;
    private final UserDAO dao = new UserDAO();
    private DefaultTableModel tableModel;
    private JLabel lblStats;

    private static final String[] COLS = {"ID","Username","Full Name","Email","Phone","Role","Last Login","Created At"};

    public UserManagementFrame(User user) {
        this.currentUser = user;
        setTitle("User Management — Admin Panel");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI(); loadData(); setVisible(true);
    }

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UITheme.TEXT_SECONDARY));
        header.setPreferredSize(new Dimension(0, 50));
        JLabel title = new JLabel("  👤  User Management (Admin Only)");
        title.setFont(UITheme.FONT_HEADER); title.setForeground(UITheme.TEXT_WHITE);
        JButton back = UITheme.primaryButton("← Back", new Color(70, 80, 110));
        back.setFont(UITheme.FONT_SMALL); back.addActionListener(e -> dispose());
        back.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        header.add(title, BorderLayout.WEST); header.add(back, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        UITheme.styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        sp.getViewport().setBackground(UITheme.BG_CARD);
        sp.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(sp, BorderLayout.CENTER);

        lblStats = new JLabel("  Loading...");
        lblStats.setFont(UITheme.FONT_SMALL);
        lblStats.setForeground(UITheme.TEXT_SECONDARY);
        lblStats.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 0));
        add(lblStats, BorderLayout.SOUTH);
    }

    private void loadData() {
        SwingWorker<List<User>, Void> w = new SwingWorker<>() {
            protected List<User> doInBackground() throws Exception { return dao.getAllUsers(); }
            protected void done() {
                try {
                    List<User> list = get(); tableModel.setRowCount(0);
                    for (User u : list) {
                        tableModel.addRow(new Object[]{
                            u.getUserId(), u.getUsername(), u.getFullName(),
                            u.getEmail(), u.getPhone(), u.getRole(),
                            u.getLastLogin() != null ? u.getLastLogin().toString().substring(0,16) : "Never",
                            u.getCreatedAt() != null ? u.getCreatedAt().toString().substring(0,16) : "—"
                        });
                    }
                    lblStats.setText("  Total Users: " + list.size());
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }
}
