package database;

import models.Victim;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VictimDAO {

    public void addVictim(Victim v) throws SQLException {
        String sql = "INSERT INTO victims(name,cnic,age,gender,disaster_type,severity_level,status,location,contact,next_of_kin,notes,registered_by) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getName());
            ps.setString(2, v.getCnic());
            ps.setInt(3, v.getAge());
            ps.setString(4, v.getGender());
            ps.setString(5, v.getDisasterType());
            ps.setInt(6, v.getSeverityLevel());
            ps.setString(7, v.getStatus());
            ps.setString(8, v.getLocation());
            ps.setString(9, v.getContact());
            ps.setString(10, v.getNextOfKin());
            ps.setString(11, v.getNotes());
            ps.setObject(12, v.getRegisteredBy(), Types.INTEGER);
            ps.executeUpdate();
            // Get auto-generated ID and set it back
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) v.setVictimId(keys.getInt(1));
        }
    }

    public void updateVictim(Victim v) throws SQLException {
        String sql = "UPDATE victims SET name=?,cnic=?,age=?,gender=?,disaster_type=?,severity_level=?,status=?,location=?,contact=?,next_of_kin=?,notes=? WHERE victim_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, v.getName());
            ps.setString(2, v.getCnic());
            ps.setInt(3, v.getAge());
            ps.setString(4, v.getGender());
            ps.setString(5, v.getDisasterType());
            ps.setInt(6, v.getSeverityLevel());
            ps.setString(7, v.getStatus());
            ps.setString(8, v.getLocation());
            ps.setString(9, v.getContact());
            ps.setString(10, v.getNextOfKin());
            ps.setString(11, v.getNotes());
            ps.setInt(12, v.getVictimId());
            ps.executeUpdate();
        }
    }

    public List<Victim> getAllVictims() throws SQLException {
        List<Victim> list = new ArrayList<>();
        String sql = "SELECT * FROM victims ORDER BY severity_level DESC, registered_at DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Victim getVictimById(int id) throws SQLException {
        String sql = "SELECT * FROM victims WHERE victim_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public void deleteVictim(int id) throws SQLException {
        String sql = "DELETE FROM victims WHERE victim_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE victims SET status=? WHERE victim_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public int getTotalCount() throws SQLException {
        ResultSet rs = DBConnection.getConnection().createStatement()
                .executeQuery("SELECT COUNT(*) FROM victims");
        return rs.next() ? rs.getInt(1) : 0;
    }

    public int getCountByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM victims WHERE status=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<Victim> searchByName(String keyword) throws SQLException {
        List<Victim> list = new ArrayList<>();
        String sql = "SELECT * FROM victims WHERE name LIKE ? ORDER BY severity_level DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Returns victim history with timestamps for display */
    public List<Victim> getRecentVictims(int limit) throws SQLException {
        List<Victim> list = new ArrayList<>();
        String sql = "SELECT * FROM victims ORDER BY registered_at DESC LIMIT ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Victim mapRow(ResultSet rs) throws SQLException {
        Victim v = new Victim(
                rs.getInt("victim_id"),
                rs.getString("name"),
                rs.getString("cnic") != null ? rs.getString("cnic") : "",
                rs.getInt("age"),
                rs.getString("gender") != null ? rs.getString("gender") : "Unknown",
                rs.getString("disaster_type") != null ? rs.getString("disaster_type") : "",
                rs.getInt("severity_level"),
                rs.getString("status") != null ? rs.getString("status") : "ACTIVE",
                rs.getString("location") != null ? rs.getString("location") : "",
                rs.getString("contact") != null ? rs.getString("contact") : "",
                rs.getString("next_of_kin") != null ? rs.getString("next_of_kin") : "",
                rs.getString("notes") != null ? rs.getString("notes") : ""
        );
        Timestamp ts = rs.getTimestamp("registered_at");
        if (ts != null) v.setRegisteredAt(ts);
        Object registeredBy = rs.getObject("registered_by");
        if (registeredBy != null) v.setRegisteredBy((Integer) registeredBy);
        return v;
    }
}
