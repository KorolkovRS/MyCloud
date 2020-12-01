package com.korolkovrs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BaseAuthService implements AuthService {
    private static BaseAuthService instance;
    private String homeFolder;
    private int clientCounter;
    private Map<Integer, Path> clients;

    private BaseAuthService() {
        homeFolder = "src\\main\\resources\\serverData\\User";
        clients = new HashMap<>();
    }

    @Override
    public synchronized AuthCard add(AuthCard authCard) {
        int token = (authCard.getUsername() + authCard.getPass()).hashCode();
        AuthCard answer;
        System.out.println("add " + token);
        try {
            if (clients.containsKey(token)) {
                throw new IOException();
            }
            clients.put(token, getNewFolder());
            answer = new AuthCard(true, authCard.getUsername(), null, token);

        } catch (IOException e) {
            answer = new AuthCard(true, authCard.getUsername(), null, null);
        }
        return answer;
    }

    @Override
    public synchronized AuthCard login(AuthCard authCard) {
        int token = (authCard.getUsername() + authCard.getPass()).hashCode();
        AuthCard answer;
        System.out.println("check " + token);
        if (clients.containsKey(token)) {
            answer = new AuthCard(false, authCard.getUsername(), null, token);
        } else {
            answer = new AuthCard(false, authCard.getUsername(), null, null);
        }
        return answer;
    }

    @Override
    public synchronized String check(Integer token) {
        return clients.get(token).toString();
    }

    private synchronized Path getNewFolder() throws IOException {
        Path newFolderPath;
        while (true) {
            newFolderPath = Paths.get(homeFolder + clientCounter);
            if (Files.exists(newFolderPath)) {
                clientCounter++;
            } else {
                Files.createDirectory(Paths.get(homeFolder + clientCounter));
                break;
            }
        }
        clientCounter++;
        return newFolderPath;
    }

    public static synchronized BaseAuthService getInstance() {
        if (instance == null) {
            instance = new BaseAuthService();
        }
        return instance;
    }
}