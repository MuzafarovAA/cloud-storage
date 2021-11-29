package ru.gb.storage.commons.message;

import java.nio.file.Path;

public class FileRequestMessage extends Message{
    private String login;
    private String fileName;
    private Path filePath;

    public FileRequestMessage(String login, String fileName) {
        this.login = login;
        this.fileName = fileName;
    }

    public FileRequestMessage() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }
}
