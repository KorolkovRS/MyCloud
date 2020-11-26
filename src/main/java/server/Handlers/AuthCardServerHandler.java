package server.Handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import server.BaseAuthService;
import utils.AuthCard;

public class AuthCardServerHandler extends SimpleChannelInboundHandler<AuthCard> {
    private BaseAuthService authService;

    public AuthCardServerHandler() {
        super();
        authService = BaseAuthService.getInstance();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AuthCard authCard) throws Exception {
        if (authCard.isCheckReq()) { //Если пришел запрос на регистрацию
            ctx.writeAndFlush(authService.add(authCard));
        } else { //Если запрос на аутентификацию
            ctx.writeAndFlush(authService.login(authCard));
        }
    }
}









