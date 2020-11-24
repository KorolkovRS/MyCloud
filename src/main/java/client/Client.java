package client;

import client.GUI.FileInfo;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.scene.web.HTMLEditorSkin;
import utils.AuthCard;
import utils.Commands;
import utils.DataPack;
import utils.FileCard;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class Client {
    private static final int BUFF_SIZE = 8192;
    private static Client instance;
    private Socket socket;
    private ObjectEncoderOutputStream out;
    private ObjectDecoderInputStream in;
    private byte[] buff;
    private String currentFile = "";
    private String username;
    private String password;

    private Client() {
        buff = new byte[BUFF_SIZE];
    }

    public static synchronized Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public void connect() throws IOException {
        socket = new Socket("localhost", 8888);
        out = new ObjectEncoderOutputStream(socket.getOutputStream());
        in = new ObjectDecoderInputStream(socket.getInputStream());
        System.out.println("Connect");
    }

    public void disconnect() throws IOException {
        in.close();
        out.close();
        System.out.println("Disconnect");
    }

    public String checkIn(String username, String pass) throws IOException {
        connect();
        try {
            out.writeObject(new AuthCard(true, username, pass));
            try {
                Object obj = in.readObject();
                String str;
                if (obj instanceof String) {
                    if (Commands.OK.getCode().equals(str = (String) obj)) {
                        this.username = username;
                        this.password = pass;
                        return str;
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new IOException();
            }
        } finally {
            disconnect();
        }
        return Commands.ERROR.getCode();
    }

    public String login(String username, String pass) throws IOException {
        connect();
        String msg = Commands.ERROR.getCode();
        try {
            out.writeObject(new AuthCard(false, username, pass));
            Object obj = in.readObject();
            String str;
            if (obj instanceof String) {
                if (Commands.OK.getCode().equals(str = (String) obj)) {
                    this.username = username;
                    this.password = pass;
                    msg = username;
                }
            }
        } finally {
            disconnect();
            return msg;
        }
    }

    public List<FileInfo> fileStructRequest(String path) throws IOException {
        connect();
        out.writeObject(new DataPack(username, password, Commands.FILE_STRUCT_REQ.getCode(), path));
        try {
            Object incoming = in.readObject();
            if (incoming instanceof List) {
                List<FileInfo> fileList = (List<FileInfo>) incoming;
                return fileList;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    public List<FileInfo> pathUpRequest(String path) throws IOException {
        connect();
        out.writeObject(new DataPack(username, password, Commands.UP_REQ.getCode(), path));
        try {
            Object incoming = in.readObject();
            if (incoming instanceof List) {
                List<FileInfo> fileList = (List<FileInfo>) incoming;
                return fileList;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    public List<FileInfo> depthFileStructRequest(Path path) throws IOException {
        connect();
        out.writeObject(new DataPack(username, password, Commands.DEPTH_REQ.getCode(), path.toString()));
        try {
            Object incoming = in.readObject();
            if (incoming instanceof List && incoming != null) {
                List<FileInfo> fileList = (List<FileInfo>) incoming;
                return fileList;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    public String fileDeleteRequest(Path path) throws IOException {
        connect();
        out.writeObject(new DataPack(username, password, Commands.DEL_REQ.getCode(), path.toString()));
        Object msg = null;
        try {
            msg = in.readObject();
            if (msg instanceof String) {
                return (String) msg;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    public String downloadFile(Path path, String downloadDir) throws IOException {
        connect();
        String msg = Commands.ERROR.getCode();
        File newFile = new File(downloadDir + "\\" + path.getFileName());
        if (newFile.exists()) {
            newFile.delete();
        }
        Object obj;
        try (FileOutputStream fos = new FileOutputStream(newFile, true)) {
            out.writeObject(new DataPack(username, password, Commands.DOWNLOAD_REQ.getCode(), path.toString()));
            while (true) {
                obj = in.readObject();
                if (obj instanceof FileCard) {
                    FileCard fileCard = (FileCard) obj;
                    if (!newFile.exists()) {
                        newFile.createNewFile();
                    }
                    if (fileCard.getFileName() != null) {
                        currentFile = fileCard.getFileName();
                    } else {
                        throw new IOException();
                    }
                    byte[] bytes = fileCard.getData();
                    if (bytes == null) {
                        currentFile = "";
                        msg = Commands.OK.getCode();
                        break;
                    } else {
                        fos.write(bytes, 0, fileCard.getLength());
                        fos.flush();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnect();
            return msg;
        }
    }

    public String uploadFile(Path path, String loadDir) throws IOException {
        connect();
        File file = new File(path.toString());
        int read;
        String msg = "error";
        String fileDir = loadDir + "\\" + path.getFileName().toString();

        try (FileInputStream fis = new FileInputStream(file)) {
            while ((read = fis.read(buff)) != -1) {
                out.writeObject(new DataPack(username, password, new FileCard(fileDir, buff, read)));
                out.flush();
            }
            out.writeObject(new DataPack(username, password, new FileCard(fileDir, null, 0)));
            out.flush();
            Object obj = in.readObject();
            if (obj instanceof String) {
                msg = (String) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            disconnect();
            return msg;
        }
    }
}