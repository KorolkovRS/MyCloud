package server.Handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import server.BaseAuthService;
import server.DBAuthService;
import utils.AuthCard;

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









