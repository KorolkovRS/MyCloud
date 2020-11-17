package server;

import client.GUI.FileInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import utils.Commands;
import utils.FileCard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class StorageServerHandler extends ChannelInboundHandlerAdapter {
    private final String homePath = "src\\main\\resources\\serverData\\User1";
    private String currentFile = "";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        if (msg instanceof String) {
            String[] strings = ((String) msg).split("\n");
            String s = strings[0];
            if (s.equals(Commands.HOME_PATH_REQ.getCode())) {
                ctx.writeAndFlush(homePath);
            } else if (s.equals(Commands.FILE_STRUCT_REQ.getCode())) {
                Path path = (Paths.get(homePath));
                List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
                ctx.writeAndFlush(list);
            } else if (s.equals(Commands.UP_REQ.getCode())) {
                if (!strings[1].isEmpty()) {
                    String currentPath = homePath + "\\" + strings[1];
                    System.out.println(currentPath);
                    Path path = Paths.get(pathUp(currentPath));
                    List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
                    ctx.writeAndFlush(list);
                }
            } else if (s.equals(Commands.DEPTH_REQ.getCode())) {
                String currentPath = homePath + "\\" + strings[1];
                Path path = Paths.get(currentPath);
                if (Files.isDirectory(path)) {
                    List<FileInfo> list = Files.list(path).map(FileInfo::new).collect(Collectors.toList());
                    ctx.writeAndFlush(list);
                } else {
                    ctx.writeAndFlush("not a dir");
                }
            } else if (s.equals(Commands.DEL_REQ.getCode())) {
                ctx.writeAndFlush(deletePath(strings[1]));
            }

        } else if (msg instanceof FileCard) {
            FileCard fileCard = (FileCard) msg;
            File newFile = new File(homePath + "\\" + fileCard.getFileName());

            if (!currentFile.equals(fileCard.getFileName())) {
                try {
                    Files.delete(newFile.toPath());
                    System.out.println("del");
                } catch (IOException e) {
                    e.printStackTrace();
                    ctx.writeAndFlush("Error");
                }
            }
            fileWrite(fileCard, ctx);
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
        System.out.println(deleteFile);
        try {
            FileUtils.forceDelete(new File(deleteFile));
            return "ok";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }

    private void fileWrite(FileCard fileCard, ChannelHandlerContext ctx) {
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
                ctx.writeAndFlush("ok");
            } else {
                fos.write(bytes, 0, fileCard.getLength());
                fos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            ctx.writeAndFlush("error");
            currentFile = "";
        }
    }
}
