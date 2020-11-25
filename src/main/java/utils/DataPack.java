package utils;

import client.GUI.FileInfo;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class DataPack implements Serializable {
    private Integer token;
    private Commands command;
    private Object data;

    public DataPack(Integer token, Commands command) {
        this.token = token;
        this.command = command;
        data = null;
    }

    public DataPack(Integer token, Commands command, Object data) {
        this.token = token;
        this.command = command;
        this.data = data;
    }

    public Integer getToken() {
        return token;
    }

    public Commands getCommand() {
        return command;
    }


    public FileCard getFileCard() throws IOException {
        if ((this.data instanceof FileCard) && Commands.UPLOAD_REQ.equals(this.command)) {
            return (FileCard) data;
        } else {
            throw new IOException();
        }
    }

    public String getStringData() throws IOException {
        if ((this.data instanceof String) &&
                (Commands.FILE_STRUCT_REQ.equals(this.command)
                    || Commands.DEL_REQ.equals(this.command))
                    || Commands.OPEN_FOLDER_REQ.equals(this.command)) {
            return (String) data;
        } else {
            throw new IOException();
        }
    }

    public List<FileInfo> getFolderStruct() throws IOException {
        if ((this.data instanceof List) && Commands.FILE_STRUCT.equals(this.command)) {
            return (List<FileInfo>) data;
        } else {
            throw new IOException();
        }
    }

    public DownloadFileCard getDownloadFileCard() throws IOException {
        if ((this.data instanceof DownloadFileCard) && Commands.DOWNLOAD_REQ.equals(this.command)) {
            return (DownloadFileCard) data;
        } else {
            throw new IOException();
        }
    }
}
