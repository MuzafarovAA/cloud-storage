package ru.gb.storage.commons.message;

public class StorageFileAddMessage extends Message{
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
