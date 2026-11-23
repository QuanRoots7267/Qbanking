package com.qbanking.dao;

import com.qbanking.model.BankingException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public final class Database {

    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static String url = "jdbc:h2:./data/qbanking";

    private Database() {
    }


    public static void useUrl(String newUrl) {
        url = newUrl;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, USER, PASSWORD);
    }

    @FunctionalInterface
    public interface SqlWork<T> {
        T run(Connection connection) throws SQLException;
    }


    public static <T> T read(SqlWork<T> work) {
        try (Connection connection = getConnection()) {
            return work.run(connection);
        } catch (SQLException e) {
            throw new BankingException("A database error occurred: " + e.getMessage(), e);
        }
    }


    public static <T> T inTransaction(SqlWork<T> work) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new BankingException("A database error occurred: " + e.getMessage(), e);
        }
    }
}
