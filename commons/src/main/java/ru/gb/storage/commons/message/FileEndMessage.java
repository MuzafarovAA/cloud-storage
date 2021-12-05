package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileEndMessage extends Message {
    private final String login;
    private final String fileName;

    public FileEndMessage(@JsonProperty("login") String login, @JsonProperty("filename") String fileName) {
        this.login = login;
        this.fileName = fileName;
    }

    public String getLogin() {
        return login;
    }

    public String getFileName() {
        return fileName;
    }
}
