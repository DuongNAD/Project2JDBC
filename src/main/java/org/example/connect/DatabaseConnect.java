package org.example.connect;

import org.example.util.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnect {
    private static final String URL = Config.get("db.url", "jdbc:mysql://localhost:3306/elearning_system");
    private static final String USER = Config.get("db.user", "root");
    private static final String PASSWORD = Config.get("db.password", "2010");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
