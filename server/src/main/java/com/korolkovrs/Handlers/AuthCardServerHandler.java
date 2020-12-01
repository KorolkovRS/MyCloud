package com.korolkovrs.Handlers;

import com.korolkovrs.AuthCard;
import com.korolkovrs.DBAuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.sql.SQLException;

public class AuthCardServerHandler extends SimpleChannelInboundHandler<AuthCard> {
    private DBAuthService authService;

    public AuthCardServerHandler() {
        super();
        authService = DBAuthService.getInstance();
        try {
            authService.connect();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AuthCard authCard) throws Exception {
        System.out.println("auth");
        try {
            if (authCard.isCheckReq()) { //Если пришел запрос на регистрацию
                ctx.writeAndFlush(authService.add(authCard));
            } else { //Если запрос на аутентификацию
                ctx.writeAndFlush(authService.login(authCard));
            }
        } catch (SQLException | IOException e) {
            ctx.writeAndFlush(new AuthCard(true, null, null));
            System.out.println("Ошибка");
        }
    }
}









