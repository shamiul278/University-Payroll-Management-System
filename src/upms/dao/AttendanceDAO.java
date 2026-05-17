package upms.dao;

import upms.db.DBConnection;
import upms.model.Attendance;
import java.sql.*;
import java.util.*;

public class AttendanceDAO {

    public List<Attendance> getAll() {
        List<Attendance> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Attendance ORDER BY date_ DESC, attendance_id");
            rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public List<Attendance> getByEmployee(String empId) {
        List<Attendance> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Attendance WHERE emp_id=? ORDER BY date_ DESC");
            ps.setString(1, empId); rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public List<Attendance> getByDate(java.util.Date date) {
        List<Attendance> list = new ArrayList<>();
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT * FROM Attendance WHERE date_=? ORDER BY emp_id");
            ps.setDate(1, new java.sql.Date(date.getTime())); rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return list;
    }

    public int countByStatus(String empId, String status) {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT COUNT(*) FROM Attendance WHERE emp_id=? AND status=?");
            ps.setString(1, empId); ps.setString(2, status); rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return 0;
    }

    public boolean insert(Attendance a) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("INSERT INTO Attendance VALUES (?,?,?,?)");
            ps.setString(1, a.getAttendanceId());
            ps.setDate(2, new java.sql.Date(a.getDate().getTime()));
            ps.setString(3, a.getStatus()); ps.setString(4, a.getEmpId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean update(Attendance a) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("UPDATE Attendance SET date_=?,status=? WHERE attendance_id=?");
            ps.setDate(1, new java.sql.Date(a.getDate().getTime()));
            ps.setString(2, a.getStatus());
            ps.setString(3, a.getAttendanceId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public boolean delete(String id) {
        Connection c = null; PreparedStatement ps = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("DELETE FROM Attendance WHERE attendance_id=?");
            ps.setString(1, id); int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
        finally { DBConnection.close(c, ps); }
    }

    public String nextId() {
        Connection c = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            ps = c.prepareStatement("SELECT MAX(attendance_id) FROM Attendance");
            rs = ps.executeQuery();
            if (rs.next() && rs.getString(1) != null) {
                String last = rs.getString(1);
                int num = Integer.parseInt(last.substring(1)) + 1;
                return "A" + String.format("%02d", num);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { DBConnection.close(c, ps, rs); }
        return "A01";
    }

    private Attendance map(ResultSet rs) throws SQLException {
        return new Attendance(rs.getString("attendance_id"), rs.getDate("date_"),
                rs.getString("status"), rs.getString("emp_id"));
    }
}
