package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

public class StorageFileAddMessage extends Message{
    private String login;
    private Path fileName;

    public StorageFileAddMessage(@JsonProperty("login") String login, @JsonProperty("filename") Path filePath) {
        this.login = login;
        this.fileName = filePath;
    }

    public String getLogin() {
        return login;
    }

    public Path getFileName() {
        return fileName;
    }
}
