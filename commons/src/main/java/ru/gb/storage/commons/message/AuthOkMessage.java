package ru.gb.storage.commons.message;

public class AuthOkMessage extends Message{
    private final String login;

    public AuthOkMessage(String login) {
        this.login = login;
    }
}

