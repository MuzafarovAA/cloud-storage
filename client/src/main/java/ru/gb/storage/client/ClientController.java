package ru.gb.storage.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.Optional;

public class ClientController {

    @FXML
    private ListView localStorageListView;
    @FXML
    private Button deleteLocalFileButton;
    @FXML
    private Button uploadFromLocalFileButton;
    @FXML
    private Button updateLocalFileListButton;
    @FXML
    private ListView cloudStorageListView;
    @FXML
    private Button downloadStorageFileButton;
    @FXML
    private Button deleteStorageFileButton;
    @FXML
    private Button updateStorageFileListButton;
    private ClientApp clientApp;
    private String login;
    private Alert alert;

    @FXML
    private void uploadFromLocalFile(ActionEvent actionEvent) {
        String fileName = getSelectedFileNameFormatted(localStorageListView);
        if (isFileSelected(fileName)) {
            if (cloudStorageListView.getItems().contains(fileName)) {
                alert = new Alert(Alert.AlertType.WARNING, "File is already exists in cloud storage. Replace it?", ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.YES) {
                    clientApp.sendUploadRequest(login, fileName);
                }
            } else {
                clientApp.sendUploadRequest(login, fileName);
            }
        }
    }

    @FXML
    private void updateLocalFileList(ActionEvent actionEvent) {
        clientApp.updateLocalFiles(login);
    }

    @FXML
    private void downloadStorageFile(ActionEvent actionEvent) {
        String fileName = getSelectedFileNameFormatted(cloudStorageListView);
        if (isFileSelected(fileName)) {
            if (localStorageListView.getItems().contains(fileName)) {
                alert = new Alert(Alert.AlertType.WARNING, "File is already exists in local storage. Replace it?", ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.YES) {
                    clientApp.sendDownloadRequest(login, fileName);
                }
            } else {
                clientApp.sendDownloadRequest(login, fileName);
            }
        }
    }

    @FXML
    private void deleteLocalFile(ActionEvent actionEvent) {
        String fileName = getSelectedFileNameFormatted(localStorageListView);
        if (isFileSelected(fileName)) {
            clientApp.deleteLocalFile(login, fileName);
        }
    }

    @FXML
    private void deleteStorageFile(ActionEvent actionEvent) {
        String fileName = getSelectedFileNameFormatted(cloudStorageListView);
        if (isFileSelected(fileName)) {
            clientApp.sendDeleteRequest(login, fileName);
        }
    }

    private String getSelectedFileNameFormatted(ListView listView) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(listView.getSelectionModel().getSelectedItems());
        fileName.deleteCharAt(0);
        fileName.deleteCharAt(fileName.length() - 1);
        System.out.println(fileName.toString());
        return fileName.toString();
    }

    private boolean isFileSelected(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            alert = new Alert(Alert.AlertType.ERROR, "File is not selected.", ButtonType.OK);
            alert.showAndWait();
            return false;
        } else return true;
    }

    @FXML
    private void updateStorageFileList(ActionEvent actionEvent) {
        clientApp.sendUpdateRequest(login);
    }

    void setLocalStorageListView(List<String> files) {
        Platform.runLater(() -> localStorageListView.setItems(FXCollections.observableList(files)));
    }

    void setCloudStorageListView(List<String> files) {
        Platform.runLater(() -> cloudStorageListView.setItems(FXCollections.observableList(files)));
    }

    void initClientApp(ClientApp clientApp) {
        this.clientApp = clientApp;
    }

    void initLogin(String login) {
        this.login = login;
    }
}