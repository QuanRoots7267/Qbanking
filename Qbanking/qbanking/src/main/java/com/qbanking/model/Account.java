package com.qbanking.model;

import java.math.BigDecimal;

/**
 * ABSTRACTION: the common part of every bank account.
 * You cannot create an "Account" directly - only a Savings, Investment or Cheque account.
 *
 * ENCAPSULATION: all fields are private. The balance can only change through
 * deposit(), withdraw() or interest - never by setting it directly.
 */
public abstract class Account {

    private long accountNumber;          // 0 until the database assigns one
    private final Customer owner;        // an account cannot exist without a customer
    private final String branch;
    private BigDecimal balance;

    /** OVERLOADING (1): constructor for a NEW account (the database will give the number). */
    protected Account(Customer owner, String branch, BigDecimal openingDeposit) {
        this(0, owner, branch, Validation.requireValidAmount(openingDeposit, false));
    }

    /** OVERLOADING (2): constructor used when an existing account is loaded from the database. */
    protected Account(long accountNumber, Customer owner, String branch, BigDecimal balance) {
        if (owner == null) {
            throw new BankingException("An account must belong to a customer.");
        }
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.branch = Validation.requireText(branch, "Branch");
        this.balance = balance;
    }

    // ----- Behaviour that is the same for every account type -----

    /** Adds money to the account. */
    public Transaction deposit(BigDecimal amount) {
        return deposit(amount, "Deposit");   // OVERLOADING: calls the version below
    }

    /** OVERLOADING: same as above but with a custom description. */
    public Transaction deposit(BigDecimal amount, String description) {
        BigDecimal validAmount = Validation.requireValidAmount(amount, true);
        return credit(validAmount, TransactionType.DEPOSIT, description);
    }

    // ----- Behaviour that each account type must define itself (OVERRIDING) -----

    /** Takes money out of the account (rules differ per account type). */
    public abstract Transaction withdraw(BigDecimal amount);

    /** Which kind of account this is. */
    public abstract AccountType getAccountType();

    // ----- Helpers for subclasses -----

    /** Adds money and builds the matching Transaction. */
    protected Transaction credit(BigDecimal amount, TransactionType type, String description) {
        balance = balance.add(amount);
        return new Transaction(accountNumber, type, amount, balance, description);
    }

    /** Removes money (if there is enough) and builds the matching Transaction. */
    protected Transaction debit(BigDecimal amount, String description) {
        BigDecimal validAmount = Validation.requireValidAmount(amount, true);
        if (validAmount.compareTo(balance) > 0) {
            throw new BankingException("Insufficient funds. Your balance is BWP " + balance + ".");
        }
        balance = balance.subtract(validAmount);
        return new Transaction(accountNumber, TransactionType.WITHDRAWAL, validAmount, balance, description);
    }

    // ----- Getters -----

    public long getAccountNumber() { return accountNumber; }
    public void setAccountNumber(long accountNumber) { this.accountNumber = accountNumber; } // set by the DAO
    public Customer getOwner() { return owner; }
    public String getBranch() { return branch; }
    public BigDecimal getBalance() { return balance; }

    @Override
    public String toString() {
        return getAccountType().getDisplayName() + " #" + accountNumber + " (BWP " + balance + ")";
    }
}
