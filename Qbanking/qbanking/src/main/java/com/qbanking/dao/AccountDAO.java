package com.qbanking.dao;

import com.qbanking.model.Account;
import com.qbanking.model.AccountType;
import com.qbanking.model.ChequeAccount;
import com.qbanking.model.Customer;
import com.qbanking.model.InvestmentAccount;
import com.qbanking.model.SavingsAccount;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


public class AccountDAO {
    // Joins the owner so every account comes back with its Customer object.
    private static final String SELECT_ACCOUNTS =
            "SELECT a.account_number, a.account_type, a.branch, a.balance, "
                    + "a.employer_name, a.employer_address, "
                    + "c.id AS customer_id, c.first_name, c.surname, c.address "
                    + "FROM accounts a JOIN customers c ON c.id = a.customer_id ";

    private final Connection connection;

    public AccountDAO(Connection connection) {
        this.connection = connection;
    }

    public void insert(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_type, branch, balance, customer_id, employer_name, employer_address) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, account.getAccountType().name());
            statement.setString(2, account.getBranch());
            statement.setBigDecimal(3, account.getBalance());
            statement.setLong(4, account.getOwner().getId());
            if (account instanceof ChequeAccount cheque) {
                statement.setString(5, cheque.getEmployerName());
                statement.setString(6, cheque.getEmployerAddress());
            } else {
                statement.setNull(5, Types.VARCHAR);
                statement.setNull(6, Types.VARCHAR);
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    account.setAccountNumber(keys.getLong(1));
                }
            }
        }
    }

    public Account findByNumber(long accountNumber) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ACCOUNTS + "WHERE a.account_number = ?")) {
            statement.setLong(1, accountNumber);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? toAccount(rs) : null;
            }
        }
    }

    public List<Account> findByCustomerId(long customerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                SELECT_ACCOUNTS + "WHERE a.customer_id = ? ORDER BY a.account_number")) {
            statement.setLong(1, customerId);
            return readAll(statement);
        }
    }

    public List<Account> findAll() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ACCOUNTS + "ORDER BY a.account_number")) {
            return readAll(statement);
        }
    }

    public void updateBalance(Account account) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, account.getBalance());
            statement.setLong(2, account.getAccountNumber());
            statement.executeUpdate();
        }
    }

    public void delete(long accountNumber) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM transactions WHERE account_number = ?")) {
            statement.setLong(1, accountNumber);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM accounts WHERE account_number = ?")) {
            statement.setLong(1, accountNumber);
            statement.executeUpdate();
        }
    }

    private List<Account> readAll(PreparedStatement statement) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                accounts.add(toAccount(rs));
            }
        }
        return accounts;
    }

    private Account toAccount(ResultSet rs) throws SQLException {
        Customer owner = new Customer(rs.getLong("customer_id"), rs.getString("first_name"),
                rs.getString("surname"), rs.getString("address"));
        long number = rs.getLong("account_number");
        String branch = rs.getString("branch");
        BigDecimal balance = rs.getBigDecimal("balance");

        return switch (AccountType.valueOf(rs.getString("account_type"))) {
            case SAVINGS -> new SavingsAccount(number, owner, branch, balance);
            case INVESTMENT -> new InvestmentAccount(number, owner, branch, balance);
            case CHEQUE -> new ChequeAccount(number, owner, branch, balance,
                    rs.getString("employer_name"), rs.getString("employer_address"));
        };
    }
}
