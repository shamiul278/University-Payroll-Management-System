package upms.dao;

import upms.db.DBConnection;
import upms.model.Bonus;
import java.sql.*;
import java.util.*;

public class BonusDAO {

    public List<Bonus> getAll() {
        List<Bonus> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Bonus ORDER BY bonus_id");
            rs = ps.executeQuery();
            while (rs.next())
                list.add(new Bonus(rs.getString("bonus_id"), rs.getDouble("amount"), rs.getString("type")));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public List<Bonus> getByPayroll(String payrollId) {
        List<Bonus> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement(
                "SELECT b.* FROM Bonus b JOIN Payroll_Bonus pb ON b.bonus_id=pb.bonus_id WHERE pb.payroll_id=?");
            ps.setString(1, payrollId); rs = ps.executeQuery();
            while (rs.next())
                list.add(new Bonus(rs.getString("bonus_id"), rs.getDouble("amount"), rs.getString("type")));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public boolean insert(Bonus b) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("INSERT INTO Bonus VALUES (?,?,?)");
            ps.setString(1, b.getBonusId()); ps.setDouble(2, b.getAmount()); ps.setString(3, b.getType());
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean update(Bonus b) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("UPDATE Bonus SET amount=?,type=? WHERE bonus_id=?");
            ps.setDouble(1, b.getAmount()); ps.setString(2, b.getType()); ps.setString(3, b.getBonusId());
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean delete(String id) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            // Remove from junction table first
            ps = c.prepareStatement("DELETE FROM Payroll_Bonus WHERE bonus_id=?");
            ps.setString(1, id); ps.executeUpdate();
            ps.close();
            ps = c.prepareStatement("DELETE FROM Bonus WHERE bonus_id=?");
            ps.setString(1, id); ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean linkToPayroll(String payrollId, String bonusId) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("INSERT INTO Payroll_Bonus VALUES (?,?)");
            ps.setString(1, payrollId); ps.setString(2, bonusId);
            ps.executeUpdate(); c.commit();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public String nextId() {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT MAX(bonus_id) FROM Bonus");
            rs = ps.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).substring(1)) + 1;
                return "B" + String.format("%02d", num);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return "B01";
    }
}
