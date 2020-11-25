package utils;

import java.io.Serializable;
import java.nio.file.Path;

public class FileCard implements Serializable {
    private String fileName;
    private byte[] data;
    private int length;

    public FileCard(String fileName, byte[] data, int length) {
        this.fileName = fileName;
        this.data = data;
        this.length = length;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return length;
    }
}
