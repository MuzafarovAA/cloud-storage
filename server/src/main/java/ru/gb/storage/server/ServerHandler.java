package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.storage.commons.message.*;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);
    //TODO логгирование

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof TextMessage) {
            TextMessage message = (TextMessage) msg;
            System.out.println("Received TextMessage: " + message.getText());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof AuthMessage) {
            AuthMessage message = (AuthMessage) msg;
            //TODO авторизация пользователя через БД
        }
        if (msg instanceof RegistrationMessage) {
            RegistrationMessage message = (RegistrationMessage) msg;
            //TODO регистрация пользователя в БД
        }
        if (msg instanceof StorageUpdateMessage) {
            StorageUpdateMessage message = (StorageUpdateMessage) msg;
            //TODO получение/обновление списка файлов хранилища
        }
        if (msg instanceof StorageFileAddMessage) {
            StorageFileAddMessage message = (StorageFileAddMessage) msg;
            //TODO запрос добавления файла
        }
        if (msg instanceof StorageFileDeleteMessage) {
            StorageFileDeleteMessage message = (StorageFileDeleteMessage) msg;
            //TODO запрос удаления файла
        }
        if (msg instanceof StorageFileDownloadMessage) {
            StorageFileDownloadMessage message = (StorageFileDownloadMessage) msg;
            //TODO запрос загрузки файла с хранилища
        }


    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel registered.");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel unregistered.");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel is active.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel is inactive.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
