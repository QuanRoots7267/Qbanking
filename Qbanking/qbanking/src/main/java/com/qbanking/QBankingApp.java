package com.qbanking;

import com.qbanking.dao.DatabaseInitializer;
import com.qbanking.view.Dialogs;
import com.qbanking.view.ViewNavigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class QBankingApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            DatabaseInitializer.initialise();   // create tables + sample data on first run
        } catch (RuntimeException e) {
            Dialogs.error("Qbanking could not open its database.\n\n" + e.getMessage()
                    + "\n\nIs another copy of Qbanking already running?");
            Platform.exit();
            return;
        }

        stage.setTitle("Qbanking");
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        ViewNavigator navigator = new ViewNavigator(stage);
        navigator.showLogin();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
