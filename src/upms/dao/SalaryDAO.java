package upms.dao;

import upms.db.DBConnection;
import upms.model.Salary;
import java.sql.*;
import java.util.*;

public class SalaryDAO {
    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage == null || lastErrorMessage.isEmpty()
            ? "Unable to save salary record."
            : lastErrorMessage;
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(SQLException e, String action) {
        int code = e.getErrorCode();
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

        if (code == 1 || code == 1062) {
            if (message.contains("uq_salary_emp") || message.contains("emp_id")) {
                lastErrorMessage = "Unable to " + action + " salary because this employee already has a salary record.";
            } else {
                lastErrorMessage = "Unable to " + action + " salary because this salary ID already exists.";
            }
        } else if (code == 2291 || code == 1452) {
            lastErrorMessage = "Unable to " + action + " salary because the selected employee does not exist.";
        } else if (code == 1400 || code == 1048) {
            lastErrorMessage = "Unable to " + action + " salary because a required field is empty.";
        } else if (code == 12899 || code == 1406) {
            lastErrorMessage = "Unable to " + action + " salary because one field is longer than the database limit.";
        } else {
            lastErrorMessage = "Unable to " + action + " salary. Database error: " + e.getMessage();
        }
    }

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
            clearLastError();
            c = DBConnection.getConnection();
            ps = c.prepareStatement("INSERT INTO Salary VALUES (?,?,?,?)");
            ps.setString(1, s.getSalaryId()); ps.setDouble(2, s.getBasicSalary());
            ps.setDouble(3, s.getAllowance()); ps.setString(4, s.getEmpId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { setLastError(e, "save"); e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean update(Salary s) {
        Connection c = null; PreparedStatement ps = null;
        try {
            clearLastError();
            c = DBConnection.getConnection();
            ps = c.prepareStatement("UPDATE Salary SET basic_salary=?,allowance=? WHERE emp_id=?");
            ps.setDouble(1, s.getBasicSalary()); ps.setDouble(2, s.getAllowance());
            ps.setString(3, s.getEmpId());
            int rows = ps.executeUpdate();
            if (rows > 0) return true;
            lastErrorMessage = "Unable to update salary because the selected employee has no salary record.";
            return false;
        } catch (SQLException e) { setLastError(e, "update"); e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean delete(String id) {
        Connection c = null; PreparedStatement ps = null;
        try {
            clearLastError();
            c = DBConnection.getConnection();
            ps = c.prepareStatement("DELETE FROM Salary WHERE salary_id=?");
            ps.setString(1, id); int rows = ps.executeUpdate();
            if (rows > 0) return true;
            lastErrorMessage = "Unable to remove salary because the salary record was not found.";
            return false;
        } catch (SQLException e) { setLastError(e, "remove"); e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public int countPayrollsForEmployee(String empId) {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT COUNT(*) FROM Payroll WHERE emp_id=?");
            ps.setString(1, empId);
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
