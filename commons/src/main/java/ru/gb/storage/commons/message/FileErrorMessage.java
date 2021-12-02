package ru.gb.storage.commons.message;

public class FileErrorMessage extends Message {
    private boolean deleteError;
    private boolean alreadyExists;

    public boolean isDeleteError() {
        return deleteError;
    }

    public void setDeleteError(boolean deleteError) {
        this.deleteError = deleteError;
    }

    public boolean isAlreadyExists() {
        return alreadyExists;
    }

    public void setAlreadyExists(boolean alreadyExists) {
        this.alreadyExists = alreadyExists;
    }
}
