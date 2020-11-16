package server;

import client.GUI.FileInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import utils.Commands;
import utils.FileCard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class StorageServerHandler extends ChannelInboundHandlerAdapter {
    private final String homePath = "src\\main\\resources\\serverData\\User1";

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
            FileCard file = (FileCard) msg;
            System.out.println(file.getFileName());
            System.out.println(file.getFileLength());
            byte[] bytes = file.getData();
            for (int i = 0; i < bytes.length; i++) {
                System.out.println(bytes[i]);
            }

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public String pathUp(String path) throws IOException {
        Path upperPath = Paths.get(path).getParent();
        if (upperPath != null) {
            return upperPath.toString();
        } else {
            throw new IOException();
        }
    }

    public String deletePath(String path) {
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
}
