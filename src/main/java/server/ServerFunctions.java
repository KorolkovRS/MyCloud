package server;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * В этом классе описан функционал сервера
 */

public class ServerFunctions {
    private final String rootPath = "src\\main\\resources\\server";
    private String currentPath;

    public ServerFunctions() {
        this.currentPath = rootPath;
    }

    public String getList(){
        return String.join(" ", new File(currentPath).list());
    }

    public void touch(String fileName) throws IOException {
        Path file = Files.createFile(Paths.get(currentPath + "\\" + fileName));
    }

    public void mkdir(String dirName) throws IOException {
        Path directory = Files.createDirectory(Paths.get(currentPath + "\\" + dirName));
    }

    public void remove(String name) throws IOException {
        File file = new File(currentPath + "\\" + name);
        FileUtils.forceDelete(file);
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public String getRootPath() {
        return rootPath;
    }
}
