package com.qbanking.dao;

import com.qbanking.model.Transaction;
import com.qbanking.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** All SQL for the transactions table (the history of every account). */
public class TransactionDAO {

    private final Connection connection;

    public TransactionDAO(Connection connection) {
        this.connection = connection;
    }

    public void insert(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_number, type, amount, balance_after, description, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, transaction.accountNumber());
            statement.setString(2, transaction.type().name());
            statement.setBigDecimal(3, transaction.amount());
            statement.setBigDecimal(4, transaction.balanceAfter());
            statement.setString(5, transaction.description());
            statement.setTimestamp(6, Timestamp.valueOf(transaction.timestamp()));
            statement.executeUpdate();
        }
    }

    /** Newest transaction first. */
    public List<Transaction> findByAccount(long accountNumber) throws SQLException {
        String sql = "SELECT id, account_number, type, amount, balance_after, description, created_at "
                + "FROM transactions WHERE account_number = ? ORDER BY created_at DESC, id DESC";
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountNumber);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getLong("id"),
                            rs.getLong("account_number"),
                            TransactionType.valueOf(rs.getString("type")),
                            rs.getBigDecimal("amount"),
                            rs.getBigDecimal("balance_after"),
                            rs.getString("description"),
                            rs.getTimestamp("created_at").toLocalDateTime()));
                }
            }
        }
        return transactions;
    }

    /** Has interest already been paid to this account in the given month? */
    public boolean interestPaidInMonth(long accountNumber, YearMonth month) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE account_number = ? AND type = 'INTEREST' "
                + "AND created_at >= ? AND created_at < ?";
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, accountNumber);
            statement.setTimestamp(2, Timestamp.valueOf(start));
            statement.setTimestamp(3, Timestamp.valueOf(end));
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
