package upms.db;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class DBConnection {
    private static final String URL      = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER     = "Project";
    private static final String PASSWORD = "12345678";
    private static final int MAX_POOL_SIZE = 5;
    private static final int LOGIN_TIMEOUT_SECONDS = 5;
    private static final long IDLE_VALIDATION_MS = 30000L;
    private static final Deque<IdleConnection> idleConnections = new ArrayDeque<>();
    private static int pooledConnectionCount = 0;

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            DriverManager.setLoginTimeout(LOGIN_TIMEOUT_SECONDS);
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found. Add ojdbc14.jar to the lib folder.");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(DBConnection::closeIdleConnections));
        testConnection();
    }

    public static Connection getConnection() throws SQLException {
        synchronized (DBConnection.class) {
            while (!idleConnections.isEmpty()) {
                IdleConnection idle = idleConnections.removeFirst();
                if (isRecentlyUsed(idle) || isConnectionUsable(idle.connection)) {
                    return wrapConnection(idle.connection, true);
                }
                closePhysical(idle.connection);
                pooledConnectionCount--;
            }

            if (pooledConnectionCount < MAX_POOL_SIZE) {
                Connection physical = createPhysicalConnection();
                pooledConnectionCount++;
                return wrapConnection(physical, true);
            }
        }

        return wrapConnection(createPhysicalConnection(), false);
    }

    public static void close(Connection c, PreparedStatement ps, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (c  != null) c.close();  } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void close(Connection c, PreparedStatement ps) {
        close(c, ps, null);
    }

    public static void testConnection() {
        try (Connection c = getConnection()) {
            if (isConnectionUsable(c))
                System.out.println("[DB] Connected to Oracle database successfully. URL: " + URL);
            else
                System.err.println("[DB] Connection obtained but reported as invalid.");
        } catch (SQLException e) {
            System.err.println("[DB] Connection FAILED: " + e.getMessage());
        }
    }

    public static void verifyTables() {
        List<String> required = Arrays.asList(
            "DEPARTMENT", "EMPLOYEE", "ATTENDANCE",
            "BONUS", "DEDUCTION", "SALARY",
            "PAYROLL", "PAYROLL_BONUS", "PAYROLL_DEDUCTION", "USERS"
        );

        String line = "+----+--------------------+---------+";
        String fmt  = "| %-2s | %-18s | %-7s |";

        System.out.println();
        System.out.println(line);
        System.out.printf ((fmt + "%n"), "#", "TABLE NAME", "STATUS");
        System.out.println(line);

        boolean allOk = true;
        int missing = 0;

        try (Connection c = getConnection()) {
            int no = 1;
            for (String table : required) {
                String sql = "SELECT COUNT(*) FROM user_tables WHERE table_name = ?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, table);
                    try (ResultSet rs = ps.executeQuery()) {
                        boolean exists = rs.next() && rs.getInt(1) > 0;
                        String status = exists ? "OK" : "MISSING";
                        System.out.printf((fmt + "%n"), no++, table, status);
                        if (!exists) { allOk = false; missing++; }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Could not verify tables: " + e.getMessage());
            return;
        }

        System.out.println(line);
        if (allOk)
            System.out.println("  All " + required.size() + " tables are present.\n");
        else
            System.out.println("  " + missing + " table(s) missing. Run:\n"
                + "  sqlplus Project/12345678@XE @sql\\upms_oracle10g.sql\n");
    }

    private static Connection createPhysicalConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static boolean isRecentlyUsed(IdleConnection idle) {
        return System.currentTimeMillis() - idle.returnedAt < IDLE_VALIDATION_MS;
    }

    private static boolean isConnectionUsable(Connection c) {
        if (c == null) return false;
        try {
            if (c.isClosed()) return false;
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT 1 FROM dual")) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private static Connection wrapConnection(Connection physical, boolean pooled) {
        InvocationHandler handler = new PooledConnectionHandler(physical, pooled);
        return (Connection) Proxy.newProxyInstance(
            DBConnection.class.getClassLoader(),
            new Class<?>[] { Connection.class },
            handler);
    }

    private static void returnToPool(Connection physical) throws SQLException {
        if (!resetConnection(physical)) {
            closeAndRemoveFromPool(physical);
            return;
        }

        synchronized (DBConnection.class) {
            if (idleConnections.size() < MAX_POOL_SIZE) {
                idleConnections.addLast(new IdleConnection(physical));
                return;
            }
            pooledConnectionCount--;
        }
        closePhysical(physical);
    }

    private static boolean resetConnection(Connection physical) {
        try {
            if (physical.isClosed()) return false;
            if (!physical.getAutoCommit()) {
                physical.rollback();
                physical.setAutoCommit(true);
            }
            if (physical.isReadOnly()) physical.setReadOnly(false);
            physical.clearWarnings();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static void closeAndRemoveFromPool(Connection physical) {
        synchronized (DBConnection.class) {
            if (pooledConnectionCount > 0) pooledConnectionCount--;
        }
        closePhysical(physical);
    }

    private static void closePhysical(Connection physical) {
        try { if (physical != null) physical.close(); } catch (SQLException ignored) {}
    }

    private static void closeIdleConnections() {
        synchronized (DBConnection.class) {
            while (!idleConnections.isEmpty()) {
                closePhysical(idleConnections.removeFirst().connection);
                if (pooledConnectionCount > 0) pooledConnectionCount--;
            }
        }
    }

    private static class IdleConnection {
        private final Connection connection;
        private final long returnedAt;

        IdleConnection(Connection connection) {
            this.connection = connection;
            this.returnedAt = System.currentTimeMillis();
        }
    }

    private static class PooledConnectionHandler implements InvocationHandler {
        private final Connection physical;
        private final boolean pooled;
        private boolean closed;

        PooledConnectionHandler(Connection physical, boolean pooled) {
            this.physical = physical;
            this.pooled = pooled;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();

            if ("close".equals(name)) {
                close();
                return null;
            }
            if ("isClosed".equals(name)) {
                return closed || physical.isClosed();
            }
            if ("unwrap".equals(name)) {
                Class<?> type = (Class<?>) args[0];
                if (type.isInstance(physical)) return physical;
            }
            if ("isWrapperFor".equals(name)) {
                Class<?> type = (Class<?>) args[0];
                return type.isInstance(physical);
            }
            if (closed) throw new SQLException("Connection is closed.");

            try {
                return method.invoke(physical, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private void close() throws SQLException {
            if (closed) return;
            closed = true;
            if (pooled) returnToPool(physical);
            else closePhysical(physical);
        }
    }
}
