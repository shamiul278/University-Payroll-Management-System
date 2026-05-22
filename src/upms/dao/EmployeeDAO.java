package upms.dao;

import upms.db.DBConnection;
import upms.model.Employee;
import java.sql.*;
import java.util.*;

public class EmployeeDAO {
    private String lastErrorMessage = "";
    private static final int DELETE_TIMEOUT_SECONDS = 15;

    public String getLastErrorMessage() {
        return lastErrorMessage == null || lastErrorMessage.isEmpty()
            ? "Unable to save employee."
            : lastErrorMessage;
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(SQLException ex, String action) {
        int code = ex.getErrorCode();
        String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (code == 1 || code == 1062) {
            if (message.contains("email")) lastErrorMessage = "Unable to " + action + " employee because this email is already used.";
            else lastErrorMessage = "Unable to " + action + " employee because this employee ID already exists.";
        } else if (code == 2291 || code == 1452) {
            lastErrorMessage = "Unable to " + action + " employee because the selected department does not exist.";
        } else if (code == 12899 || code == 1406) {
            lastErrorMessage = "Unable to " + action + " employee because one field is longer than the database limit.";
        } else if (code == 1400 || code == 1048) {
            lastErrorMessage = "Unable to " + action + " employee because a required field is empty.";
        } else if (code == 54 || code == 30006 || message.contains("timeout")) {
            lastErrorMessage = "Unable to " + action + " employee because related records are locked by another database session. Try again after closing other open edit windows or committing pending SQL changes.";
        } else {
            lastErrorMessage = "Unable to " + action + " employee. Database error: " + ex.getMessage();
        }
    }

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
            clearLastError();
            c = DBConnection.getConnection();
            ps = c.prepareStatement(
                "INSERT INTO Employee VALUES (?,?,?,?,?,?,?,?)");
            ps.setString(1, e.getEmpId()); ps.setString(2, e.getName());
            ps.setString(3, e.getDesignation()); ps.setString(4, e.getEmail());
            ps.setString(5, e.getPhone());
            if (e.getJoinDate() == null) ps.setNull(6, Types.DATE);
            else ps.setDate(6, new java.sql.Date(e.getJoinDate().getTime()));
            ps.setString(7, e.getEmploymentType()); ps.setString(8, e.getDeptId());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) { setLastError(ex, "add"); ex.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean update(Employee e) {
        Connection c = null; PreparedStatement ps = null;
        try {
            clearLastError();
            c = DBConnection.getConnection();
            ps = c.prepareStatement(
                "UPDATE Employee SET name=?,designation=?,email=?,phone=?,join_date=?,employment_type=?,dept_id=? WHERE emp_id=?");
            ps.setString(1, e.getName()); ps.setString(2, e.getDesignation());
            ps.setString(3, e.getEmail()); ps.setString(4, e.getPhone());
            if (e.getJoinDate() == null) ps.setNull(5, Types.DATE);
            else ps.setDate(5, new java.sql.Date(e.getJoinDate().getTime()));
            ps.setString(6, e.getEmploymentType()); ps.setString(7, e.getDeptId());
            ps.setString(8, e.getEmpId());
            int rows = ps.executeUpdate();
            if (rows > 0) return true;
            lastErrorMessage = "Unable to update employee because the employee record was not found.";
            return false;
        } catch (SQLException ex) { setLastError(ex, "update"); ex.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean delete(String id) {
        Connection c = null; PreparedStatement ps = null;
        try {
            clearLastError();
            c = DBConnection.getConnection();
            c.setAutoCommit(false);

            executeEmployeeCleanup(c, "UPDATE Payroll SET emp_id=NULL WHERE emp_id=?", id);
            executeEmployeeCleanup(c, "UPDATE Users SET emp_id=NULL WHERE emp_id=?", id);
            executeEmployeeCleanup(c, "DELETE FROM Attendance WHERE emp_id=?", id);
            executeEmployeeCleanup(c, "DELETE FROM Salary WHERE emp_id=?", id);

            ps = c.prepareStatement("DELETE FROM Employee WHERE emp_id=?");
            ps.setQueryTimeout(DELETE_TIMEOUT_SECONDS);
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                c.rollback();
                lastErrorMessage = "Unable to delete employee because the employee record was not found.";
                return false;
            }
            c.commit();
            return true;
        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException rollbackError) { rollbackError.printStackTrace(); }
            setLastError(e, "delete");
            e.printStackTrace();
            return false;
        }
        finally { DBConnection.close(c, ps); }
    }

    private void executeEmployeeCleanup(Connection c, String sql, String empId) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = c.prepareStatement(sql);
            ps.setQueryTimeout(DELETE_TIMEOUT_SECONDS);
            ps.setString(1, empId);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
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
