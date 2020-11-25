package server;

import client.GUI.FileInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.io.FileUtils;
import utils.AuthCard;
import utils.Commands;
import utils.DataPack;
import utils.FileCard;

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
    private String homeFolder;
    private static Set<File> filesInProgress = new HashSet<>();
    private byte[] buff = new byte[8192];
    private int token;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataPack dataPack) throws Exception {
        // Если соединение есть в списке зарегистрированных, то работаем с пакетом, иначе возвращаем код ошибки
        token = dataPack.getToken();
        homeFolder = AuthHandler.check(token);
        if (homeFolder != null) {
            Commands command = dataPack.getCommand();
            try {
                if(command == null) {
                    throw new IOException();
                }
                switch (command) {
                    case UPLOAD_REQ:
                        uploadFile(dataPack.getFileCard(), ctx);
                        break;
                    case FILE_STRUCT_REQ:
                        getFileStruct(dataPack.getStringData(), ctx);
                        break;
                    case DEL_REQ:
                        deleteFile(dataPack.getStringData(), ctx);
                        break;
                    case OPEN_FOLDER_REQ:
                        getFileStruct(dataPack.getStringData(), ctx);
                        break;
                    default:
                        throw new IOException();
                }
            } catch (IOException e) {
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
}


//public class StorageServerHandler extends ChannelInboundHandlerAdapter {
//    private String homePath;
//    private String currentFile = "";
//    private byte[] buff = new byte[8192];
//
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("ACTIVE");
//    }
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("INACTIVE");
//    }
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
//        String command;
//        String dir;
//        FileCard fileCard;
//
//        if (msg instanceof DataPack) {
//            DataPack dataPack = (DataPack) msg;
//
//            if ((command = dataPack.getCommand()) != null && (dir = dataPack.getPath()) != null) {
//                if (command.equals(Commands.HOME_PATH_REQ.getCode())) {
//                    ctx.writeAndFlush(homePath);
//                } else if (command.equals(Commands.FILE_STRUCT_REQ.getCode())) {
//                    Path path;
//                    try {
//                        path = (Paths.get(homePath + "\\" + dir));
//                    } catch (ArrayIndexOutOfBoundsException e) {
//                        path = (Paths.get(homePath));
//                    }
//                    List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
//                    ctx.writeAndFlush(list);
//                } else if (command.equals(Commands.UP_REQ.getCode())) {
//                    if (!dir.isEmpty()) {
//                        String currentPath = homePath + "\\" + dir;
//                        Path path = Paths.get(pathUp(currentPath));
//                        List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
//                        ctx.writeAndFlush(list);
//                    }
//                } else if (command.equals(Commands.DEPTH_REQ.getCode())) {
//                    String currentPath = homePath + "\\" + dir;
//                    Path path = Paths.get(currentPath);
//                    if (Files.isDirectory(path)) {
//                        List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
//                        ctx.writeAndFlush(list);
//                    } else {
//                        ctx.writeAndFlush("not a dir");
//                    }
//                } else if (command.equals(Commands.DEL_REQ.getCode())) {
//                    ctx.writeAndFlush(deletePath(dir));
//                } else if (command.equals(Commands.DOWNLOAD_REQ.getCode())) {
//                    uploadFile(dir, ctx);
//                }
//            } else if ((fileCard = dataPack.getFileCard()) != null) {
//                File newFile = new File(homePath + "\\" + fileCard.getFileName());
//                if (!currentFile.equals(fileCard.getFileName())) {
//                    try {
//                        Files.delete(newFile.toPath());
//                    } catch (IOException e) {
//                    }
//                }
//                downloadFile(fileCard, ctx);
//            }
//        } else if (msg instanceof AuthCard) {
//            AuthCard authCard;
//            if ((authCard = (AuthCard) msg).isCheckReq()) {
//                if (checkIn(authCard.getUsername(), authCard.getPass())) {
//                    ctx.writeAndFlush(Commands.OK.getCode());
//                } else {
//                    ctx.writeAndFlush(Commands.ERROR.getCode());
//                }
//            } else {
//                if (login(authCard.getUsername(), authCard.getPass())) {
//                    ctx.writeAndFlush(Commands.OK.getCode());
//                } else {
//                    ctx.writeAndFlush(Commands.ERROR.getCode());
//                }
//            }
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        cause.printStackTrace();
//        ctx.close();
//    }
//
//    private String pathUp(String path) throws IOException {
//        Path upperPath = Paths.get(path).getParent();
//        if (upperPath != null) {
//            return upperPath.toString();
//        } else {
//            throw new IOException();
//        }
//    }
//
//    private String deletePath(String path) {
//        String deleteFile = homePath + "\\" + path;
//        try {
//            FileUtils.forceDelete(new File(deleteFile));
//            return Commands.OK.getCode();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return Commands.ERROR.getCode();
//    }
//
//    private void downloadFile(FileCard fileCard, ChannelHandlerContext ctx) {
//        File newFile = new File(homePath + "\\" + fileCard.getFileName());
//        try (FileOutputStream fos = new FileOutputStream(newFile, true)) {
//            if (!newFile.exists()) {
//                newFile.createNewFile();
//            }
//            if (fileCard.getFileName() != null) {
//                currentFile = fileCard.getFileName();
//            } else {
//                throw new IOException();
//            }
//            byte[] bytes = fileCard.getData();
//            if (bytes == null) {
//                currentFile = "";
//                ctx.writeAndFlush(Commands.OK.getCode());
//            } else {
//                fos.write(bytes, 0, fileCard.getLength());
//                fos.flush();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            ctx.writeAndFlush(Commands.ERROR.getCode());
//            currentFile = "";
//        }
//    }
//
//    private void uploadFile(String fileName, ChannelHandlerContext ctx) {
//        File file = new File(homePath + "\\" + fileName);
//        FileCard fileCard;
//        int read;
//
//        try (FileInputStream fis = new FileInputStream(file)) {
//            while ((read = fis.read(buff)) != -1) {
//                fileCard = new FileCard(Paths.get(file.getName()).toString(), buff, read);
//                ctx.writeAndFlush(fileCard);
//            }
//            fileCard = new FileCard(Paths.get(file.getName()).toString(), null, 0);
//            ctx.writeAndFlush(fileCard);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private boolean login(String login, String pass) {
//        String dir;
//        if (login != null && pass != null) {
//            if ((dir = AuthHandler.getHomePath(login, pass)) != null) {
//                System.out.println("SH login");
//                homePath = dir;
//                System.out.println("homepath " + homePath);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean checkIn(String login, String pass) {
//        try {
//            AuthHandler.setHomePath(login, pass);
//            return true;
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//}
