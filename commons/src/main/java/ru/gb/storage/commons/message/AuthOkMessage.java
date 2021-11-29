package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthOkMessage extends Message{
    private final String login;

    public AuthOkMessage(@JsonProperty("login") String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }
}

