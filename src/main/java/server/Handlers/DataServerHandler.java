package server.Handlers;

import client.GUI.FileInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.io.FileUtils;
import server.BaseAuthService;
import server.DBAuthService;
import utils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DataServerHandler extends SimpleChannelInboundHandler<DataPack> {
    private DBAuthService authService;
    private String homeFolder;
    private static Set<File> filesInProgress = new HashSet<>();
    private byte[] buff = new byte[8192];
    private int token;

    public DataServerHandler() {
        super();
        authService = DBAuthService.getInstance();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataPack dataPack) throws Exception {
        // Если соединение есть в списке зарегистрированных, то работаем с пакетом, иначе возвращаем код ошибки
        System.out.println("data");
        token = dataPack.getToken();
        homeFolder = authService.check(token);
        if (homeFolder != null) {
            Commands command = dataPack.getCommand();
            System.out.println(command);
            try {
                if (command == null) {
                    throw new IOException();
                }
                switch (command) {
                    case UPLOAD_REQ:
                        uploadFile(dataPack.getFileCard(), ctx);
                        break;
                    case DOWNLOAD_REQ:
                        downloadFile(dataPack.getDownloadFileCard(), ctx);
                        break;
                    case FILE_STRUCT_REQ:
                    case OPEN_FOLDER_REQ:
                        getFileStruct(dataPack.getStringData(), ctx);
                        break;
                    case DEL_REQ:
                        deleteFile(dataPack.getStringData(), ctx);
                        break;
                    default:
                        throw new IOException();
                }
            } catch (IOException e) {
                e.printStackTrace();
                ctx.writeAndFlush(new DataPack(token, Commands.ERROR, "Ошибочная команда"));
            }
        } else {
            ctx.writeAndFlush(new DataPack(token, Commands.ERROR, "Ошибка аутентификации при отправке пакета"));
        }
    }

    private void uploadFile(FileCard inputFileCard, ChannelHandlerContext ctx) {
        if (inputFileCard == null) {
            return;
        }
        String inputFileName = inputFileCard.getFileName();
        File newFile = new File(homeFolder + "\\" + inputFileName);

        if (!filesInProgress.contains(newFile) && newFile.exists()) {
            newFile.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(newFile, true)) {
            filesInProgress.add(newFile);
            byte[] bytes = inputFileCard.getData();
            if (bytes == null) {
                filesInProgress.remove(newFile);
                ctx.writeAndFlush(new DataPack(token, Commands.OK, "Файл загружен"));
            } else {
                fos.write(bytes, 0, inputFileCard.getLength());
                fos.flush();
            }
        } catch (IOException e) {
            System.out.println("Ошибка записи");
            ctx.writeAndFlush(new DataPack(token, Commands.ERROR, "Ошибка загрузки файла на сервер"));
        }
    }

    private void getFileStruct(String cloudPanelPath, ChannelHandlerContext ctx) {
        Path path;
        if (cloudPanelPath == null) {
            path = Paths.get(homeFolder);
        } else {
            path = Paths.get(homeFolder + "\\" + cloudPanelPath);
        }
        try {
            List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
            ctx.writeAndFlush(new DataPack(token, Commands.FILE_STRUCT, list));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFile(String path, ChannelHandlerContext ctx) {
        try {
            if (path == null || path.isBlank()) {
                throw new IOException();
            }
            String deleteFile = homeFolder + "\\" + path;
            FileUtils.forceDelete(new File(deleteFile));
            ctx.writeAndFlush(new DataPack(token, Commands.OK));
        } catch (IOException e) {
            System.out.println("Ошибка при удалении файла");
            ctx.writeAndFlush(new DataPack(token, Commands.ERROR, "Ошибка при удалении файла"));
        }
    }

    private void downloadFile(DownloadFileCard downloadFileCard, ChannelHandlerContext ctx) {
        try {
            String serverPath = downloadFileCard.getServerPath();
            String clientPath = downloadFileCard.getClientPath();

            String fileName = Paths.get(serverPath).getFileName().toString();
            clientPath = clientPath + "\\" + fileName;

            File file = new File(homeFolder + "\\" + serverPath);
            int read;
            try (FileInputStream fis = new FileInputStream(file)) {
                while ((read = fis.read(buff)) != -1) {
                    ctx.writeAndFlush(new DataPack(token, Commands.UPLOAD_REQ,
                            new FileCard(clientPath, buff, read)));
                }
                ctx.writeAndFlush(new DataPack(token, Commands.UPLOAD_REQ, new FileCard(clientPath, null, 0)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            ctx.writeAndFlush(new DataPack(token, Commands.ERROR, "Ошибка при скачивании файла файла"));
        }
    }
}
