package com.qbanking.dao;

import com.qbanking.model.Role;
import com.qbanking.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** All SQL for the users (login) table. */
public class UserDAO {

    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, customer_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.username());
            statement.setString(2, user.passwordHash());
            statement.setString(3, user.role().name());
            if (user.customerId() > 0) {
                statement.setLong(4, user.customerId());
            } else {
                statement.setNull(4, Types.BIGINT);     // staff are not customers
            }
            statement.executeUpdate();
        }
    }

    /** Username matching is not case sensitive. Returns null when not found. */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, customer_id FROM users "
                + "WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new User(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"),
                        Role.valueOf(rs.getString("role")), rs.getLong("customer_id")); // NULL becomes 0
            }
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        return findByUsername(username) != null;
    }

    public void delete(String username) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE LOWER(username) = LOWER(?)")) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }
}
