package ru.gb.storage.commons.message;

import java.nio.file.Path;
import java.util.ArrayList;

public class StorageFileListMessage extends Message {
    private ArrayList<String> files;

    public ArrayList<String> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<String> files) {
        this.files = files;
    }
}
