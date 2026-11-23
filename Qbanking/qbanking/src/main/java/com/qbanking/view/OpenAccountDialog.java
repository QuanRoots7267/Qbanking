package com.qbanking.view;

import com.qbanking.controller.AccountController;
import com.qbanking.model.AccountType;
import com.qbanking.model.BankingException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/** Boundary class: the form for opening a new Savings, Investment or Cheque account. */
public class OpenAccountDialog {

    private final AccountController controller;
    private boolean accountOpened = false;

    public OpenAccountDialog(AccountController controller) {
        this.controller = controller;
    }

    /** Shows the form. Returns true if an account was opened. */
    public boolean showAndWait() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Open a New Account");
        dialog.setHeaderText("Choose the account type and fill in the details");

        ButtonType openButtonType = new ButtonType("Open Account", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(openButtonType, ButtonType.CANCEL);

        ComboBox<AccountType> typeBox = new ComboBox<>(FXCollections.observableArrayList(AccountType.values()));
        typeBox.getSelectionModel().selectFirst();

        TextField branchField = new TextField("Gaborone Main");
        TextField depositField = new TextField();
        depositField.setPromptText("0.00");
        TextField employerNameField = new TextField();
        TextField employerAddressField = new TextField();

        // Employer details are only needed (and only enabled) for a Cheque account.
        employerNameField.disableProperty().bind(typeBox.valueProperty().isNotEqualTo(AccountType.CHEQUE));
        employerAddressField.disableProperty().bind(typeBox.valueProperty().isNotEqualTo(AccountType.CHEQUE));

        Label rulesLabel = new Label(rulesFor(typeBox.getValue()));
        rulesLabel.setWrapText(true);
        rulesLabel.getStyleClass().add("hint-label");
        typeBox.valueProperty().addListener((observable, oldType, newType) -> rulesLabel.setText(rulesFor(newType)));

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.add(new Label("Account type"), 0, 0);
        form.add(typeBox, 1, 0);
        form.add(rulesLabel, 0, 1, 2, 1);
        form.add(new Label("Branch"), 0, 2);
        form.add(branchField, 1, 2);
        form.add(new Label("Opening deposit (BWP)"), 0, 3);
        form.add(depositField, 1, 3);
        form.add(new Label("Employer name"), 0, 4);
        form.add(employerNameField, 1, 4);
        form.add(new Label("Employer address"), 0, 5);
        form.add(employerAddressField, 1, 5);
        form.add(errorLabel, 0, 6, 2, 1);
        form.setPrefWidth(460);
        dialog.getDialogPane().setContent(form);

        // When "Open Account" is pressed: try it. If the controller reports a problem,
        // show it inside the form and keep the dialog open (consume the event).
        Button openButton = (Button) dialog.getDialogPane().lookupButton(openButtonType);
        openButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                controller.openAccount(typeBox.getValue(), branchField.getText(), depositField.getText(),
                        employerNameField.getText(), employerAddressField.getText());
                accountOpened = true;
            } catch (BankingException e) {
                errorLabel.setText(e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
        return accountOpened;
    }

    /** Short explanation of each account type, shown above the form. */
    private String rulesFor(AccountType type) {
        return switch (type) {
            case SAVINGS -> "Savings: earns 0.05% interest per month. Deposits only - no withdrawals.";
            case INVESTMENT -> "Investment: earns 5% interest per month. Deposits and withdrawals allowed. "
                    + "Minimum opening deposit BWP 500.00.";
            case CHEQUE -> "Cheque: for salaries. Deposits and withdrawals allowed. "
                    + "Only for working people - employer details are required.";
        };
    }
}
