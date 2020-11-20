package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class AuthHandler {
    private static final String homePath = "src\\main\\resources\\serverData\\User";
    private static final HashMap<String, String> loginAndPass = new HashMap<>();
    private static final HashMap<String, String> loginAndHomePath = new HashMap<>();
    private static int count;


    public synchronized static String getHomePath(String login, String pass) {
        if (pass.equals(loginAndPass.get(login))) {
            System.out.println("getHomePath");
            return loginAndHomePath.get(login);
        }
        return null;
    }

    public synchronized static void setHomePath(String login, String pass) throws IllegalAccessException, IOException {
        if (loginAndPass.containsKey(login)) {
            throw new IllegalAccessException();
        }
        Path path;
        loginAndPass.put(login, pass);
        while (true) {
            path = Paths.get(homePath + count);
            if (Files.exists(path)) {
                count++;
            } else {
                Files.createDirectory(Paths.get(homePath + count));
                break;
            }
        }

        count++;
        loginAndHomePath.put(login, path.toString());
    }
}
