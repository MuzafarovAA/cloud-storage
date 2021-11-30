package ru.gb.storage.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegController {

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;

    @FXML
    public void executeReg(ActionEvent actionEvent) {
    }

    @FXML
    public void switchToAuth(ActionEvent actionEvent) throws IOException {
        ClientApp.setRoot("authDialog");
    }
}
