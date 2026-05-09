package database;

import models.Shelter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShelterDAO {

    public void addShelter(Shelter s) throws SQLException {
        String sql = "INSERT INTO shelters(name,location,city,capacity,occupied,status,contact,in_charge,has_medical,has_food,has_water,notes) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getLocation());
            ps.setString(3, s.getCity());
            ps.setInt(4, s.getCapacity());
            ps.setInt(5, s.getOccupied());
            ps.setString(6, s.getStatus());
            ps.setString(7, s.getContact());
            ps.setString(8, s.getInCharge());
            ps.setBoolean(9, s.hasMedical());
            ps.setBoolean(10, s.hasFood());
            ps.setBoolean(11, s.hasWater());
            ps.setString(12, s.getNotes());
            ps.executeUpdate();
        }
    }

    public void updateShelter(Shelter s) throws SQLException {
        String sql = "UPDATE shelters SET name=?,location=?,city=?,capacity=?,occupied=?,status=?,contact=?,in_charge=?,has_medical=?,has_food=?,has_water=?,notes=? WHERE shelter_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getLocation());
            ps.setString(3, s.getCity());
            ps.setInt(4, s.getCapacity());
            ps.setInt(5, s.getOccupied());
            ps.setString(6, s.getStatus());
            ps.setString(7, s.getContact());
            ps.setString(8, s.getInCharge());
            ps.setBoolean(9, s.hasMedical());
            ps.setBoolean(10, s.hasFood());
            ps.setBoolean(11, s.hasWater());
            ps.setString(12, s.getNotes());
            ps.setInt(13, s.getShelterId());
            ps.executeUpdate();
        }
    }

    public void deleteShelter(int id) throws SQLException {
        String sql = "DELETE FROM shelters WHERE shelter_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Shelter> getAllShelters() throws SQLException {
        List<Shelter> list = new ArrayList<>();
        String sql = "SELECT * FROM shelters ORDER BY shelter_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public int getTotalCapacity() throws SQLException {
        ResultSet rs = DBConnection.getConnection().createStatement()
                .executeQuery("SELECT SUM(capacity) FROM shelters WHERE status='OPEN'");
        return rs.next() ? rs.getInt(1) : 0;
    }

    public int getTotalOccupied() throws SQLException {
        ResultSet rs = DBConnection.getConnection().createStatement()
                .executeQuery("SELECT SUM(occupied) FROM shelters");
        return rs.next() ? rs.getInt(1) : 0;
    }

    public int getOpenCount() throws SQLException {
        ResultSet rs = DBConnection.getConnection().createStatement()
                .executeQuery("SELECT COUNT(*) FROM shelters WHERE status='OPEN'");
        return rs.next() ? rs.getInt(1) : 0;
    }

    private Shelter mapRow(ResultSet rs) throws SQLException {
        return new Shelter(
                rs.getInt("shelter_id"),
                rs.getString("name"),
                rs.getString("location") != null ? rs.getString("location") : "",
                rs.getString("city") != null ? rs.getString("city") : "",
                rs.getInt("capacity"),
                rs.getInt("occupied"),
                rs.getString("status"),
                rs.getString("contact") != null ? rs.getString("contact") : "",
                rs.getString("in_charge") != null ? rs.getString("in_charge") : "",
                rs.getBoolean("has_medical"),
                rs.getBoolean("has_food"),
                rs.getBoolean("has_water"),
                rs.getString("notes") != null ? rs.getString("notes") : ""
        );
    }
}
