package com.qbanking.controller;

import com.qbanking.dao.AccountDAO;
import com.qbanking.dao.Database;
import com.qbanking.dao.TransactionDAO;
import com.qbanking.model.Account;
import com.qbanking.model.InterestBearing;
import com.qbanking.model.Transaction;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/** What bank staff can do: see all accounts and pay the monthly interest (FR10). */
public class StaffController {

    private final Session session;

    public StaffController(Session session) {
        this.session = session;
    }

    public List<Account> getAllAccounts() {
        session.requireStaff();
        return Database.read(connection -> new AccountDAO(connection).findAll());
    }

    /**
     * Pays this month's interest to every interest-bearing account.
     * An account that already received interest this month is skipped,
     * so pressing the button twice can never pay twice.
     */
    public InterestSummary payMonthlyInterest() {
        session.requireStaff();
        YearMonth month = YearMonth.now();

        return Database.inTransaction(connection -> {
            AccountDAO accountDAO = new AccountDAO(connection);
            TransactionDAO transactionDAO = new TransactionDAO(connection);

            int paid = 0;
            int skipped = 0;
            BigDecimal total = BigDecimal.ZERO;

            for (Account account : accountDAO.findAll()) {
                // Polymorphism: only accounts that implement InterestBearing earn interest.
                if (!(account instanceof InterestBearing interestAccount)) {
                    continue;
                }
                if (transactionDAO.interestPaidInMonth(account.getAccountNumber(), month)) {
                    skipped++;
                    continue;
                }
                Optional<Transaction> interest = interestAccount.payMonthlyInterest();
                if (interest.isEmpty()) {
                    skipped++;
                    continue;
                }
                accountDAO.updateBalance(account);
                transactionDAO.insert(interest.get());
                paid++;
                total = total.add(interest.get().amount());
            }
            return new InterestSummary(month, paid, skipped, total);
        });
    }
}
