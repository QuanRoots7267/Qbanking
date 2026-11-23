package com.qbanking.dao;

import com.qbanking.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private final Connection connection;

    public CustomerDAO(Connection connection) {
        this.connection = connection;
    }

    public void insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (first_name, surname, address) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getSurname());
            statement.setString(3, customer.getAddress());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getLong(1));
                }
            }
        }
    }

    public Customer findById(long id) throws SQLException {
        String sql = "SELECT id, first_name, surname, address FROM customers WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? toCustomer(rs) : null;
            }
        }
    }

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT id, first_name, surname, address FROM customers ORDER BY id";
        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                customers.add(toCustomer(rs));
            }
        }
        return customers;
    }

    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET first_name = ?, surname = ?, address = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getSurname());
            statement.setString(3, customer.getAddress());
            statement.setLong(4, customer.getId());
            statement.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM customers WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM customers");
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Customer toCustomer(ResultSet rs) throws SQLException {
        return new Customer(rs.getLong("id"), rs.getString("first_name"),
                rs.getString("surname"), rs.getString("address"));
    }
}
