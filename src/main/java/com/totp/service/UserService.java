// ============================================================================
// User Service (Database Operations)
// ============================================================================
package com.totp.service;

import com.totp.config.DatabaseConfig;
import com.totp.model.User;
import com.totp.util.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;

public class UserService {
    private final Connection connection;

    public UserService() {
        try {
            DatabaseConfig.initializeDatabase();
            this.connection = DatabaseConfig.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize UserService", e);
        }
    }

    public boolean createUser(User user) {
        String sql = """
            INSERT INTO users (email, username, password, totp_secret, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, PasswordUtil.hashPassword(user.getPassword()));
            pstmt.setString(4, user.getTotpSecret());
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }

        return false;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }

        return false;
    }

    public User authenticateUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");

                    if (PasswordUtil.verifyPassword(password, hashedPassword)) {
                        User user = new User();
                        user.setId(rs.getLong("id"));
                        user.setEmail(rs.getString("email"));
                        user.setUsername(rs.getString("username"));
                        user.setPassword(hashedPassword);
                        user.setTotpSecret(rs.getString("totp_secret"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }

        return null;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}

