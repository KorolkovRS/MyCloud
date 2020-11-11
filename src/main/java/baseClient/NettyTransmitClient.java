package baseClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import utils.CommandCode;

/**
 * Nio клиент.
 * Открывает канал и держит его открытым пока не будет вызван метод disconnect().
 * Функции, чтобы дергать их UI-клиентом.
 */


public class NettyTransmitClient {
    private String host;
    private int port;
    private Channel channel;
    EventLoopGroup worker;

    public NettyTransmitClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start()  {
        worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new StringDecoder(),
                                new StringEncoder(),
                                new TransmitClientHandler()
                        );
                    }
                });
        ChannelFuture future;
        try {
            future = bootstrap.connect(host, port).sync();
            channel = future.sync().channel();
        } catch (InterruptedException e) {
            worker.shutdownGracefully();
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        channel.writeAndFlush(CommandCode.MSG.getTitle() + " " + message);
    }

    public void list() {
        channel.writeAndFlush(CommandCode.LIST.getTitle());
    }

    public void touch(String fileName) {
        channel.writeAndFlush(CommandCode.TOUCH.getTitle() + " " + fileName);
    }

    public void mkdir(String fileName) {
        channel.writeAndFlush(CommandCode.MKDIR.getTitle() + " " + fileName);
    }

    public void remove(String fileName) {
        channel.writeAndFlush(CommandCode.REMOVE.getTitle() + " " + fileName);
    }

    public void disconnect() throws InterruptedException {
        worker.shutdownGracefully();
        channel.closeFuture().sync();
    }
}
