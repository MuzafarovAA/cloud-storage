package ru.gb.storage.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.util.List;

public class ClientController {

    @FXML
    public ListView localStorageListView;
    @FXML
    public Button deleteLocalFileButton;
    @FXML
    public Button uploadFromLocalFileButton;
    @FXML
    public Button updateLocalFileListButton;
    @FXML
    public ListView cloudStorageListView;
    @FXML
    public Button downloadStorageFileButton;
    @FXML
    public Button deleteStorageFileButton;
    @FXML
    public Button updateStorageFileListButton;
    private ClientApp clientApp;
    private String login;

//TODO файл не выбран
    public void uploadFromLocalFile(ActionEvent actionEvent) {
    }

    public void updateLocalFileList(ActionEvent actionEvent) {
        clientApp.updateLocalFiles(login);
    }

    public void downloadStorageFile(ActionEvent actionEvent) {
    }

    public void deleteLocalFile(ActionEvent actionEvent) {
        String fileName = getSelectedFileNameFormatted(localStorageListView);
        clientApp.deleteLocalFile(login, fileName);
    }

    public void deleteStorageFile(ActionEvent actionEvent) {
        String fileName = getSelectedFileNameFormatted(cloudStorageListView);
        clientApp.sendDeleteRequest(login, fileName);
    }

    private String getSelectedFileNameFormatted(ListView listView) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(listView.getSelectionModel().getSelectedItems());
        fileName.deleteCharAt(0);
        fileName.deleteCharAt(fileName.length() - 1);
        System.out.println(fileName.toString());
        return fileName.toString();
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