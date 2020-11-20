package server;

import client.GUI.FileInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class StorageServerHandler extends ChannelInboundHandlerAdapter {
    private String homePath;
    private String currentFile = "";
    private byte[] buff = new byte[8192];

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ACTIVE");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("INACTIVE");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        String command;
        String dir;
        FileCard fileCard;

        if (msg instanceof DataPack) {
            DataPack dataPack = (DataPack) msg;
            if (!login(dataPack.getLogin(), dataPack.getPass())) {
                ctx.writeAndFlush(Commands.ERROR.getCode());
                return;
            }

            if ((command = dataPack.getCommand()) != null && (dir = dataPack.getPath()) != null) {
                if (command.equals(Commands.HOME_PATH_REQ.getCode())) {
                    ctx.writeAndFlush(homePath);
                } else if (command.equals(Commands.FILE_STRUCT_REQ.getCode())) {
                    Path path;
                    try {
                        path = (Paths.get(homePath + "\\" + dir));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        path = (Paths.get(homePath));
                    }
                    List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
                    ctx.writeAndFlush(list);
                } else if (command.equals(Commands.UP_REQ.getCode())) {
                    if (!dir.isEmpty()) {
                        String currentPath = homePath + "\\" + dir;
                        Path path = Paths.get(pathUp(currentPath));
                        List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
                        ctx.writeAndFlush(list);
                    }
                } else if (command.equals(Commands.DEPTH_REQ.getCode())) {
                    String currentPath = homePath + "\\" + dir;
                    Path path = Paths.get(currentPath);
                    if (Files.isDirectory(path)) {
                        List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
                        ctx.writeAndFlush(list);
                    } else {
                        ctx.writeAndFlush("not a dir");
                    }
                } else if (command.equals(Commands.DEL_REQ.getCode())) {
                    ctx.writeAndFlush(deletePath(dir));
                } else if (command.equals(Commands.DOWNLOAD_REQ.getCode())) {
                    uploadFile(dir, ctx);
                }
            } else if ((fileCard = dataPack.getFileCard()) != null) {
                File newFile = new File(homePath + "\\" + fileCard.getFileName());
                if (!currentFile.equals(fileCard.getFileName())) {
                    try {
                        Files.delete(newFile.toPath());
                    } catch (IOException e) {
                    }
                }
                downloadFile(fileCard, ctx);
            }
        } else if (msg instanceof AuthCard) {
            AuthCard authCard;
            if ((authCard = (AuthCard) msg).isCheckReq()) {
                if(checkIn(authCard.getUsername(), authCard.getPass())) {
                    ctx.writeAndFlush(Commands.OK.getCode());
                } else {
                    ctx.writeAndFlush(Commands.ERROR.getCode());
                }
            } else {
               if(login(authCard.getUsername(), authCard.getPass())) {
                   ctx.writeAndFlush(Commands.OK.getCode());
               } else {
                   ctx.writeAndFlush(Commands.ERROR.getCode());
               }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private String pathUp(String path) throws IOException {
        Path upperPath = Paths.get(path).getParent();
        if (upperPath != null) {
            return upperPath.toString();
        } else {
            throw new IOException();
        }
    }

    private String deletePath(String path) {
        String deleteFile = homePath + "\\" + path;
        try {
            FileUtils.forceDelete(new File(deleteFile));
            return Commands.OK.getCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Commands.ERROR.getCode();
    }

    private void downloadFile(FileCard fileCard, ChannelHandlerContext ctx) {
        File newFile = new File(homePath + "\\" + fileCard.getFileName());
        try (FileOutputStream fos = new FileOutputStream(newFile, true)) {
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
                ctx.writeAndFlush(Commands.OK.getCode());
            } else {
                fos.write(bytes, 0, fileCard.getLength());
                fos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            ctx.writeAndFlush(Commands.ERROR.getCode());
            currentFile = "";
        }
    }

    private void uploadFile(String fileName, ChannelHandlerContext ctx) {
        File file = new File(homePath + "\\" + fileName);
        FileCard fileCard;
        int read;

        try (FileInputStream fis = new FileInputStream(file)) {
            while ((read = fis.read(buff)) != -1) {
                fileCard = new FileCard(Paths.get(file.getName()).toString(), buff, read);
                ctx.writeAndFlush(fileCard);
            }
            fileCard = new FileCard(Paths.get(file.getName()).toString(), null, 0);
            ctx.writeAndFlush(fileCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean login(String login, String pass) {
        String dir;
        if (login != null && pass != null) {
            if((dir = AuthHandler.getHomePath(login, pass)) != null) {
                System.out.println("SH login");
                homePath = dir;
                System.out.println("homepath " + homePath);
                return true;
            }
        }
        return false;
    }

    private boolean checkIn(String login, String pass) {
        try {
            AuthHandler.setHomePath(login, pass);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
