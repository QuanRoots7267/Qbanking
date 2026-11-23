package com.qbanking.model;

import java.math.BigDecimal;

/**
 * Used for salaries. Deposits and withdrawals of any amount are allowed (no interest).
 * Only a working person can open one, so the employer's name and address are required.
 */
public class ChequeAccount extends Account {

    private final String employerName;
    private final String employerAddress;

    /** New account. */
    public ChequeAccount(Customer owner, String branch, BigDecimal openingDeposit,
                         String employerName, String employerAddress) {
        super(owner, branch, openingDeposit);
        this.employerName = Validation.requireText(employerName, "Employer name (required for a Cheque account)");
        this.employerAddress = Validation.requireText(employerAddress, "Employer address (required for a Cheque account)");
    }

    /** Existing account loaded from the database. */
    public ChequeAccount(long accountNumber, Customer owner, String branch, BigDecimal balance,
                         String employerName, String employerAddress) {
        super(accountNumber, owner, branch, balance);
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    @Override
    public Transaction withdraw(BigDecimal amount) {
        return debit(amount, "Withdrawal");
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.CHEQUE;
    }

    public String getEmployerName() { return employerName; }
    public String getEmployerAddress() { return employerAddress; }
}
