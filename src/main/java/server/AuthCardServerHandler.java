package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import utils.AuthCard;

public class AuthCardServerHandler extends SimpleChannelInboundHandler<AuthCard> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AuthCard authCard) throws Exception {
        if (authCard.isCheckReq()) { //Если пришел запрос на регистрацию
            ctx.writeAndFlush(AuthHandler.add(authCard));
        } else { //Если запрос на аутентификацию
            ctx.writeAndFlush(AuthHandler.login(authCard));
        }
    }
}









