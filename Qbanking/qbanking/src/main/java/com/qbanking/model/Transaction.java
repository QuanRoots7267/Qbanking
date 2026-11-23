package com.qbanking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A record of one financial event on one account.
 * A Java "record" is an immutable data holder - once created it cannot change,
 * which is exactly what we want for a financial history.
 */
public record Transaction(
        long id,
        long accountNumber,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        LocalDateTime timestamp) {

    /** Used when a new transaction happens now (the database gives it an id later). */
    public Transaction(long accountNumber, TransactionType type, BigDecimal amount,
                       BigDecimal balanceAfter, String description) {
        this(0, accountNumber, type, amount, balanceAfter, description, LocalDateTime.now());
    }
}
