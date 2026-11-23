package com.qbanking.model;

import java.math.BigDecimal;

/**
 * Small helper methods that check values and throw a {@link BankingException}
 * with a friendly message when something is wrong.
 */
public final class Validation {

    private Validation() {
        // Utility class - nobody should create an object of it.
    }

    /** Makes sure the text is not empty and returns it without leading/trailing spaces. */
    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BankingException(fieldName + " is required.");
        }
        return value.trim();
    }

    /**
     * Turns text typed by the user (e.g. "250.50") into a money amount.
     * Only positive numbers with at most 2 decimal places are accepted.
     */
    public static BigDecimal parseAmount(String text) {
        if (text == null || text.isBlank()) {
            throw new BankingException("Please enter an amount.");
        }
        try {
            return requireValidAmount(new BigDecimal(text.trim()), true);
        } catch (NumberFormatException e) {
            throw new BankingException("'" + text.trim() + "' is not a valid amount. Example: 250.00");
        }
    }

    /**
     * Checks a money amount.
     *
     * @param mustBePositive true  = amount must be greater than zero (deposit, withdrawal)
     *                       false = zero is allowed too (opening balance of a Savings/Cheque account)
     * @return the amount with exactly 2 decimal places
     */
    public static BigDecimal requireValidAmount(BigDecimal amount, boolean mustBePositive) {
        if (amount == null) {
            throw new BankingException("Please enter an amount.");
        }
        if (amount.signum() < 0 || (mustBePositive && amount.signum() == 0)) {
            throw new BankingException(mustBePositive
                    ? "The amount must be greater than zero."
                    : "The amount cannot be negative.");
        }
        if (amount.scale() > 2 && amount.stripTrailingZeros().scale() > 2) {
            throw new BankingException("The amount can have at most 2 decimal places.");
        }
        return amount.setScale(2, java.math.RoundingMode.UNNECESSARY);
    }
}
