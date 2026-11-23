package com.qbanking.integration;

import com.qbanking.controller.AccountController;
import com.qbanking.controller.AuthController;
import com.qbanking.controller.CustomerController;
import com.qbanking.controller.InterestSummary;
import com.qbanking.controller.Session;
import com.qbanking.controller.StaffController;
import com.qbanking.dao.CustomerDAO;
import com.qbanking.dao.Database;
import com.qbanking.dao.DatabaseInitializer;
import com.qbanking.model.Account;
import com.qbanking.model.AccountType;
import com.qbanking.model.BankingException;
import com.qbanking.model.Transaction;
import com.qbanking.model.TransactionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests: the Controllers, the Model and the real JDBC/DAO layer working
 * together against a temporary in-memory H2 database.
 * (The JavaFX screens only call these same controller methods, so this covers the
 * full path: View -> Controller -> Model -> DAO -> Database.)
 */
class BankingIntegrationTest {

    private final AuthController authController = new AuthController();

    @BeforeAll
    static void createFreshDatabase() {
        Database.useUrl("jdbc:h2:mem:qbanking_test;DB_CLOSE_DELAY=-1");
        DatabaseInitializer.initialise();
    }

    private static BigDecimal bwp(String amount) {
        return new BigDecimal(amount);
    }

    // --------------------------------------------------------------- database

    @Test
    void sampleDataContainsAtLeastTenCustomers() {
        int customers = Database.read(connection -> new CustomerDAO(connection).count());
        assertTrue(customers >= 10, "Expected at least 10 sample customers but found " + customers);
    }

    // --------------------------------------------------- register / login flow

    @Test
    void customerCanRegisterLoginOpenAllThreeAccountsDepositAndWithdraw() {
        // 1. Register and log in
        authController.register("Itest", "Flow", "Plot 1, Gaborone", "itest_flow", "Passw0rd!", "Passw0rd!");
        Session session = authController.login("itest_flow", "Passw0rd!");
        AccountController accounts = new AccountController(session);

        // 2. Open one account of each type (a customer can hold multiple accounts)
        Account savings = accounts.openAccount(AccountType.SAVINGS, "Gaborone Main", "200.00", "", "");
        Account investment = accounts.openAccount(AccountType.INVESTMENT, "Gaborone Main", "500.00", "", "");
        Account cheque = accounts.openAccount(AccountType.CHEQUE, "Gaborone Main", "", "Acme Ltd", "Gaborone");
        assertEquals(3, accounts.getMyAccounts().size());

        // 3. Deposit and withdraw, then check the balances that were saved in the database
        accounts.deposit(savings.getAccountNumber(), "100.50");
        accounts.deposit(cheque.getAccountNumber(), "5000");
        accounts.withdraw(cheque.getAccountNumber(), "1200.25");
        accounts.withdraw(investment.getAccountNumber(), "100");

        List<Account> saved = accounts.getMyAccounts();
        assertEquals(bwp("300.50"), balanceOf(saved, savings));
        assertEquals(bwp("3799.75"), balanceOf(saved, cheque));
        assertEquals(bwp("400.00"), balanceOf(saved, investment));

        // 4. Transaction history was recorded (newest first)
        List<Transaction> history = accounts.getTransactions(cheque.getAccountNumber());
        assertEquals(2, history.size());   // the deposit and the withdrawal (opening deposit was 0, so none recorded)
        assertEquals(TransactionType.WITHDRAWAL, history.get(0).type());
        assertEquals(TransactionType.DEPOSIT, history.get(1).type());
    }

    @Test
    void loginFailsWithWrongPasswordOrUnknownUser() {
        assertThrows(BankingException.class, () -> authController.login("masego", "WrongPassword1"));
        assertThrows(BankingException.class, () -> authController.login("nobody_here", DatabaseInitializer.SAMPLE_PASSWORD));
    }

    @Test
    void duplicateUsernamesAndWeakPasswordsAreRejected() {
        assertThrows(BankingException.class,
                () -> authController.register("A", "B", "Addr", "masego", "Passw0rd!", "Passw0rd!"));
        assertThrows(BankingException.class,
                () -> authController.register("A", "B", "Addr", "weakpw_user", "short", "short"));
    }

    // -------------------------------------------------------- business rules

    @Test
    void savingsWithdrawalIsRefusedAndNothingChanges() {
        Session session = authController.login("kabelo", DatabaseInitializer.SAMPLE_PASSWORD);
        AccountController accounts = new AccountController(session);
        Account savings = firstOfType(accounts.getMyAccounts(), AccountType.SAVINGS);
        BigDecimal before = savings.getBalance();
        int historyBefore = accounts.getTransactions(savings.getAccountNumber()).size();

        assertThrows(BankingException.class, () -> accounts.withdraw(savings.getAccountNumber(), "10"));

        Account reloaded = firstOfType(accounts.getMyAccounts(), AccountType.SAVINGS);
        assertEquals(before, reloaded.getBalance());
        assertEquals(historyBefore, accounts.getTransactions(savings.getAccountNumber()).size());
    }

    @Test
    void investmentAccountNeedsFiveHundredToOpenAndCannotBeOverdrawn() {
        authController.register("Itest", "Invest", "Plot 2, Gaborone", "itest_invest", "Passw0rd!", "Passw0rd!");
        AccountController accounts = new AccountController(authController.login("itest_invest", "Passw0rd!"));

        assertThrows(BankingException.class,
                () -> accounts.openAccount(AccountType.INVESTMENT, "Main", "499.99", "", ""));

        Account investment = accounts.openAccount(AccountType.INVESTMENT, "Main", "500", "", "");
        assertThrows(BankingException.class, () -> accounts.withdraw(investment.getAccountNumber(), "500.01"));
    }

    @Test
    void chequeAccountNeedsEmployerDetails() {
        authController.register("Itest", "Cheque", "Plot 3, Gaborone", "itest_cheque", "Passw0rd!", "Passw0rd!");
        AccountController accounts = new AccountController(authController.login("itest_cheque", "Passw0rd!"));

        assertThrows(BankingException.class,
                () -> accounts.openAccount(AccountType.CHEQUE, "Main", "100", "", ""));
    }

    @Test
    void invalidAmountsAreRejected() {
        Session session = authController.login("neo", DatabaseInitializer.SAMPLE_PASSWORD);
        AccountController accounts = new AccountController(session);
        long number = accounts.getMyAccounts().get(0).getAccountNumber();

        assertThrows(BankingException.class, () -> accounts.deposit(number, "abc"));
        assertThrows(BankingException.class, () -> accounts.deposit(number, "-20"));
        assertThrows(BankingException.class, () -> accounts.deposit(number, "10.999"));
    }

    // -------------------------------------------------------------- security

    @Test
    void customerCannotTouchAnotherCustomersAccount() {
        AccountController masego = new AccountController(authController.login("masego", DatabaseInitializer.SAMPLE_PASSWORD));
        long masegosAccount = masego.getMyAccounts().get(0).getAccountNumber();

        AccountController thabo = new AccountController(authController.login("thabo", DatabaseInitializer.SAMPLE_PASSWORD));

        assertThrows(BankingException.class, () -> thabo.deposit(masegosAccount, "10"));
        assertThrows(BankingException.class, () -> thabo.getTransactions(masegosAccount));
    }

    @Test
    void customerCannotPayMonthlyInterest() {
        Session customerSession = authController.login("lesedi", DatabaseInitializer.SAMPLE_PASSWORD);
        assertThrows(BankingException.class, () -> new StaffController(customerSession).payMonthlyInterest());
    }

    @Test
    void customerCanUpdateTheirDetails() {
        Session session = authController.login("tshepo", DatabaseInitializer.SAMPLE_PASSWORD);
        new CustomerController(session).updateMyDetails("Tshepo", "Seretse-Moyo", "New Address, Lobatse");

        Session loginAgain = authController.login("tshepo", DatabaseInitializer.SAMPLE_PASSWORD);
        assertEquals("Seretse-Moyo", loginAgain.requireCustomer().getSurname());
        assertEquals("New Address, Lobatse", loginAgain.requireCustomer().getAddress());
    }

    // -------------------------------------------------------------- interest

    @Test
    void staffPaysMonthlyInterestOnlyOncePerMonth() {
        // Remember Masego's savings balance before interest
        AccountController masego = new AccountController(authController.login("masego", DatabaseInitializer.SAMPLE_PASSWORD));
        Account savingsBefore = firstOfType(masego.getMyAccounts(), AccountType.SAVINGS);
        BigDecimal expectedInterest = savingsBefore.getBalance().multiply(bwp("0.0005")).setScale(2, RoundingMode.HALF_UP);

        StaffController staff = new StaffController(
                authController.login(DatabaseInitializer.STAFF_USERNAME, DatabaseInitializer.STAFF_PASSWORD));

        InterestSummary first = staff.payMonthlyInterest();
        assertTrue(first.accountsPaid() > 0);
        assertTrue(first.totalPaid().signum() > 0);

        Account savingsAfter = firstOfType(masego.getMyAccounts(), AccountType.SAVINGS);
        assertEquals(savingsBefore.getBalance().add(expectedInterest), savingsAfter.getBalance());

        // Pressing the button again in the same month must not pay again
        InterestSummary second = staff.payMonthlyInterest();
        assertEquals(0, second.accountsPaid());
        assertEquals(savingsAfter.getBalance(), firstOfType(masego.getMyAccounts(), AccountType.SAVINGS).getBalance());
    }

    // ---------------------------------------------------------------- helpers

    private static BigDecimal balanceOf(List<Account> accounts, Account wanted) {
        return accounts.stream()
                .filter(account -> account.getAccountNumber() == wanted.getAccountNumber())
                .findFirst().orElseThrow()
                .getBalance();
    }

    private static Account firstOfType(List<Account> accounts, AccountType type) {
        return accounts.stream()
                .filter(account -> account.getAccountType() == type)
                .findFirst().orElseThrow();
    }
}
