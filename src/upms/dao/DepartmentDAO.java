package upms.dao;

import upms.db.DBConnection;
import upms.model.Department;
import java.sql.*;
import java.util.*;

public class DepartmentDAO {

    public List<Department> getAll() {
        List<Department> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Department ORDER BY dept_id");
            rs = ps.executeQuery();
            while (rs.next())
                list.add(new Department(rs.getString("dept_id"), rs.getString("dept_name"),
                        rs.getString("building"), rs.getString("contact_no")));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public Department getById(String id) {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Department WHERE dept_id=?");
            ps.setString(1, id);
            rs = ps.executeQuery();
            if (rs.next())
                return new Department(rs.getString("dept_id"), rs.getString("dept_name"),
                        rs.getString("building"), rs.getString("contact_no"));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return null;
    }

    public boolean insert(Department d) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("INSERT INTO Department VALUES (?,?,?,?)");
            ps.setString(1, d.getDeptId()); ps.setString(2, d.getDeptName());
            ps.setString(3, d.getBuilding()); ps.setString(4, d.getContactNo());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean update(Department d) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("UPDATE Department SET dept_name=?,building=?,contact_no=? WHERE dept_id=?");
            ps.setString(1, d.getDeptName()); ps.setString(2, d.getBuilding());
            ps.setString(3, d.getContactNo()); ps.setString(4, d.getDeptId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean delete(String id) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);

            ps = c.prepareStatement("UPDATE Employee SET dept_id=NULL WHERE dept_id=?");
            ps.setString(1, id);
            ps.executeUpdate();
            ps.close();

            ps = c.prepareStatement("DELETE FROM Department WHERE dept_id=?");
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            c.commit();
            return rows > 0;
        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (c != null) c.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            DBConnection.close(c, ps);
        }
    }

    public int countEmployees(String deptId) {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT COUNT(*) FROM Employee WHERE dept_id=?");
            ps.setString(1, deptId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return 0;
    }

    public String nextId() {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT MAX(dept_id) FROM Department");
            rs = ps.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                String last = rs.getString(1);
                int num = Integer.parseInt(last.substring(1)) + 1;
                return "D" + String.format("%02d", num);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return "D01";
    }
}
