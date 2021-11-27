package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.storage.commons.message.*;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);
    //TODO логгирование уровня INFO сделать из класса ServerHandler, не AuthService. AuthService сделать только логгирование уровня ERROR

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof TextMessage) {
            TextMessage message = (TextMessage) msg;
            System.out.println("Received TextMessage: " + message.getText());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof AuthRequestMessage) {
            LOGGER.info("Received new AuthMessage.");
            AuthRequestMessage message = (AuthRequestMessage) msg;
            String login = message.getLogin();
            String password = message.getPassword();
            message.getPassword();
            AuthService authService = new AuthService();
            authService.connectToDatabase();
            AuthErrorMessage authErrorMessage = new AuthErrorMessage();
            if (authService.checkLogin(login)) {
                if (authService.checkPassword(login, password)) {
                    AuthOkMessage authOkMessage = new AuthOkMessage(login);
                    ctx.writeAndFlush(authOkMessage);
                    LOGGER.info("Successful authentication. Login: " + login);
                } else {
                    authErrorMessage.setLoginError(false);
                    authErrorMessage.setPasswordError(true);
                    ctx.writeAndFlush(authErrorMessage);
                    LOGGER.info("Authentication failed. Incorrect password. Login: " + login);
                }
            } else {
                authErrorMessage.setLoginError(true);
                ctx.writeAndFlush(authErrorMessage);
                LOGGER.info("Authentication failed. Incorrect login: " + login);
            }
            authService.disconnectFromDatabase();

        }
        if (msg instanceof AuthRegisterMessage) {
            LOGGER.info("Received new AuthRegisterMessage");
            AuthRegisterMessage message = (AuthRegisterMessage) msg;
            String login = message.getLogin();
            String password = message.getPassword();
            AuthService authService = new AuthService();
            authService.connectToDatabase();
            AuthErrorMessage authErrorMessage = new AuthErrorMessage();
            if (!authService.checkLogin(login)) {
                if (authService.registerUser(login, password)) {
                    AuthOkMessage authOkMessage = new AuthOkMessage(login);
                    ctx.writeAndFlush(authOkMessage);
                    LOGGER.info("Registered new user. Login: " + login);
                }
            } else {
                authErrorMessage.setLoginError(true);
                ctx.writeAndFlush(authErrorMessage);
                LOGGER.info("Registering new user failed. Login is already exist: " + login);
            }
            authService.disconnectFromDatabase();

            //TODO проверить регистрацию пользователя в БД

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
            LOGGER.info("File request message received of File: " + message.getPath());
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
