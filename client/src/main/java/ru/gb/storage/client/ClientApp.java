package ru.gb.storage.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ClientApp extends Application {

    public static final String MAIN_SCENE_FXML = "mainScene.fxml";
    public static final String AUTH_DIALOG_FXML = "authDialog.fxml";
    private Scene scene;
    private Stage primaryStage;
    private Stage authDialogStage;
    private Network network;
    private Alert alert;
    private ClientController clientController;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;

        Thread thread = new Thread(() -> {
            network = new Network();
            network.start(this);
        });
        thread.setDaemon(true);
        thread.start();

        initAuthWindow();
        initMainWindow();
        primaryStage.show();
        authDialogStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });

        authDialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });

    }

    private void initMainWindow() throws IOException {
        FXMLLoader fxmlMainLoader = new FXMLLoader(ClientApp.class.getResource(MAIN_SCENE_FXML));
        Parent mainPanel = fxmlMainLoader.load();
        primaryStage.setTitle("Cloud Storage");
        scene = new Scene(mainPanel);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        clientController = fxmlMainLoader.getController();
        clientController.initClientApp(this);
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
        authDialogStage.setResizable(false);
        AuthController authController = fxmlAuthLoader.getController();
        authController.initClientApp(this);
    }

    public static void main(String[] args) {
        launch();
    }

    public void sendAuthMessage(String login, String password) {
        network.sendAuthMessage(login, password);
    }

    public void sendRegMessage(String login, String password) {
        network.sendRegMessage(login, password);
    }

    public void sendDeleteRequest(String login, String fileName) {
        network.sendDeleteMessage(login, fileName);
    }

    public void setAuthOk(String login) {
        Platform.runLater(() -> {
            alert = new Alert(Alert.AlertType.INFORMATION, "Successful authentication. Welcome, " + login, ButtonType.OK);
            alert.showAndWait();
            authDialogStage.close();
            primaryStage.setTitle("Cloud Storage " + login);
            clientController.initLogin(login);
            updateLocalFiles(login);
            sendUpdateRequest(login);
        });
    }

    public void setAuthError(String errorMessage) {
        Platform.runLater(() -> {
            alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.OK);
            alert.showAndWait();
        });
    }

    public void setStorageFileList(List<String> files) {
        if (!(files == null)) {
            clientController.setCloudStorageListView(files);
        }
    }

    public void sendUpdateRequest(String login) {
        network.sendUpdateRequest(login);
    }

    public void sendDownloadRequest(String login, String fileName) {
        network.sendDownloadRequest(login, fileName);
    }

    public void sendUploadRequest(String login, String fileName) {
        Path filePath = Path.of("local-storage/" + login + "/" + fileName);
        network.sendAddMessage(login, filePath);
    }


    public void updateLocalFiles(String login) {
        List<String> files = new ArrayList<>();
        Path path = Path.of("local-storage/" + login);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (NoSuchFileException e) {
                System.out.println("no such file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    files.add(path.relativize(file).toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientController.setLocalStorageListView(files);

    }

    public void deleteLocalFile(String login, String fileName) {
        Path path = Paths.get("local-storage/" + login + "/" + fileName);
        try {
            Files.delete(path);
        } catch (NoSuchFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateLocalFiles(login);
    }
}

