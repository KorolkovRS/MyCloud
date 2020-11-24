package utils;

import java.io.Serializable;

public class AuthCard implements Serializable {
    private boolean checkReq;
    private String username;
    private String pass;

    public AuthCard(boolean checkReq, String username, String pass) {
        this.checkReq = checkReq;
        this.username = username;
        this.pass = pass;
    }

    public boolean isCheckReq() {
        return checkReq;
    }

    public String getUsername() {
        return username;
    }

    public String getPass() {
        return pass;
    }
}
