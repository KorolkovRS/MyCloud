package com.korolkovrs;

import com.korolkovrs.Connecrors.MySQLConnector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;

public class DBAuthService implements AuthService {
    private String homeFolder = "C:\\Programs\\GeekBrains\\Cloud\\MyCloud\\server\\src\\main\\resources\\serverData\\";
    private static DBAuthService instance;
    private HashMap<Integer, String> authUsers;

    private DBAuthService() {
        authUsers = new HashMap<>();
    }

    @Override
    public AuthCard add(AuthCard authCard) throws SQLException, IOException {
        int token = (authCard.getUsername() + authCard.getPass()).hashCode();
        AuthCard answer = new AuthCard(true, authCard.getUsername(), null, null);

        Path userFolder = Paths.get(homeFolder + authCard.getUsername());
        if (!Files.exists(userFolder)) {
            if (MySQLConnector.addUser(authCard.getUsername(), authCard.getPass())) {
                Files.createDirectory(userFolder);
                authUsers.put(token, authCard.getUsername());
                answer = new AuthCard(true, authCard.getUsername(), null, token);
            }
        }
        return answer;
    }

    @Override
    public AuthCard login(AuthCard authCard) throws SQLException {
        int token = (authCard.getUsername() + authCard.getPass()).hashCode();
        AuthCard answer;
        if (MySQLConnector.checkUser(authCard.getUsername(), authCard.getPass())) {
            answer = new AuthCard(false, authCard.getUsername(), null, token);
            authUsers.put(token, authCard.getUsername());
        } else {
            answer = new AuthCard(false, authCard.getUsername(), null, null);
        }
        return answer;
    }

    @Override
    public String check(Integer token) {
        if (authUsers.containsKey(token)) {
            return homeFolder + authUsers.get(token);
        }
        return null;
    }

    public synchronized void connect() throws SQLException, ClassNotFoundException {
        MySQLConnector.connection();
    }

    public static synchronized DBAuthService getInstance() {
        if (instance == null) {
            instance = new DBAuthService();
        }
        return instance;
    }
}
