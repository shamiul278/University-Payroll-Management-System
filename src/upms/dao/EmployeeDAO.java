package upms.dao;

import upms.db.DBConnection;
import upms.model.Employee;
import java.sql.*;
import java.util.*;

public class EmployeeDAO {

    public List<Employee> getAll() {
        List<Employee> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Employee ORDER BY emp_id");
            rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public Employee getById(String id) {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Employee WHERE emp_id=?");
            ps.setString(1, id); rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return null;
    }

    public List<Employee> getByDept(String deptId) {
        List<Employee> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Employee WHERE dept_id=? ORDER BY name");
            ps.setString(1, deptId); rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public boolean insert(Employee e) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement(
                "INSERT INTO Employee VALUES (?,?,?,?,?,?,?,?)");
            ps.setString(1, e.getEmpId()); ps.setString(2, e.getName());
            ps.setString(3, e.getDesignation()); ps.setString(4, e.getEmail());
            ps.setString(5, e.getPhone());
            ps.setDate(6, new java.sql.Date(e.getJoinDate().getTime()));
            ps.setString(7, e.getEmploymentType()); ps.setString(8, e.getDeptId());
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean update(Employee e) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement(
                "UPDATE Employee SET name=?,designation=?,email=?,phone=?,join_date=?,employment_type=?,dept_id=? WHERE emp_id=?");
            ps.setString(1, e.getName()); ps.setString(2, e.getDesignation());
            ps.setString(3, e.getEmail()); ps.setString(4, e.getPhone());
            ps.setDate(5, new java.sql.Date(e.getJoinDate().getTime()));
            ps.setString(6, e.getEmploymentType()); ps.setString(7, e.getDeptId());
            ps.setString(8, e.getEmpId());
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean delete(String id) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("DELETE FROM Employee WHERE emp_id=?");
            ps.setString(1, id); ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public String nextId() {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT MAX(emp_id) FROM Employee");
            rs = ps.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                String last = rs.getString(1);
                int num = Integer.parseInt(last.substring(1)) + 1;
                return "E" + String.format("%02d", num);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return "E01";
    }

    private Employee map(ResultSet rs) throws SQLException {
        return new Employee(
            rs.getString("emp_id"), rs.getString("name"),
            rs.getString("designation"), rs.getString("email"),
            rs.getString("phone"), rs.getDate("join_date"),
            rs.getString("employment_type"), rs.getString("dept_id"));
    }
}
