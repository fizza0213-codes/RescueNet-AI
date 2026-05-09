package database;

import models.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    public void addResource(Resource r) throws SQLException {
        String sql = "INSERT INTO resources(name,category,quantity,unit,location,status,donated_by) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getCategory());
            ps.setInt(3, r.getQuantity());
            ps.setString(4, r.getUnit());
            ps.setString(5, r.getLocation());
            ps.setString(6, r.getStatus());
            ps.setString(7, r.getDonatedBy());
            ps.executeUpdate();
        }
    }

    public void updateResource(Resource r) throws SQLException {
        String sql = "UPDATE resources SET name=?,category=?,quantity=?,unit=?,location=?,status=?,donated_by=? WHERE resource_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getCategory());
            ps.setInt(3, r.getQuantity());
            ps.setString(4, r.getUnit());
            ps.setString(5, r.getLocation());
            ps.setString(6, r.getStatus());
            ps.setString(7, r.getDonatedBy());
            ps.setInt(8, r.getResourceId());
            ps.executeUpdate();
        }
    }

    public void deleteResource(int id) throws SQLException {
        String sql = "DELETE FROM resources WHERE resource_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Resource> getAllResources() throws SQLException {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources ORDER BY category, name";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public int getTotalItems() throws SQLException {
        ResultSet rs = DBConnection.getConnection().createStatement()
                .executeQuery("SELECT COUNT(*) FROM resources");
        return rs.next() ? rs.getInt(1) : 0;
    }

    private Resource mapRow(ResultSet rs) throws SQLException {
        return new Resource(
                rs.getInt("resource_id"),
                rs.getString("name"),
                rs.getString("category") != null ? rs.getString("category") : "OTHER",
                rs.getInt("quantity"),
                rs.getString("unit") != null ? rs.getString("unit") : "",
                rs.getString("location") != null ? rs.getString("location") : "",
                rs.getString("status"),
                rs.getString("donated_by") != null ? rs.getString("donated_by") : ""
        );
    }
}
