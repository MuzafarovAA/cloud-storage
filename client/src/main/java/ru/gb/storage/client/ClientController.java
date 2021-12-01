package ru.gb.storage.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.util.List;

public class ClientController {

    @FXML
    public ListView localStorageListView;
    @FXML
    public Button selectAllLocalFilesButton;
    @FXML
    public Button uploadFromLocalFileButton;
    @FXML
    public Button updateLocalFileListButton;
    @FXML
    public ListView cloudStorageListView;
    @FXML
    public Button selectAllStorageFilesButton;
    @FXML
    public Button downloadStorageFileButton;
    @FXML
    public Button deleteStorageFileButton;
    @FXML
    public Button updateStorageFileListButton;
    private ClientApp clientApp;
    private String login;

    public void selectAllLocalFiles(ActionEvent actionEvent) {
    }

    public void uploadFromLocalFile(ActionEvent actionEvent) {
    }

    public void updateLocalFileList(ActionEvent actionEvent) {
        clientApp.updateLocalFiles(login);
    }

    public void selectAllStorageFiles(ActionEvent actionEvent) {
    }

    public void downloadStorageFile(ActionEvent actionEvent) {
    }

    public void deleteStorageFile(ActionEvent actionEvent) {
    }

    public void updateStorageFileList(ActionEvent actionEvent) {
        clientApp.sendUpdateRequest(login);
    }

    public void setLocalStorageListView(List<String> files) {
        Platform.runLater(() -> localStorageListView.setItems(FXCollections.observableList(files)));
    }

    public void setCloudStorageListView(List<String> files) {
        Platform.runLater(() -> cloudStorageListView.setItems(FXCollections.observableList(files)));
    }

    public void initClientApp(ClientApp clientApp) {
        this.clientApp = clientApp;
    }

    public void initLogin(String login) {
        this.login = login;
    }

}