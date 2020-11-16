package client;

import client.GUI.FileInfo;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import utils.Commands;
import utils.FileCard;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Client {
    private static Client instance;
    private Socket socket;
    private ObjectEncoderOutputStream out;
    private ObjectDecoderInputStream in;
    private byte[] buff;

    private Client() {
        buff = new byte[8192];
    }

    public static synchronized Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public void connect() throws IOException {
        socket = new Socket("localhost", 8889);
        out = new ObjectEncoderOutputStream(socket.getOutputStream());
        in = new ObjectDecoderInputStream(socket.getInputStream());
        System.out.println("Connect");
    }

    public void disconnect() throws IOException {
        in.close();
        out.close();
    }

    public List<FileInfo> fileStructRequest() throws IOException {
        out.writeObject(Commands.FILE_STRUCT_REQ.getCode());
        try {
            Object incoming = in.readObject();
            if (incoming instanceof List) {
                List<FileInfo> fileList = (List<FileInfo>) incoming;
                return fileList;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<FileInfo> pathUpRequest(String path) throws IOException {
        try {
            out.writeObject(Commands.UP_REQ.getCode() + "\n" + path);
            Object incoming = in.readObject();
            if (incoming instanceof List) {
                List<FileInfo> fileList = (List<FileInfo>) incoming;
                return fileList;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<FileInfo> depthFileStructRequest(Path path) throws IOException {
        out.writeObject(Commands.DEPTH_REQ.getCode() + "\n" + path);
        try {
            Object incoming = in.readObject();
            if (incoming instanceof List && incoming != null) {
                List<FileInfo> fileList = (List<FileInfo>) incoming;
                return fileList;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String fileDeleteRequest(Path path) throws IOException {
        out.writeObject(Commands.DEL_REQ.getCode() + "\n" + path);
        Object msg = null;
        try {
            msg = in.readObject();
            if (msg instanceof String) {
                return (String) msg;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void uploadFile(Path path) {
        File file = new File(path.toString());
        long length = file.length();
        try {
            FileInputStream fIn = new FileInputStream(file);
            fIn.read(buff);
            fIn.close();
            FileCard fileCard = new FileCard(path.getFileName().toString(), length, buff);
            out.writeObject(fileCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}