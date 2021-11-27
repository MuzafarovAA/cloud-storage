package ru.gb.storage.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.storage.commons.handler.JsonDecoder;
import ru.gb.storage.commons.handler.JsonEncoder;
import ru.gb.storage.commons.message.AuthRegisterMessage;
import ru.gb.storage.commons.message.AuthRequestMessage;
import ru.gb.storage.commons.message.StorageFileDownloadMessage;

public class Client {

    public static final String HOST = "localhost";
    public static final int PORT = 9000;

    public static void main(String[] args) {

        new Client().start();

    }

    private void start() {


        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024,0,3,0,3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new ClientHandler()
                            );
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channel = bootstrap.connect(HOST, PORT).sync();

            AuthRegisterMessage message = new AuthRegisterMessage("login4", "pass4");

//            AuthRequestMessage message = new AuthRequestMessage("login2", "pass2");

//            StorageFileDownloadMessage message = new StorageFileDownloadMessage();
//            message.setPath("testToSend.txt");

            channel.channel().writeAndFlush(message);
            channel.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }


    }

}

