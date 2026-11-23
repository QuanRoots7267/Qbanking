package com.qbanking.model;

/** The three kinds of account the bank offers. */
public enum AccountType {
    SAVINGS("Savings"),
    INVESTMENT("Investment"),
    CHEQUE("Cheque");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName; // used by ComboBox in the GUI
    }
}
