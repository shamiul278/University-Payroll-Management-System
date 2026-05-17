package upms.dao;

import upms.db.DBConnection;
import upms.model.Payroll;
import java.sql.*;
import java.util.*;

public class PayrollDAO {
    private String lastErrorMessage = "";

    public static class GenerateResult {
        private final String payrollId;
        private final double totalSalary;

        public GenerateResult(String payrollId, double totalSalary) {
            this.payrollId = payrollId;
            this.totalSalary = totalSalary;
        }

        public String getPayrollId() { return payrollId; }
        public double getTotalSalary() { return totalSalary; }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage == null || lastErrorMessage.isEmpty()
            ? "Unable to complete payroll operation."
            : lastErrorMessage;
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(SQLException e, String action) {
        String message = e.getMessage();
        if (message != null && message.contains("does not exist")) {
            lastErrorMessage = "Unable to " + action + " payroll because the payroll procedure is missing. Run sql\\create_tables.sql again.";
        } else if (message != null && message.contains("employee salary is not configured")) {
            lastErrorMessage = "Unable to generate payroll because this employee has no salary record.";
        } else if (e.getErrorCode() == 1062) {
            lastErrorMessage = "Unable to " + action + " payroll because this payroll ID already exists.";
        } else {
            lastErrorMessage = "Unable to " + action + " payroll. Database error: " + message;
        }
    }

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
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public GenerateResult generatePayroll(String payrollId, String month, String empId, String bonusId, String deductionId) {
        Connection c = null; CallableStatement cs = null;
        try {
            clearLastError();
            c = DBConnection.getConnection();
            cs = c.prepareCall("{CALL sp_generate_payroll(?,?,?,?,?,?)}");
            cs.setString(1, payrollId);
            cs.setString(2, month);
            cs.setString(3, empId);
            if (bonusId == null) cs.setNull(4, Types.VARCHAR);
            else cs.setString(4, bonusId);
            if (deductionId == null) cs.setNull(5, Types.VARCHAR);
            else cs.setString(5, deductionId);
            cs.registerOutParameter(6, Types.DECIMAL);
            cs.execute();
            return new GenerateResult(payrollId, cs.getDouble(6));
        } catch (SQLException e) { setLastError(e, "generate"); e.printStackTrace(); return null; }
        finally { DBConnection.close(c, cs); }
    }

    public boolean updateStatus(String payrollId, String status) {
        Connection c = null; PreparedStatement ps = null;
        try {
            clearLastError();
            c = DBConnection.getConnection();
            ps = c.prepareStatement("UPDATE Payroll SET payment_status=? WHERE payroll_id=?");
            ps.setString(1, status); ps.setString(2, payrollId);
            int rows = ps.executeUpdate();
            if (rows > 0) return true;
            lastErrorMessage = "Unable to update payroll status because the payroll record was not found.";
            return false;
        } catch (SQLException e) { setLastError(e, "update"); e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean delete(String id) {
        Connection c = null; PreparedStatement ps = null;
        try {
            clearLastError();
            c = DBConnection.getConnection();
            ps = c.prepareStatement("DELETE FROM Payroll_Bonus WHERE payroll_id=?");
            ps.setString(1, id); ps.executeUpdate(); ps.close();
            ps = c.prepareStatement("DELETE FROM Payroll_Deduction WHERE payroll_id=?");
            ps.setString(1, id); ps.executeUpdate(); ps.close();
            ps = c.prepareStatement("DELETE FROM Payroll WHERE payroll_id=?");
            ps.setString(1, id); int rows = ps.executeUpdate();
            if (rows > 0) return true;
            lastErrorMessage = "Unable to delete payroll because the payroll record was not found.";
            return false;
        } catch (SQLException e) { setLastError(e, "delete"); e.printStackTrace(); return false; }
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
