package com.qbanking.model;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * INHERITANCE: a SavingsAccount IS-A Account.
 * It also implements the InterestBearing interface.
 *
 * Rules: deposits allowed, NO withdrawals, 0.05% interest every month.
 */
public class SavingsAccount extends Account implements InterestBearing {

    private static final BigDecimal MONTHLY_RATE = new BigDecimal("0.0005"); // 0.05%

    /** New account. */
    public SavingsAccount(Customer owner, String branch, BigDecimal openingDeposit) {
        super(owner, branch, openingDeposit);
    }

    /** Existing account loaded from the database. */
    public SavingsAccount(long accountNumber, Customer owner, String branch, BigDecimal balance) {
        super(accountNumber, owner, branch, balance);
    }

    @Override
    public Transaction withdraw(BigDecimal amount) {
        throw new BankingException("Savings accounts do not allow withdrawals.");
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }

    @Override
    public BigDecimal getMonthlyInterestRate() {
        return MONTHLY_RATE;
    }

    @Override
    public Optional<Transaction> payMonthlyInterest() {
        BigDecimal interest = calculateMonthlyInterest();
        if (interest.signum() == 0) {
            return Optional.empty();
        }
        return Optional.of(credit(interest, TransactionType.INTEREST,
                "Monthly interest (" + getRateAsPercentText() + "%)"));
    }
}
