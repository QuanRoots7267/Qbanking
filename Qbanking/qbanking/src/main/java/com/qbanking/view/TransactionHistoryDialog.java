package com.qbanking.view;

import com.qbanking.controller.AccountController;
import com.qbanking.model.Account;
import com.qbanking.model.BankingException;
import com.qbanking.model.Transaction;
import com.qbanking.model.TransactionType;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.function.Function;

/** Boundary class: a pop-up table listing the transactions of one account (newest first). */
public final class TransactionHistoryDialog {

    private TransactionHistoryDialog() {
    }

    public static void show(AccountController controller, Account account) {
        List<Transaction> transactions;
        try {
            transactions = controller.getTransactions(account.getAccountNumber());
        } catch (BankingException e) {
            Dialogs.error(e.getMessage());
            return;
        }

        TableView<Transaction> table = new TableView<>(FXCollections.observableArrayList(transactions));
        table.getColumns().add(column("Date", 140, t -> t.timestamp().format(Formats.DATE_TIME)));
        table.getColumns().add(column("Type", 100, t -> t.type().name()));
        table.getColumns().add(column("Description", 200, Transaction::description));
        table.getColumns().add(column("Amount (BWP)", 120, t -> signedAmount(t)));
        table.getColumns().add(column("Balance after", 120, t -> Formats.money(t.balanceAfter())));
        table.setPlaceholder(new Label("No transactions yet."));
        table.setPrefSize(720, 380);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Transaction History");
        dialog.setHeaderText(account.getAccountType().getDisplayName() + " account " + account.getAccountNumber());
        dialog.getDialogPane().setContent(table);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /** Withdrawals are shown with a minus sign, everything else with a plus. */
    private static String signedAmount(Transaction transaction) {
        String sign = transaction.type() == TransactionType.WITHDRAWAL ? "-" : "+";
        return sign + Formats.money(transaction.amount());
    }

    private static TableColumn<Transaction, String> column(String title, double width,
                                                           Function<Transaction, String> valueOf) {
        TableColumn<Transaction, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new SimpleStringProperty(valueOf.apply(cell.getValue())));
        return column;
    }
}
