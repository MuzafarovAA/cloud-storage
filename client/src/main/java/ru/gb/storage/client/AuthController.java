package ru.gb.storage.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController {

    private Alert alert;
    private ClientApp clientApp;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void executeReg(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank()) {
            alert = new Alert(Alert.AlertType.ERROR, "Login is null", ButtonType.OK);
            alert.showAndWait();
        } else if (password == null || password.isBlank()) {
            alert = new Alert(Alert.AlertType.ERROR, "Password is null", ButtonType.OK);
            alert.showAndWait();
        } else {
            clientApp.sendRegMessage(loginField.getText(), passwordField.getText());
        }
    }

    @FXML
    private void executeAuth(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank()) {
            alert = new Alert(Alert.AlertType.ERROR, "Login is null", ButtonType.OK);
            alert.showAndWait();
        } else if (password == null || password.isBlank()) {
            alert = new Alert(Alert.AlertType.ERROR, "Password is null", ButtonType.OK);
            alert.showAndWait();
        } else {
            clientApp.sendAuthMessage(loginField.getText(), passwordField.getText());
        }
    }

    void initClientApp(ClientApp clientApp) {
        this.clientApp = clientApp;
    }

}
