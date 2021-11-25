package ru.gb.storage.commons.message;

public class RegistrationMessage extends Message{
    private String login;
    private String password;

    public RegistrationMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
