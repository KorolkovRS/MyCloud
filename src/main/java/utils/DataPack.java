package utils;

import javafx.scene.web.HTMLEditorSkin;

import java.io.Serializable;
import java.nio.file.Path;

public class DataPack implements Serializable {
    private Integer token;
    private Commands command;
    private String path;
    private FileCard fileCard;

    public DataPack(Integer token, Commands command) {
        this.token = token;
        this.command = command;
        path = null;
        fileCard = null;
    }

    public DataPack(Integer token, Commands command, String path) {
        this.token = token;
        this.command = command;
        this.path = path;
        fileCard = null;
    }

    public DataPack(Integer token, Commands command, FileCard fileCard) {
        this.token = token;
        this.fileCard = fileCard;
        this.command = command;
        path = null;
    }

    public Integer getToken() {
        return token;
    }

    public Commands getCommand() {
        return command;
    }

    public String getPath() {
        return path;
    }

    public FileCard getFileCard() {
        return fileCard;
    }
}
