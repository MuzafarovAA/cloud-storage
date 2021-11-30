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
import ru.gb.storage.commons.message.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    public static final String HOST = "localhost";
    public static final int PORT = 9000;

    public static void main(String[] args) {

        new Client().start();

    }

    private void start() {


        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        ExecutorService threadPool = Executors.newCachedThreadPool();

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
                                    new ClientHandler(threadPool)
                            );
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channel = bootstrap.connect(HOST, PORT).sync();

//            AuthRegisterMessage message = new AuthRegisterMessage("login4", "pass4");

            AuthRequestMessage message1 = new AuthRequestMessage("login1", "pass1");

//            StorageFileDownloadMessage message = new StorageFileDownloadMessage();
//            message.setPath("testToSend.txt");

            StorageUpdateMessage message2 = new StorageUpdateMessage("login1");

//            StorageFileDeleteMessage message = new StorageFileDeleteMessage("login1", "jh");

            FileRequestMessage message3 = new FileRequestMessage("login1", "8.mp4");

//            Path filePath = Paths.get("D:\\GeekBrains\\cloud-storage\\2.mp4");
//            StorageFileAddMessage message = new StorageFileAddMessage("login1", filePath);

            channel.channel().writeAndFlush(message1);
//            channel.channel().writeAndFlush(message2);
//            channel.channel().writeAndFlush(message3);
            channel.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }


    }

}

