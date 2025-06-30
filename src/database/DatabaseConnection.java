package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdbi.v3.core.Jdbi;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import constants.ServerConstants;
import tools.FilePrinter;

/**
 * @author Frz - Big Daddy
 * @author The Real Spookster - some modifications to this beautiful code
 * @author Ronan - some connection pool to this beautiful code
 */
public class DatabaseConnection {

    private static HikariDataSource ps;
    private static HikariDataSource ws;

    public static Connection getWorldConnection() throws SQLException {
        //System.out.println("connection");
        int denies = 0;
        if (ws != null) {
            try {
                return ws.getConnection();
            } catch (SQLException sqle) {
                FilePrinter.printError(FilePrinter.SQL_EXCEPTION, "SQL Driver refused to give a connection. Problem: " + sqle.getMessage());
                sqle.printStackTrace();
                throw sqle;
            }
        }

        while (true) {   // There is no way it can pass with a null out of here?
            try {
                return DriverManager.getConnection("jdbc:mysql://localhost:3306/lithium?autoReconnect=true&useSSL=false", "root", "");
                //return DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);
            } catch (SQLException sqle) {
                denies++;

                if (denies == 3) {
                    // Give up, throw exception. Nothing good will come from this.
                    FilePrinter.printError(FilePrinter.SQL_EXCEPTION, "SQL Driver refused to give a connection after " + denies + " tries. Problem: " + sqle.getMessage());
                    sqle.printStackTrace();
                    throw sqle;
                }
            }
        }
    }

    public static Connection getPlayerConnection() throws SQLException {
        //System.out.println("connection");
        int denies = 0;
        if (ps != null) {
            try {
                return ps.getConnection();
            } catch (SQLException sqle) {
                FilePrinter.printError(FilePrinter.SQL_EXCEPTION, "SQL Driver refused to give a connection. Problem: " + sqle.getMessage());
                sqle.printStackTrace();
                throw sqle;
            }
        }

        while (true) {   // There is no way it can pass with a null out of here?
            try {
                return DriverManager.getConnection("jdbc:mysql://localhost:3306/lithium?autoReconnect=true&useSSL=false", "root", "");
                //return DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);
            } catch (SQLException sqle) {
                denies++;

                if (denies == 3) {
                    // Give up, throw exception. Nothing good will come from this.
                    FilePrinter.printError(FilePrinter.SQL_EXCEPTION, "SQL Driver refused to give a connection after " + denies + " tries. Problem: " + sqle.getMessage());
                    sqle.printStackTrace();
                    throw sqle;
                }
            }
        }
    }

    private static int getNumberOfAccounts() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT count(*) FROM accounts")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            } finally {
                con.close();
            }
        } catch (SQLException sqle) {
            return 20;
        }
    }

    public DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // touch the mysql driver
        } catch (ClassNotFoundException e) {
            System.out.println("[SEVERE] SQL Driver Not Found. Consider death by clams.");
            e.printStackTrace();
        }

        ws = null;
        ps = null;
        if (ServerConstants.DB_CONNECTION_POOL) {
            // Connection Pool on database ftw!
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/lithium?autoReconnect=true&useSSL=false");
            config.setUsername("root");
            config.setPassword("");

            // Make sure pool size is comfortable for the worst case scenario.
            // Under 100 accounts? Make it 10. Over 10000 accounts? Make it 30.
            int poolSize = (int) Math.ceil(0.00202020202 * getNumberOfAccounts() + 9.797979798);
            if (poolSize < 10) {
                poolSize = 10;
            } else if (poolSize > 30) {
                poolSize = 30;
            }
            config.setMaximumPoolSize(poolSize);
            config.setConnectionTimeout(30000);
            config.addDataSourceProperty("cachePrepStmts", true);
            config.addDataSourceProperty("useServerPrepStmts", true);
            config.addDataSourceProperty("prepStmtCacheSize", 300);
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 8192);
            ws = new HikariDataSource(config);
        }
        if (ServerConstants.DB_CONNECTION_POOL) {
            // Connection Pool on database ftw!
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/lithium?autoReconnect=true&useSSL=false");
            config.setUsername("root");
            config.setPassword("");

            // Make sure pool size is comfortable for the worst case scenario.
            // Under 100 accounts? Make it 10. Over 10000 accounts? Make it 30.
            int poolSize = (int) Math.ceil(0.00202020202 * getNumberOfAccounts() + 9.797979798);
            if (poolSize < 10) {
                poolSize = 10;
            } else if (poolSize > 30) {
                poolSize = 30;
            }
            config.setMaximumPoolSize(poolSize);
            config.setConnectionTimeout(30000);
            config.addDataSourceProperty("cachePrepStmts", true);
            config.addDataSourceProperty("useServerPrepStmts", true);
            config.addDataSourceProperty("prepStmtCacheSize", 300);
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 8192);
            ps = new HikariDataSource(config);
        }
    }

    public static Jdbi createJdbi() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/lithium?autoReconnect=true&useSSL=false");
        config.setUsername("root");
        config.setPassword("");

        int poolSize = (int) Math.ceil(0.00202020202 * getNumberOfAccounts() + 9.797979798);
        if (poolSize < 10) {
            poolSize = 10;
        } else if (poolSize > 30) {
            poolSize = 30;
        }
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(30000);

        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 300);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 8192);

        return Jdbi.create(new HikariDataSource(config));
    }
}
