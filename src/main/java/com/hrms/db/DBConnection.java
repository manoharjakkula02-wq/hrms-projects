package com.hrms.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Creates JDBC connections using db.properties (env vars HRMS_DB_URL/USER/PASSWORD override). */
public final class DBConnection {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (in != null) PROPS.load(in);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() {}

    private static String cfg(String env, String key) {
        String v = System.getenv(env);
        return (v != null && !v.isBlank()) ? v : PROPS.getProperty(key);
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(cfg("HRMS_DB_URL", "db.url"),
                cfg("HRMS_DB_USER", "db.user"), cfg("HRMS_DB_PASSWORD", "db.password"));
    }
}
