package emsapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {

    // Method to get a database connection with custom parameters
    public static Connection getConnection(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
    
    // Overloaded method to connect to the default database (event_management_system)
    public static Connection getConnection() throws SQLException {
        // Default database connection parameters
        String url = "jdbc:mysql://localhost:3306/event_management_system"; // Updated database name
        String user = ""; // Default username
        String password = ""; // Default password

        return DriverManager.getConnection(url, user, password);
    }
}
