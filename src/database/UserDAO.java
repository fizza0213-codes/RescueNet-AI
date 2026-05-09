package database;

import models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /** Register a new user. Returns true if successful, false if username taken. */
    public boolean registerUser(User u) throws SQLException {
        // Check if username already exists
        String check = "SELECT COUNT(*) FROM users WHERE username=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(check)) {
            ps.setString(1, u.getUsername());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return false; // username taken
        }

        String sql = "INSERT INTO users(username,password,full_name,email,phone,role) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getRole());
            ps.executeUpdate();
        }
        return true;
    }

    /** Authenticate user. Returns User object if valid, null otherwise. */
    public User login(String username, String password, String role) throws SQLException {
        String sql = "SELECT * FROM users WHERE username=? AND password=? AND role=? AND active=TRUE";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = mapRow(rs);
                // Update last login timestamp
                updateLastLogin(u.getUserId());
                return u;
            }
        }
        return null;
    }

    private void updateLastLogin(int userId) {
        try {
            String sql = "UPDATE users SET last_login=NOW() WHERE user_id=?";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public int getTotalUsers() throws SQLException {
        ResultSet rs = DBConnection.getConnection().createStatement()
                .executeQuery("SELECT COUNT(*) FROM users");
        return rs.next() ? rs.getInt(1) : 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("full_name") != null ? rs.getString("full_name") : "",
                rs.getString("email") != null ? rs.getString("email") : "",
                rs.getString("phone") != null ? rs.getString("phone") : "",
                rs.getString("role")
        );
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) u.setLastLogin(lastLogin);
        return u;
    }
}
