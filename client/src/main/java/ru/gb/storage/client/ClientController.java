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
    private Alert alert;

    public void uploadFromLocalFile(ActionEvent actionEvent) {
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

    public void updateLocalFileList(ActionEvent actionEvent) {
        clientApp.updateLocalFiles(login);
    }

    public void downloadStorageFile(ActionEvent actionEvent) {
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

    public void deleteLocalFile(ActionEvent actionEvent) {
        String fileName = getSelectedFileNameFormatted(localStorageListView);
        if (isFileSelected(fileName)) {
            clientApp.deleteLocalFile(login, fileName);
        }
    }

    public void deleteStorageFile(ActionEvent actionEvent) {
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