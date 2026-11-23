package com.qbanking.view;

import com.qbanking.controller.InterestSummary;
import com.qbanking.controller.Session;
import com.qbanking.controller.StaffController;
import com.qbanking.model.Account;
import com.qbanking.model.BankingException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.Function;

/** Boundary class: the screen for bank staff - see every account and pay monthly interest. */
public class StaffDashboardView {

    private final ViewNavigator navigator;
    private final Session session;
    private final StaffController staffController;

    private final BorderPane root = new BorderPane();
    private final TableView<Account> accountTable = new TableView<>();

    public StaffDashboardView(ViewNavigator navigator, Session session) {
        this.navigator = navigator;
        this.session = session;
        this.staffController = new StaffController(session);

        root.setTop(buildHeader());
        root.setCenter(buildAccountTable());
        root.setBottom(buildButtons());
        refreshAccounts();
    }

    public Parent getRoot() {
        return root;
    }

    private HBox buildHeader() {
        Label brand = new Label("Qbanking");
        brand.getStyleClass().add("brand");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label welcomeLabel = new Label("Welcome, " + session.getDisplayName());

        Button logoutButton = new Button("Log out");
        logoutButton.getStyleClass().add("header-button");
        logoutButton.setOnAction(event -> navigator.showLogin());

        HBox header = new HBox(16, brand, spacer, welcomeLabel, logoutButton);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private TableView<Account> buildAccountTable() {
        accountTable.getColumns().add(column("Account No", 110, a -> String.valueOf(a.getAccountNumber())));
        accountTable.getColumns().add(column("Owner", 190, a -> a.getOwner().getFullName()));
        accountTable.getColumns().add(column("Type", 110, a -> a.getAccountType().getDisplayName()));
        accountTable.getColumns().add(column("Branch", 150, Account::getBranch));
        accountTable.getColumns().add(column("Balance (BWP)", 150, a -> Formats.money(a.getBalance())));

        BorderPane.setMargin(accountTable, new javafx.geometry.Insets(20, 20, 0, 20));
        return accountTable;
    }

    private HBox buildButtons() {
        Button interestButton = new Button("Pay Monthly Interest");
        interestButton.getStyleClass().add("primary-button");
        interestButton.setOnAction(event -> onPayInterest());

        HBox buttons = new HBox(10, interestButton);
        buttons.getStyleClass().add("toolbar");
        return buttons;
    }

    private void onPayInterest() {
        try {
            InterestSummary summary = staffController.payMonthlyInterest();
            Dialogs.info("Interest for " + summary.month() + "\n\n"
                    + "Accounts paid: " + summary.accountsPaid() + "\n"
                    + "Accounts skipped (already paid or 0.00 interest): " + summary.accountsSkipped() + "\n"
                    + "Total interest paid: BWP " + Formats.money(summary.totalPaid()));
        } catch (BankingException e) {
            Dialogs.error(e.getMessage());
        }
        refreshAccounts();
    }

    private void refreshAccounts() {
        try {
            accountTable.setItems(FXCollections.observableArrayList(staffController.getAllAccounts()));
        } catch (BankingException e) {
            Dialogs.error(e.getMessage());
        }
    }

    private TableColumn<Account, String> column(String title, double width, Function<Account, String> valueOf) {
        TableColumn<Account, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(cell -> new SimpleStringProperty(valueOf.apply(cell.getValue())));
        return column;
    }
}
