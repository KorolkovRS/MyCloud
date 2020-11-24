package server;

import utils.AuthCard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AuthHandler {
    private static final String homeFolder = "src\\main\\resources\\serverData\\User";
    private static int clientCounter;
    private static Map<Integer, Path> clients = new HashMap<>();

    public static synchronized AuthCard add(AuthCard authCard) {
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

    public static synchronized AuthCard login(AuthCard authCard) {
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

    public static synchronized String check(Integer token) {
        return clients.get(token).toString();
    }

    private static synchronized Path getNewFolder() throws IOException {
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
}
//
//
//
//    private static final String homePath = "src\\main\\resources\\serverData\\User";
//    private static final HashMap<String, String> loginAndPass = new HashMap<>();
//    private static final HashMap<String, String> loginAndHomePath = new HashMap<>();
//    private static int count;
//
//
//    public synchronized static String getHomePath(String login, String pass) {
//        if (pass.equals(loginAndPass.get(login))) {
//            System.out.println("getHomePath");
//            return loginAndHomePath.get(login);
//        }
//        return null;
//    }
//
//    public synchronized static void setHomePath(String login, String pass) throws IllegalAccessException, IOException {
//        if (loginAndPass.containsKey(login)) {
//            throw new IllegalAccessException();
//        }
//        Path path;
//        loginAndPass.put(login, pass);
//        while (true) {
//            path = Paths.get(homePath + count);
//            if (Files.exists(path)) {
//                count++;
//            } else {
//                Files.createDirectory(Paths.get(homePath + count));
//                break;
//            }
//        }
//        count++;
//        loginAndHomePath.put(login, path.toString());
//    }
//}
