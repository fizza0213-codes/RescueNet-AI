package database;

import models.RescueTeam;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RescueTeamDAO {

    public void addTeam(RescueTeam t) throws SQLException {
        String sql = "INSERT INTO rescue_teams(team_name,leader,leader_phone,members,vehicle,vehicle_no,specialization,status,current_location) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getTeamName());
            ps.setString(2, t.getLeader());
            ps.setString(3, t.getLeaderPhone());
            ps.setInt(4, t.getMembers());
            ps.setString(5, t.getVehicle());
            ps.setString(6, t.getVehicleNo());
            ps.setString(7, t.getSpecialization());
            ps.setString(8, t.getStatus());
            ps.setString(9, t.getCurrentLocation());
            ps.executeUpdate();
        }
    }

    public void updateTeam(RescueTeam t) throws SQLException {
        String sql = "UPDATE rescue_teams SET team_name=?,leader=?,leader_phone=?,members=?,vehicle=?,vehicle_no=?,specialization=?,status=?,current_location=? WHERE team_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getTeamName());
            ps.setString(2, t.getLeader());
            ps.setString(3, t.getLeaderPhone());
            ps.setInt(4, t.getMembers());
            ps.setString(5, t.getVehicle());
            ps.setString(6, t.getVehicleNo());
            ps.setString(7, t.getSpecialization());
            ps.setString(8, t.getStatus());
            ps.setString(9, t.getCurrentLocation());
            ps.setInt(10, t.getTeamId());
            ps.executeUpdate();
        }
    }

    public void deleteTeam(int id) throws SQLException {
        String sql = "DELETE FROM rescue_teams WHERE team_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<RescueTeam> getAllTeams() throws SQLException {
        List<RescueTeam> list = new ArrayList<>();
        String sql = "SELECT * FROM rescue_teams ORDER BY team_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public int getAvailableCount() throws SQLException {
        ResultSet rs = DBConnection.getConnection().createStatement()
                .executeQuery("SELECT COUNT(*) FROM rescue_teams WHERE status='AVAILABLE'");
        return rs.next() ? rs.getInt(1) : 0;
    }

    private RescueTeam mapRow(ResultSet rs) throws SQLException {
        return new RescueTeam(
                rs.getInt("team_id"),
                rs.getString("team_name"),
                rs.getString("leader") != null ? rs.getString("leader") : "",
                rs.getString("leader_phone") != null ? rs.getString("leader_phone") : "",
                rs.getInt("members"),
                rs.getString("vehicle") != null ? rs.getString("vehicle") : "",
                rs.getString("vehicle_no") != null ? rs.getString("vehicle_no") : "",
                rs.getString("specialization") != null ? rs.getString("specialization") : "",
                rs.getString("status"),
                rs.getString("current_location") != null ? rs.getString("current_location") : ""
        );
    }
}
