package com.qbanking.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/** Tiny helpers to show message boxes. */
public final class Dialogs {

    private Dialogs() {
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Error", message);
    }

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "Qbanking", message);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
