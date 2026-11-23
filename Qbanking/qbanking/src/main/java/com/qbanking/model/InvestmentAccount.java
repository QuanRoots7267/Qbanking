package com.qbanking.model;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Rules: deposits and withdrawals allowed, 5% interest every month,
 * and the account can only be opened with at least BWP 500.00.
 */
public class InvestmentAccount extends Account implements InterestBearing {

    public static final BigDecimal MINIMUM_OPENING_DEPOSIT = new BigDecimal("500.00");
    private static final BigDecimal MONTHLY_RATE = new BigDecimal("0.05"); // 5%

    /** New account: the opening deposit is checked BEFORE the account is created. */
    public InvestmentAccount(Customer owner, String branch, BigDecimal openingDeposit) {
        super(owner, branch, requireMinimum(openingDeposit));
    }

    /** Existing account loaded from the database (no opening-deposit check needed). */
    public InvestmentAccount(long accountNumber, Customer owner, String branch, BigDecimal balance) {
        super(accountNumber, owner, branch, balance);
    }

    private static BigDecimal requireMinimum(BigDecimal openingDeposit) {
        if (openingDeposit == null || openingDeposit.compareTo(MINIMUM_OPENING_DEPOSIT) < 0) {
            throw new BankingException("An Investment account needs an opening deposit of at least BWP "
                    + MINIMUM_OPENING_DEPOSIT + ".");
        }
        return openingDeposit;
    }

    @Override
    public Transaction withdraw(BigDecimal amount) {
        return debit(amount, "Withdrawal");
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.INVESTMENT;
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
