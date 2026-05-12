package upms.dao;

import upms.db.DBConnection;
import upms.model.Salary;
import java.sql.*;
import java.util.*;

public class SalaryDAO {

    public List<Salary> getAll() {
        List<Salary> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Salary ORDER BY salary_id");
            rs = ps.executeQuery();
            while (rs.next())
                list.add(new Salary(rs.getString("salary_id"), rs.getDouble("basic_salary"),
                        rs.getDouble("allowance"), rs.getString("emp_id")));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public Salary getByEmpId(String empId) {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Salary WHERE emp_id=?");
            ps.setString(1, empId); rs = ps.executeQuery();
            if (rs.next())
                return new Salary(rs.getString("salary_id"), rs.getDouble("basic_salary"),
                        rs.getDouble("allowance"), rs.getString("emp_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return null;
    }

    public boolean insert(Salary s) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("INSERT INTO Salary VALUES (?,?,?,?)");
            ps.setString(1, s.getSalaryId()); ps.setDouble(2, s.getBasicSalary());
            ps.setDouble(3, s.getAllowance()); ps.setString(4, s.getEmpId());
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean update(Salary s) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("UPDATE Salary SET basic_salary=?,allowance=? WHERE emp_id=?");
            ps.setDouble(1, s.getBasicSalary()); ps.setDouble(2, s.getAllowance());
            ps.setString(3, s.getEmpId());
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean delete(String id) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("DELETE FROM Salary WHERE salary_id=?");
            ps.setString(1, id); ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public String nextId() {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT MAX(salary_id) FROM Salary");
            rs = ps.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(1)) + 1;
                return "S" + String.format("%02d", num);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return "S01";
    }
}
