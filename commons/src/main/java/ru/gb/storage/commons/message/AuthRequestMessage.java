package ru.gb.storage.commons.message;

public class AuthRequestMessage extends Message{
    private String login;
    private String password;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
