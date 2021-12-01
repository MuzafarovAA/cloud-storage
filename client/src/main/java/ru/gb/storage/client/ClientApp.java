package ru.gb.storage.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application {

    public static final String MAIN_SCENE_FXML = "mainScene.fxml";
    public static final String AUTH_DIALOG_FXML = "authDialog.fxml";
    private Scene scene;
    private Stage primaryStage;
    private Stage authDialogStage;
    private Client client;
    private Alert alert;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;

        Thread thread = new Thread(() -> {
            client = new Client();
            client.start(this);
        });
        thread.setDaemon(true);
        thread.start();
        
        initAuthWindow();
        initMainWindow();
        primaryStage.show();
        authDialogStage.show();

    }

    private void initMainWindow() throws IOException {
        FXMLLoader fxmlMainLoader = new FXMLLoader(ClientApp.class.getResource(MAIN_SCENE_FXML));
        Parent mainPanel = fxmlMainLoader.load();
        primaryStage.setTitle("Cloud Storage");
        scene = new Scene(mainPanel);
        primaryStage.setScene(scene);
    }

    private void initAuthWindow() throws IOException {
        FXMLLoader fxmlAuthLoader = new FXMLLoader(ClientApp.class.getResource(AUTH_DIALOG_FXML));
        Parent authDialogPanel = fxmlAuthLoader.load();
        authDialogStage = new Stage();
        authDialogStage.setTitle("Authentication");
        authDialogStage.initOwner(primaryStage);
        authDialogStage.initModality(Modality.WINDOW_MODAL);
        scene = new Scene(authDialogPanel);
        authDialogStage.setScene(scene);
        AuthController authController = fxmlAuthLoader.getController();
        authController.initClientApp(this);
    }

    public static void main(String[] args) {
        launch();
    }

    public void sendAuthMessage(String login, String password) {
        //TODO проверка на пустые поля
        client.sendAuthMessage(login, password);
    }

    public void sendRegMessage(String login, String password) {

        client.sendRegMessage(login, password);
    }

    public void setAuthOk(String login) {
        Platform.runLater(() -> {
            authDialogStage.close();
            primaryStage.setTitle("Cloud Storage " + login);
        });
    }

    public void setAuthError(String errorMessage) {
        Platform.runLater(() -> {
            alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.OK);
            alert.showAndWait();
        });
    }
}

