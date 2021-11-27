package ru.gb.storage.commons.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthRegisterMessage extends Message{
    private String login;
    private String password;

    public AuthRegisterMessage(@JsonProperty("login") String login, @JsonProperty("password") String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
