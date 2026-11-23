package com.qbanking.view;

import com.qbanking.controller.AuthController;
import com.qbanking.controller.Session;
import com.qbanking.model.BankingException;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Boundary class: the login screen.
 * It only shows controls and passes what the user typed to the AuthController.
 */
public class LoginView {

    private final ViewNavigator navigator;
    private final AuthController authController = new AuthController();

    private final StackPane root = new StackPane();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label messageLabel = new Label();

    public LoginView(ViewNavigator navigator) {
        this.navigator = navigator;
        buildScreen();
    }

    public Parent getRoot() {
        return root;
    }

    private void buildScreen() {
        Label title = new Label("Qbanking");
        title.getStyleClass().add("title");
        Label subtitle = new Label("Sign in to your account");
        subtitle.getStyleClass().add("subtitle");

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        passwordField.setOnAction(event -> onLogin());       // Enter key logs in

        messageLabel.getStyleClass().add("error-label");
        messageLabel.setWrapText(true);

        Button loginButton = new Button("Log In");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(event -> onLogin());

        Button registerButton = new Button("New customer? Register here");
        registerButton.getStyleClass().add("link-button");
        registerButton.setOnAction(event -> navigator.showRegister());

        VBox card = new VBox(12, title, subtitle, usernameField, passwordField,
                messageLabel, loginButton, registerButton);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(360);
        card.setMaxHeight(VBox.USE_PREF_SIZE);

        root.getChildren().add(card);
    }

    private void onLogin() {
        try {
            Session session = authController.login(usernameField.getText(), passwordField.getText());
            navigator.showDashboard(session);
        } catch (BankingException e) {
            messageLabel.setText(e.getMessage());
            passwordField.clear();
        }
    }
}
