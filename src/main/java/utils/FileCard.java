package utils;

import java.io.Serializable;

public class FileCard implements Serializable {
    private String fileName;
    private long fileLength;
    private byte[] data;

    public FileCard(String fileName, long fileLength, byte[] data) {
        this.fileLength = fileLength;
        this.fileName = fileName;
        this.data = data;
    }

    public long getFileLength() {
        return fileLength;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }
}
