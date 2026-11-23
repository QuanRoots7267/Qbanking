package com.qbanking.view;

import com.qbanking.controller.CustomerController;
import com.qbanking.model.BankingException;
import com.qbanking.model.Customer;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/** Boundary class: lets the customer change their name and address. */
public class ProfileDialog {

    private final CustomerController controller;

    public ProfileDialog(CustomerController controller) {
        this.controller = controller;
    }

    public void showAndWait() {
        Customer customer = controller.getMyDetails();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("My Details");
        dialog.setHeaderText("Update your personal details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField firstNameField = new TextField(customer.getFirstName());
        TextField surnameField = new TextField(customer.getSurname());
        TextField addressField = new TextField(customer.getAddress());

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.add(new Label("First name"), 0, 0);
        form.add(firstNameField, 1, 0);
        form.add(new Label("Surname"), 0, 1);
        form.add(surnameField, 1, 1);
        form.add(new Label("Address"), 0, 2);
        form.add(addressField, 1, 2);
        form.add(errorLabel, 0, 3, 2, 1);
        form.setPrefWidth(420);
        dialog.getDialogPane().setContent(form);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                controller.updateMyDetails(firstNameField.getText(), surnameField.getText(), addressField.getText());
            } catch (BankingException e) {
                errorLabel.setText(e.getMessage());
                event.consume();      // keep the dialog open so the user can fix the input
            }
        });

        dialog.showAndWait();
    }
}
