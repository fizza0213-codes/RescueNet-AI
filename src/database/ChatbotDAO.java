package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatbotDAO {

    public void saveChat(Integer userId, String sessionId, String query, String response, String type) {
        try {
            String sql = "INSERT INTO chatbot_history(user_id,session_id,user_query,ai_response,response_type) VALUES(?,?,?,?,?)";
            PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
            if (userId != null) ps.setInt(1, userId); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, sessionId);
            ps.setString(3, query);
            ps.setString(4, response);
            ps.setString(5, type);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public List<String[]> getChatHistory(Integer userId, int limit) throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql;
        PreparedStatement ps;
        if (userId != null) {
            sql = "SELECT user_query, ai_response, response_type, timestamp FROM chatbot_history WHERE user_id=? ORDER BY timestamp DESC LIMIT ?";
            ps = DBConnection.getConnection().prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, limit);
        } else {
            sql = "SELECT user_query, ai_response, response_type, timestamp FROM chatbot_history ORDER BY timestamp DESC LIMIT ?";
            ps = DBConnection.getConnection().prepareStatement(sql);
            ps.setInt(1, limit);
        }
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new String[]{
                rs.getString("user_query"),
                rs.getString("ai_response"),
                rs.getString("response_type"),
                rs.getTimestamp("timestamp").toString()
            });
        }
        return list;
    }
}
