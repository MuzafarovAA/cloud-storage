package ru.gb.storage.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthController {

    private ClientApp clientApp;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public void executeReg(ActionEvent actionEvent){
        //TODO проверка на пустые поля
        clientApp.sendRegMessage(loginField.getText(), passwordField.getText());
    }
    @FXML
    public void executeAuth(ActionEvent actionEvent){
        //TODO проверка на пустые поля
        clientApp.sendAuthMessage(loginField.getText(), passwordField.getText());
    }

    public void initClientApp(ClientApp clientApp) {
        this.clientApp = clientApp;
    }

}
