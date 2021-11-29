package ru.gb.storage.commons.message;

public class FileErrorMessage extends Message{
    private boolean deleteError;

    public boolean isDeleteError() {
        return deleteError;
    }

    public void setDeleteError(boolean deleteError) {
        this.deleteError = deleteError;
    }
}
