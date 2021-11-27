package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthRequestMessage extends Message{
    private final String login;
    private final String password;

    public AuthRequestMessage(@JsonProperty("login") String login, @JsonProperty("password") String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
