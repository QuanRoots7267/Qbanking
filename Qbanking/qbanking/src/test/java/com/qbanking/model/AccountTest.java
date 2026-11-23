package com.qbanking.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the business rules inside the model classes (no database, no GUI). */
class AccountTest {

    private final Customer customer = new Customer("Test", "Person", "Gaborone");

    private static BigDecimal bwp(String amount) {
        return new BigDecimal(amount);
    }

    // ----------------------------------------------------------------- Savings

    @Test
    void savingsDepositIncreasesBalanceAndReturnsTransaction() {
        SavingsAccount savings = new SavingsAccount(customer, "Main", bwp("100"));

        Transaction transaction = savings.deposit(bwp("50.25"));

        assertEquals(bwp("150.25"), savings.getBalance());
        assertEquals(TransactionType.DEPOSIT, transaction.type());
        assertEquals(bwp("150.25"), transaction.balanceAfter());
    }

    @Test
    void savingsDoesNotAllowWithdrawals() {
        SavingsAccount savings = new SavingsAccount(customer, "Main", bwp("100"));

        BankingException error = assertThrows(BankingException.class, () -> savings.withdraw(bwp("10")));

        assertTrue(error.getMessage().contains("do not allow withdrawals"));
        assertEquals(bwp("100.00"), savings.getBalance());
    }

    @Test
    void savingsPaysPointZeroFivePercentMonthlyInterest() {
        SavingsAccount savings = new SavingsAccount(customer, "Main", bwp("2000"));

        Transaction interest = savings.payMonthlyInterest().orElseThrow();

        assertEquals(bwp("1.00"), interest.amount());          // 0.05% of 2000
        assertEquals(bwp("2001.00"), savings.getBalance());
        assertEquals(TransactionType.INTEREST, interest.type());
    }

    // -------------------------------------------------------------- Investment

    @Test
    void investmentNeedsAtLeastFiveHundredToOpen() {
        assertThrows(BankingException.class, () -> new InvestmentAccount(customer, "Main", bwp("499.99")));
        InvestmentAccount account = new InvestmentAccount(customer, "Main", bwp("500"));
        assertEquals(bwp("500.00"), account.getBalance());
    }

    @Test
    void investmentPaysFivePercentMonthlyInterest() {
        InvestmentAccount investment = new InvestmentAccount(customer, "Main", bwp("1000"));

        Transaction interest = investment.payMonthlyInterest().orElseThrow();

        assertEquals(bwp("50.00"), interest.amount());          // 5% of 1000
        assertEquals(bwp("1050.00"), investment.getBalance());
    }

    @Test
    void investmentAllowsWithdrawalButNotMoreThanBalance() {
        InvestmentAccount investment = new InvestmentAccount(customer, "Main", bwp("1000"));

        investment.withdraw(bwp("400"));
        assertEquals(bwp("600.00"), investment.getBalance());

        BankingException error = assertThrows(BankingException.class, () -> investment.withdraw(bwp("600.01")));
        assertTrue(error.getMessage().contains("Insufficient funds"));
        assertEquals(bwp("600.00"), investment.getBalance());
    }

    // ------------------------------------------------------------------ Cheque

    @Test
    void chequeAccountRequiresEmployerDetails() {
        assertThrows(BankingException.class,
                () -> new ChequeAccount(customer, "Main", bwp("0"), "", "Gaborone"));
        assertThrows(BankingException.class,
                () -> new ChequeAccount(customer, "Main", bwp("0"), "Acme", " "));
    }

    @Test
    void chequeAccountAllowsDepositsAndWithdrawals() {
        ChequeAccount cheque = new ChequeAccount(customer, "Main", bwp("0"), "Acme", "Gaborone");

        cheque.deposit(bwp("5000"), "Salary");
        Transaction withdrawal = cheque.withdraw(bwp("1250.50"));

        assertEquals(bwp("3749.50"), cheque.getBalance());
        assertEquals(TransactionType.WITHDRAWAL, withdrawal.type());
    }

    @Test
    void chequeAccountDoesNotEarnInterest() {
        ChequeAccount cheque = new ChequeAccount(customer, "Main", bwp("100"), "Acme", "Gaborone");
        assertFalse(((Object) cheque) instanceof InterestBearing);
    }

    // --------------------------------------------------------- Shared rules

    @Test
    void accountCannotExistWithoutACustomer() {
        assertThrows(BankingException.class, () -> new SavingsAccount(null, "Main", bwp("10")));
    }

    @Test
    void amountsMustBePositiveWithAtMostTwoDecimals() {
        SavingsAccount savings = new SavingsAccount(customer, "Main", bwp("100"));

        assertThrows(BankingException.class, () -> savings.deposit(bwp("0")));
        assertThrows(BankingException.class, () -> savings.deposit(bwp("-5")));
        assertThrows(BankingException.class, () -> savings.deposit(bwp("1.005")));
        assertEquals(bwp("100.00"), savings.getBalance());
    }

    @Test
    void polymorphismAnAccountReferenceBehavesAccordingToItsRealType() {
        Account[] accounts = {
                new SavingsAccount(customer, "Main", bwp("1000")),
                new InvestmentAccount(customer, "Main", bwp("1000")),
                new ChequeAccount(customer, "Main", bwp("1000"), "Acme", "Gaborone")
        };

        int withdrawalsAllowed = 0;
        for (Account account : accounts) {
            try {
                account.withdraw(bwp("100"));      // same call, different behaviour
                withdrawalsAllowed++;
            } catch (BankingException expected) {
                assertEquals(AccountType.SAVINGS, account.getAccountType());
            }
        }
        assertEquals(2, withdrawalsAllowed);
    }

    @Test
    void passwordHasherAcceptsOnlyTheCorrectPassword() {
        String stored = com.qbanking.util.PasswordHasher.hash("Secret123");

        assertTrue(com.qbanking.util.PasswordHasher.matches("Secret123", stored));
        assertFalse(com.qbanking.util.PasswordHasher.matches("secret123", stored));
    }
}
