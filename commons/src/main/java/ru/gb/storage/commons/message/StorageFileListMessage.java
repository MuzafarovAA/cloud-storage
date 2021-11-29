package ru.gb.storage.commons.message;

import java.util.List;

public class StorageFileListMessage extends Message {
    private List<String> files;

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
