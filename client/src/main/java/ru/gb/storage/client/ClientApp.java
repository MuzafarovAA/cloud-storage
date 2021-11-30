package ru.gb.storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientApp extends Application {

    private Scene scene;
    private Stage primaryStage;
    private Stage authDialogStage;
    private Client client;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;

        Thread thread = new Thread(() -> {
            client = new Client();
            client.start();
        });
        thread.setDaemon(true);
        thread.start();
        
        initAuthWindow();
        initMainWindow();
        primaryStage.show();
        authDialogStage.show();

    }

    private void initMainWindow() throws IOException {
        FXMLLoader fxmlMainLoader = new FXMLLoader(ClientApp.class.getResource("mainScene.fxml"));
        Parent mainPanel = fxmlMainLoader.load();
        primaryStage.setTitle("Cloud Storage");
        scene = new Scene(mainPanel);
        primaryStage.setScene(scene);
    }

    private void initAuthWindow() throws IOException {
        FXMLLoader fxmlAuthLoader = new FXMLLoader(ClientApp.class.getResource("authDialog.fxml"));
        Parent authDialogPanel = fxmlAuthLoader.load();
        authDialogStage = new Stage();
        authDialogStage.setTitle("Authentication");
        authDialogStage.initOwner(primaryStage);
        authDialogStage.initModality(Modality.WINDOW_MODAL);
        scene = new Scene(authDialogPanel);
        authDialogStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }

}

