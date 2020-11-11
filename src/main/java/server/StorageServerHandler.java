package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import utils.CommandCode;

import java.io.IOException;

/**
 * Обработчик входящих строковых сообщений.
 */

public class StorageServerHandler extends SimpleChannelInboundHandler<String> {
    private ServerFunctions functions = new ServerFunctions();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        System.out.println(s);
        if (s.startsWith(CommandCode.MSG.getTitle())) {
            messageHandler(ctx, s.substring(CommandCode.MSG.getTitle().length() + 1));
        } else if (s.equals(CommandCode.LIST.getTitle())) {
            getList(ctx);
        } else if(s.startsWith(CommandCode.TOUCH.getTitle())) {
            touch(ctx, s.substring(CommandCode.TOUCH.getTitle().length() + 1));
        } else if(s.startsWith(CommandCode.MKDIR.getTitle())) {
            mkdir(ctx, s.substring(CommandCode.MKDIR.getTitle().length() + 1));
        } else if(s.startsWith(CommandCode.REMOVE.getTitle())) {
            remove(ctx, s.substring(CommandCode.REMOVE.getTitle().length() + 1));
        }
    }

    private void messageHandler(ChannelHandlerContext ctx, String msg) {
        ctx.writeAndFlush("echo " + msg);
    }

    private void getList(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(functions.getList());
    }

    private void touch(ChannelHandlerContext ctx, String fileName) {
        try {
            functions.touch(fileName);
            ctx.writeAndFlush("Ok");
        } catch (IOException e) {
            ctx.writeAndFlush("Error! File don't create");
        }
    }

    private void mkdir(ChannelHandlerContext ctx, String fileName) {
        try {
            functions.mkdir(fileName);
            ctx.writeAndFlush("Ok");
        } catch (IOException e) {
            ctx.writeAndFlush("Error! Directory don't create");
        }
    }

    private void remove(ChannelHandlerContext ctx, String name) {
        try {
            functions.remove(name);
            ctx.writeAndFlush("Ok");
        } catch (IOException e) {
            ctx.writeAndFlush("Delete error");
        }
    }
}
