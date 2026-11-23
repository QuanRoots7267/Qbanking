package com.qbanking.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Interface (contract) for accounts that earn interest.
 * Savings and Investment accounts implement it; the Cheque account does not.
 */
public interface InterestBearing {

    /** Monthly rate as a fraction, e.g. 0.05 means 5%. */
    BigDecimal getMonthlyInterestRate();

    /** Current balance (every Account already has this method). */
    BigDecimal getBalance();

    /** Adds this month's interest to the account. Empty if the interest rounds to 0.00. */
    Optional<Transaction> payMonthlyInterest();

    /** Default method: shared by all implementers, so it is written only once. */
    default BigDecimal calculateMonthlyInterest() {
        return getBalance()
                .multiply(getMonthlyInterestRate())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Rate as readable text, e.g. "5" or "0.05". */
    default String getRateAsPercentText() {
        return getMonthlyInterestRate()
                .multiply(BigDecimal.valueOf(100))
                .stripTrailingZeros()
                .toPlainString();
    }
}
