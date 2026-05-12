package upms.dao;

import upms.db.DBConnection;
import upms.model.Payroll;
import java.sql.*;
import java.util.*;

public class PayrollDAO {

    public List<Payroll> getAll() {
        List<Payroll> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Payroll ORDER BY generated_date DESC");
            rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public Payroll getById(String id) {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Payroll WHERE payroll_id=?");
            ps.setString(1, id); rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return null;
    }

    public List<Payroll> getByEmployee(String empId) {
        List<Payroll> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Payroll WHERE emp_id=? ORDER BY generated_date DESC");
            ps.setString(1, empId); rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public boolean insert(Payroll p) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("INSERT INTO Payroll VALUES (?,?,?,?,?,?)");
            ps.setString(1, p.getPayrollId()); ps.setString(2, p.getMonth());
            ps.setDate(3, new java.sql.Date(p.getGeneratedDate().getTime()));
            ps.setString(4, p.getPaymentStatus()); ps.setDouble(5, p.getTotalSalary());
            ps.setString(6, p.getEmpId());
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean updateStatus(String payrollId, String status) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("UPDATE Payroll SET payment_status=? WHERE payroll_id=?");
            ps.setString(1, status); ps.setString(2, payrollId);
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean delete(String id) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("DELETE FROM Payroll_Bonus WHERE payroll_id=?");
            ps.setString(1, id); ps.executeUpdate(); ps.close();
            ps = c.prepareStatement("DELETE FROM Payroll_Deduction WHERE payroll_id=?");
            ps.setString(1, id); ps.executeUpdate(); ps.close();
            ps = c.prepareStatement("DELETE FROM Payroll WHERE payroll_id=?");
            ps.setString(1, id); ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public double getTotalExpenditure(String month) {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT SUM(total_salary) FROM Payroll WHERE month_=?");
            ps.setString(1, month); rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return 0;
    }

    public String nextId() {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT MAX(payroll_id) FROM Payroll");
            rs = ps.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(1)) + 1;
                return "P" + String.format("%02d", num);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return "P01";
    }

    private Payroll map(ResultSet rs) throws SQLException {
        return new Payroll(rs.getString("payroll_id"), rs.getString("month_"),
                rs.getDate("generated_date"), rs.getString("payment_status"),
                rs.getDouble("total_salary"), rs.getString("emp_id"));
    }
}
