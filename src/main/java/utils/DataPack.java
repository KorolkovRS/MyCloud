package utils;

public class DataPack {
    private String userAndPass;
    private String command;
    private String path;
    private FileCard fileCard;

    public DataPack(String userAndPass, String command, String path) {
        this.userAndPass = userAndPass;
        this.command = command;
        this.path = path;
        fileCard = null;
    }

    public DataPack(String userAndPass, FileCard fileCard) {
        this.userAndPass = userAndPass;
        this.fileCard = fileCard;
    }

    public String getUserAndPass() {
        return userAndPass;
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
