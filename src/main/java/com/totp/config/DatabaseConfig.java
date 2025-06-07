// ============================================================================
// Database Configuration
// ============================================================================
package com.totp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/<database_name>";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "<database_password>";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Postgtotp_authreSQL Driver not found", e);
        }
    }

    public static void initializeDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                email VARCHAR(255) UNIQUE NOT NULL,
                username VARCHAR(100) NOT NULL,
                password VARCHAR(255) NOT NULL,
                totp_secret VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(createTableSQL);
            System.out.println("✅ Database initialized successfully");

        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

