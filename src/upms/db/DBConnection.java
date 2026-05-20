package upms.db;

import java.sql.*;

public class DBConnection {
    private static final String URL      = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER     = "Project";
    private static final String PASSWORD = "12345678";

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found. Add ojdbc14.jar to the lib folder.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(Connection c, PreparedStatement ps, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (c  != null) c.close();  } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void close(Connection c, PreparedStatement ps) {
        close(c, ps, null);
    }
}
