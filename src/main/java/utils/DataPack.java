package utils;

import java.io.Serializable;

public class DataPack implements Serializable {
    private String login;
    private String pass;
    private String command;
    private String path;
    private FileCard fileCard;

    public DataPack(String login, String pass, String command, String path) {
        this.login = login;
        this.pass = pass;
        this.command = command;
        this.path = path;
        fileCard = null;
    }

    public DataPack(String login, String pass, FileCard fileCard) {
        this.login = login;
        this.pass = pass;
        this.fileCard = fileCard;
        command = null;
        path = null;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public String getCommand() {
        return command;
    }

    public String getPath() {
        return path;
    }

    public FileCard getFileCard() {
        return fileCard;
    }
}
