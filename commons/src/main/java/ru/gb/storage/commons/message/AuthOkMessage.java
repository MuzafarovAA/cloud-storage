package ru.gb.storage.commons.message;

public class AuthOkMessage extends Message{
    private boolean isLoginCorrect;
    private boolean isPasswordCorrect;

    public boolean isLoginCorrect() {
        return isLoginCorrect;
    }

    public void setLoginCorrect(boolean loginCorrect) {
        isLoginCorrect = loginCorrect;
    }

    public boolean isPasswordCorrect() {
        return isPasswordCorrect;
    }

    public void setPasswordCorrect(boolean passwordCorrect) {
        isPasswordCorrect = passwordCorrect;
    }
}
