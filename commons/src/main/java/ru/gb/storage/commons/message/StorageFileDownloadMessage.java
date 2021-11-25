package ru.gb.storage.commons.message;

public class StorageFileDownloadMessage extends Message{
    //TODO StorageFileDownloadMessage

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
