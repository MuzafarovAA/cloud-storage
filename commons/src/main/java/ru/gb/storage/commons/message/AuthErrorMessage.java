package ru.gb.storage.commons.message;

public class AuthErrorMessage extends Message{
    private boolean loginError;
    private boolean passwordError;

    public boolean isLoginError() {
        return loginError;
    }

    public void setLoginError(boolean loginError) {
        this.loginError = loginError;
    }

    public boolean isPasswordError() {
        return passwordError;
    }

    public void setPasswordError(boolean passwordError) {
        this.passwordError = passwordError;
    }
}
