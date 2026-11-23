package com.qbanking.view;

import com.qbanking.controller.AccountController;
import com.qbanking.controller.CustomerController;
import com.qbanking.controller.Session;
import com.qbanking.model.Account;
import com.qbanking.model.BankingException;
import com.qbanking.model.ChequeAccount;
import com.qbanking.model.InterestBearing;
import com.qbanking.model.Transaction;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Boundary class: the main screen for a logged-in customer.
 * Shows the customer's accounts and the buttons for what they can do.
 * All real work is done by the controllers.
 */
public class CustomerDashboardView {

    private final ViewNavigator navigator;
    private final Session session;
    private final AccountController accountController;
    private final CustomerController customerController;

    private final BorderPane root = new BorderPane();
    private final TableView<Account> accountTable = new TableView<>();
    private final Label welcomeLabel = new Label();

    public CustomerDashboardView(ViewNavigator navigator, Session session) {
        this.navigator = navigator;
        this.session = session;
        this.accountController = new AccountController(session);
        this.customerController = new CustomerController(session);

        root.setTop(buildHeader());
        root.setCenter(buildAccountTable());
        root.setBottom(buildButtons());
        refreshAccounts();
    }

    public Parent getRoot() {
        return root;
    }

    // ------------------------------------------------------------ screen parts

    private HBox buildHeader() {
        Label brand = new Label("Qbanking");
        brand.getStyleClass().add("brand");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Log out");
        logoutButton.getStyleClass().add("header-button");
        logoutButton.setOnAction(event -> navigator.showLogin());

        HBox header = new HBox(16, brand, spacer, welcomeLabel, logoutButton);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        updateWelcomeText();
        return header;
    }

    private TableView<Account> buildAccountTable() {
        accountTable.getColumns().add(column("Account No", 120,
                account -> String.valueOf(account.getAccountNumber())));
        accountTable.getColumns().add(column("Type", 110,
                account -> account.getAccountType().getDisplayName()));
        accountTable.getColumns().add(column("Branch", 150, Account::getBranch));
        accountTable.getColumns().add(column("Balance (BWP)", 150,
                account -> Formats.money(account.getBalance())));
        accountTable.getColumns().add(column("Details", 260, this::describe));
        accountTable.setPlaceholder(new Label("You have no accounts yet. Click \"Open Account\"."));

        BorderPane.setMargin(accountTable, new javafx.geometry.Insets(20, 20, 0, 20));
        return accountTable;
    }

    private HBox buildButtons() {
        Button openButton = actionButton("Open Account", this::onOpenAccount);
        Button depositButton = actionButton("Deposit", () -> onAmountAction("Deposit", accountController::deposit));
        Button withdrawButton = actionButton("Withdraw", () -> onAmountAction("Withdraw", accountController::withdraw));
        Button historyButton = actionButton("Transaction History", this::onShowHistory);
        Button detailsButton = actionButton("My Details", this::onEditDetails);

        // These buttons only make sense when an account is selected in the table.
        for (Button button : new Button[]{depositButton, withdrawButton, historyButton}) {
            button.disableProperty().bind(accountTable.getSelectionModel().selectedItemProperty().isNull());
        }

        HBox buttons = new HBox(10, openButton, depositButton, withdrawButton, historyButton, detailsButton);
        buttons.getStyleClass().add("toolbar");
        return buttons;
    }

    // ----------------------------------------------------------- button actions

    private void onOpenAccount() {
        new OpenAccountDialog(accountController).showAndWait();
        refreshAccounts();
    }

    /** Shared by Deposit and Withdraw: ask for an amount, then call the controller. */
    private void onAmountAction(String title, BiFunction<Long, String, Transaction> action) {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(title + " - " + selected.getAccountType().getDisplayName()
                + " account " + selected.getAccountNumber());
        dialog.setContentText("Amount (BWP):");

        Optional<String> answer = dialog.showAndWait();
        if (answer.isEmpty()) {
            return;     // user pressed Cancel
        }
        try {
            Transaction transaction = action.apply(selected.getAccountNumber(), answer.get());
            Dialogs.info(title + " successful.\nNew balance: BWP " + Formats.money(transaction.balanceAfter()));
        } catch (BankingException e) {
            Dialogs.error(e.getMessage());
        }
        refreshAccounts();
    }

    private void onShowHistory() {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TransactionHistoryDialog.show(accountController, selected);
        }
    }

    private void onEditDetails() {
        new ProfileDialog(customerController).showAndWait();
        updateWelcomeText();
    }

    // ----------------------------------------------------------------- helpers

    private void refreshAccounts() {
        try {
            accountTable.setItems(FXCollections.observableArrayList(accountController.getMyAccounts()));
        } catch (BankingException e) {
            Dialogs.error(e.getMessage());
        }
    }

    private void updateWelcomeText() {
        welcomeLabel.setText("Welcome, " + session.getDisplayName());
    }

    /** Text for the "Details" column. */
    private String describe(Account account) {
        if (account instanceof InterestBearing interestAccount) {
            return interestAccount.getRateAsPercentText() + "% interest per month";
        }
        if (account instanceof ChequeAccount cheque) {
            return "Employer: " + cheque.getEmployerName();
        }
        return "";
    }

    private Button actionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        button.setOnAction(event -> action.run());
        return button;
    }

    private TableColumn<Account, String> column(String title, double width,
                                                java.util.function.Function<Account, String> valueOf) {
        TableColumn<Account, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new SimpleStringProperty(valueOf.apply(cell.getValue())));
        return column;
    }
}
