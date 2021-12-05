package ru.gb.storage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.storage.commons.handler.JsonDecoder;
import ru.gb.storage.commons.handler.JsonEncoder;
import ru.gb.storage.commons.message.*;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Network {

    public static final String HOST = "localhost";
    public static final int PORT = 9000;
    private ChannelFuture channel;

//    public static void main(String[] args) {
//
//        new Client().start();
//
//    }

    void start(ClientApp clientApp) {


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
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new ClientHandler(threadPool, clientApp)
                            );
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            channel = bootstrap.connect(HOST, PORT).sync();
            channel.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }


    }

    void sendAuthMessage(String login, String password) {
        AuthRequestMessage authRequestMessage = new AuthRequestMessage(login, password);
        channel.channel().writeAndFlush(authRequestMessage);
    }

    void sendRegMessage(String login, String password) {
        AuthRegisterMessage authRegisterMessage = new AuthRegisterMessage(login, password);
        channel.channel().writeAndFlush(authRegisterMessage);
    }

    public void sendUpdateRequest(String login) {
        StorageUpdateMessage storageUpdateMessage = new StorageUpdateMessage(login);
        channel.channel().writeAndFlush(storageUpdateMessage);
    }

    public void sendDeleteMessage(String login, String fileName) {
        StorageFileDeleteMessage storageFileDeleteMessage = new StorageFileDeleteMessage(login, fileName);
        channel.channel().writeAndFlush(storageFileDeleteMessage);
    }

    public void sendDownloadRequest(String login, String fileName) {
        FileRequestMessage fileRequestMessage = new FileRequestMessage();
        fileRequestMessage.setLogin(login);
        fileRequestMessage.setFileName(fileName);
        channel.channel().writeAndFlush(fileRequestMessage);
    }

    public void sendAddMessage(String login, Path filePath) {
        StorageFileAddMessage storageFileAddMessage = new StorageFileAddMessage(login, filePath);
        channel.channel().writeAndFlush(storageFileAddMessage);
    }
}

