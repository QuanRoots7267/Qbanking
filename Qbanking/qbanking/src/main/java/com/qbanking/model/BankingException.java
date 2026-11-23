package com.qbanking.model;

/**
 * The one exception type used for every "business rule" or "bad input" problem
 * (for example: insufficient funds, invalid amount, wrong password).
 *
 * The View catches it and shows {@link #getMessage()} to the user.
 */
public class BankingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BankingException(String message) {
        super(message);
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}
