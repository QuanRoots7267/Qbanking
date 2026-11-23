package com.qbanking.view;

import com.qbanking.controller.AuthController;
import com.qbanking.model.BankingException;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Boundary class: new customer registration screen. */
public class RegisterView {

    private final ViewNavigator navigator;
    private final AuthController authController = new AuthController();

    private final StackPane root = new StackPane();
    private final TextField firstNameField = new TextField();
    private final TextField surnameField = new TextField();
    private final TextField addressField = new TextField();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmField = new PasswordField();
    private final Label messageLabel = new Label();

    public RegisterView(ViewNavigator navigator) {
        this.navigator = navigator;
        buildScreen();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildScreen() {
        Label title = new Label("Create your Qbanking profile");
        title.getStyleClass().add("title");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        addRow(form, 0, "First name", firstNameField);
        addRow(form, 1, "Surname", surnameField);
        addRow(form, 2, "Address", addressField);
        addRow(form, 3, "Username", usernameField);
        addRow(form, 4, "Password", passwordField);
        addRow(form, 5, "Confirm password", confirmField);

        Label hint = new Label("Username: 4-20 letters/digits/underscores. "
                + "Password: at least 8 characters with a letter and a digit.");
        hint.getStyleClass().add("hint-label");
        hint.setWrapText(true);

        messageLabel.getStyleClass().add("error-label");
        messageLabel.setWrapText(true);

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("primary-button");
        registerButton.setDefaultButton(true);
        registerButton.setOnAction(event -> onRegister());

        Button backButton = new Button("Back to login");
        backButton.getStyleClass().add("link-button");
        backButton.setOnAction(event -> navigator.showLogin());

        HBox buttons = new HBox(16, registerButton, backButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14, title, form, hint, messageLabel, buttons);
        card.getStyleClass().add("card");
        card.setMaxWidth(480);
        card.setMaxHeight(VBox.USE_PREF_SIZE);

        root.getChildren().add(card);
    }

    private void addRow(GridPane form, int row, String labelText, javafx.scene.Node field) {
        form.add(new Label(labelText), 0, row);
        form.add(field, 1, row);
        GridPane.setHgrow(field, javafx.scene.layout.Priority.ALWAYS);
    }

    private void onRegister() {
        try {
            authController.register(firstNameField.getText(), surnameField.getText(), addressField.getText(),
                    usernameField.getText(), passwordField.getText(), confirmField.getText());
            Dialogs.info("Registration successful! You can now log in.");
            navigator.showLogin();
        } catch (BankingException e) {
            messageLabel.setText(e.getMessage());
        }
    }
}
