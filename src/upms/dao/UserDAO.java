package upms.dao;

import upms.db.DBConnection;
import upms.model.User;
import java.sql.*;
import java.util.*;

public class UserDAO {

    public User login(String username, String password) {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Users WHERE username=? AND password=?");
            ps.setString(1, username); ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next())
                return new User(rs.getString("user_id"), rs.getString("username"),
                    rs.getString("password"), rs.getString("role"), rs.getString("emp_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return null;
    }

    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Users ORDER BY user_id");
            rs = ps.executeQuery();
            while (rs.next())
                list.add(new User(rs.getString("user_id"), rs.getString("username"),
                    rs.getString("password"), rs.getString("role"), rs.getString("emp_id")));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public boolean insert(User u) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("INSERT INTO Users VALUES (?,?,?,?,?)");
            ps.setString(1, u.getUserId()); ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword()); ps.setString(4, u.getRole());
            ps.setString(5, u.getEmpId());
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean update(User u) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("UPDATE Users SET username=?,password=?,role=?,emp_id=? WHERE user_id=?");
            ps.setString(1, u.getUsername()); ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole()); ps.setString(4, u.getEmpId());
            ps.setString(5, u.getUserId());
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean delete(String id) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("DELETE FROM Users WHERE user_id=?");
            ps.setString(1, id); ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public String nextId() {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT MAX(user_id) FROM Users");
            rs = ps.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(1)) + 1;
                return "U" + String.format("%02d", num);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return "U01";
    }
}
