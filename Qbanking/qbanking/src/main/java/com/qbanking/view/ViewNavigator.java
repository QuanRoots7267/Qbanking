package com.qbanking.view;

import com.qbanking.controller.Session;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

/** Switches between screens by replacing what is shown inside the one window. */
public class ViewNavigator {

    private final Scene scene;

    public ViewNavigator(Stage stage) {
        this.scene = new Scene(new StackPane(), 960, 640);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/qbanking.css")).toExternalForm());
        stage.setScene(scene);
    }

    public void showLogin() {
        show(new LoginView(this).getRoot());
    }

    public void showRegister() {
        show(new RegisterView(this).getRoot());
    }

    /** Staff and customers get different dashboards. */
    public void showDashboard(Session session) {
        if (session.isStaff()) {
            show(new StaffDashboardView(this, session).getRoot());
        } else {
            show(new CustomerDashboardView(this, session).getRoot());
        }
    }

    private void show(Parent root) {
        scene.setRoot(root);
    }
}
