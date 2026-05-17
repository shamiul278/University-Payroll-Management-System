package upms.dao;

import upms.db.DBConnection;
import upms.model.Deduction;
import java.sql.*;
import java.util.*;

public class DeductionDAO {

    public List<Deduction> getAll() {
        List<Deduction> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Deduction ORDER BY deduction_id");
            rs = ps.executeQuery();
            while (rs.next())
                list.add(new Deduction(rs.getString("deduction_id"), rs.getDouble("amount"), rs.getString("reason")));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public List<Deduction> getByPayroll(String payrollId) {
        List<Deduction> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement(
                "SELECT d.* FROM Deduction d JOIN Payroll_Deduction pd ON d.deduction_id=pd.deduction_id WHERE pd.payroll_id=?");
            ps.setString(1, payrollId); rs = ps.executeQuery();
            while (rs.next())
                list.add(new Deduction(rs.getString("deduction_id"), rs.getDouble("amount"), rs.getString("reason")));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public boolean insert(Deduction d) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("INSERT INTO Deduction VALUES (?,?,?)");
            ps.setString(1, d.getDeductionId()); ps.setDouble(2, d.getAmount()); ps.setString(3, d.getReason());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean update(Deduction d) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("UPDATE Deduction SET amount=?,reason=? WHERE deduction_id=?");
            ps.setDouble(1, d.getAmount()); ps.setString(2, d.getReason()); ps.setString(3, d.getDeductionId());
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
            ps = c.prepareStatement("DELETE FROM Payroll_Deduction WHERE deduction_id=?");
            ps.setString(1, id); ps.executeUpdate(); ps.close();
            ps = c.prepareStatement("DELETE FROM Deduction WHERE deduction_id=?");
            ps.setString(1, id); int rows = ps.executeUpdate(); c.commit();
            return rows > 0;
        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace(); return false;
        }
        finally {
            try { if (c != null) c.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            DBConnection.close(c, ps);
        }
    }

    public int countPayrollLinks(String deductionId) {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT COUNT(*) FROM Payroll_Deduction WHERE deduction_id=?");
            ps.setString(1, deductionId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return 0;
    }

    public boolean linkToPayroll(String payrollId, String deductionId) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("INSERT INTO Payroll_Deduction VALUES (?,?)");
            ps.setString(1, payrollId); ps.setString(2, deductionId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public String nextId() {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT MAX(deduction_id) FROM Deduction");
            rs = ps.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                String last = rs.getString(1);
                // Handle DD1, DD2... format
                int num = Integer.parseInt(last.replaceAll("[^0-9]", "")) + 1;
                return "DD" + num;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return "DD1";
    }
}
