package utils;

public class Record {
    private String username;
    private String password;
    private String folder;

    public Record(String username, String password, String folder) {
        this.username = username;
        this.password = password;
        this.folder = folder;
    }

    public String getUsername() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
