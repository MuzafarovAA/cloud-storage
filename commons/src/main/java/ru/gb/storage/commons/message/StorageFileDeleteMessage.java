package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StorageFileDeleteMessage extends Message{
    private final String login;
    private final String fileName;

    public String getFileName() {
        return fileName;
    }

    public String getLogin() {
        return login;
    }

    public StorageFileDeleteMessage(@JsonProperty("login") String login, @JsonProperty("filename") String fileName) {
        this.login = login;
        this.fileName = fileName;
    }
}
