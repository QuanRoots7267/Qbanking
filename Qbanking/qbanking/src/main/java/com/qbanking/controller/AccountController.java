package com.qbanking.controller;

import com.qbanking.dao.AccountDAO;
import com.qbanking.dao.Database;
import com.qbanking.dao.TransactionDAO;
import com.qbanking.model.Account;
import com.qbanking.model.AccountType;
import com.qbanking.model.BankingException;
import com.qbanking.model.ChequeAccount;
import com.qbanking.model.Customer;
import com.qbanking.model.InvestmentAccount;
import com.qbanking.model.SavingsAccount;
import com.qbanking.model.Transaction;
import com.qbanking.model.TransactionType;
import com.qbanking.model.Validation;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Everything a customer can do with their accounts:
 * open, list, deposit, withdraw and view history (FR3, FR4, FR8, FR9, FR11).
 *
 * Controller's job: check the input -> ask the Account (model) to apply its rules
 * -> save the result with the DAOs, all in one database transaction.
 */
public class AccountController {

    private final Session session;

    public AccountController(Session session) {
        this.session = session;
    }

    public List<Account> getMyAccounts() {
        Customer customer = session.requireCustomer();
        return Database.read(connection -> new AccountDAO(connection).findByCustomerId(customer.getId()));
    }

    /**
     * Opens a new account for the logged-in customer.
     * employerName / employerAddress are only used for Cheque accounts.
     */
    public Account openAccount(AccountType type, String branch, String openingDepositText,
                               String employerName, String employerAddress) {
        Customer owner = session.requireCustomer();
        if (type == null) {
            throw new BankingException("Please choose an account type.");
        }
        String branchName = Validation.requireText(branch, "Branch");
        BigDecimal openingDeposit = parseOpeningDeposit(openingDepositText);

        // The model classes enforce their own rules (e.g. Investment minimum BWP 500).
        Account account = switch (type) {
            case SAVINGS -> new SavingsAccount(owner, branchName, openingDeposit);
            case INVESTMENT -> new InvestmentAccount(owner, branchName, openingDeposit);
            case CHEQUE -> new ChequeAccount(owner, branchName, openingDeposit, employerName, employerAddress);
        };

        return Database.inTransaction(connection -> {
            new AccountDAO(connection).insert(account);
            if (account.getBalance().signum() > 0) {
                new TransactionDAO(connection).insert(new Transaction(account.getAccountNumber(),
                        TransactionType.DEPOSIT, account.getBalance(), account.getBalance(), "Opening deposit"));
            }
            return account;
        });
    }

    public Transaction deposit(long accountNumber, String amountText) {
        BigDecimal amount = Validation.parseAmount(amountText);
        return Database.inTransaction(connection -> {
            Account account = findMyAccount(connection, accountNumber);
            Transaction transaction = account.deposit(amount);
            saveChange(connection, account, transaction);
            return transaction;
        });
    }

    public Transaction withdraw(long accountNumber, String amountText) {
        BigDecimal amount = Validation.parseAmount(amountText);
        return Database.inTransaction(connection -> {
            Account account = findMyAccount(connection, accountNumber);
            Transaction transaction = account.withdraw(amount);   // Savings refuses, others check the balance
            saveChange(connection, account, transaction);
            return transaction;
        });
    }

    public List<Transaction> getTransactions(long accountNumber) {
        return Database.read(connection -> {
            findMyAccount(connection, accountNumber);              // makes sure it is my account
            return new TransactionDAO(connection).findByAccount(accountNumber);
        });
    }

    // ---------------------------------------------------------------- helpers

    /** Finds the account AND checks it belongs to the logged-in customer. */
    private Account findMyAccount(Connection connection, long accountNumber) throws SQLException {
        Customer customer = session.requireCustomer();
        Account account = new AccountDAO(connection).findByNumber(accountNumber);
        if (account == null || account.getOwner().getId() != customer.getId()) {
            throw new BankingException("Account not found.");
        }
        return account;
    }

    /** Saves the new balance and the transaction record together. */
    private void saveChange(Connection connection, Account account, Transaction transaction) throws SQLException {
        new AccountDAO(connection).updateBalance(account);
        new TransactionDAO(connection).insert(transaction);
    }

    /** Opening deposit may be 0 for Savings and Cheque accounts, so blank means 0. */
    private BigDecimal parseOpeningDeposit(String text) {
        if (text == null || text.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return Validation.requireValidAmount(new BigDecimal(text.trim()), false);
        } catch (NumberFormatException e) {
            throw new BankingException("'" + text.trim() + "' is not a valid amount. Example: 500.00");
        }
    }
}
