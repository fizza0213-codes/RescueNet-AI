package database;

import java.sql.*;

public class DBConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/rescuenet_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = ""; // ← Change to your MySQL password if set

    private static Connection conn = null;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL Driver not found.");
            }
        }
        return conn;
    }

    public static boolean isConnected() {
        try { return conn != null && !conn.isClosed() && conn.isValid(2); }
        catch (SQLException e) { return false; }
    }

    public static String getStatusText() {
        return isConnected() ? "✅ MySQL Connected" : "⚠️ DB Offline";
    }

    public static void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }

    public static boolean testConnection() {
        try { getConnection(); return isConnected(); }
        catch (Exception e) { return false; }
    }
}
