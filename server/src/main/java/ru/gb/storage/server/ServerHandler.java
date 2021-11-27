package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.storage.commons.message.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

        }
        if (msg instanceof StorageUpdateMessage) {
            StorageUpdateMessage message = (StorageUpdateMessage) msg;
            String login = message.getLogin();
            ArrayList<String> files = getFileList(login);
            StorageFileListMessage messageOutput = new StorageFileListMessage();
            messageOutput.setFiles(files);
            ctx.writeAndFlush(messageOutput);

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






    private ArrayList<String> getFileList(String login) throws IOException {
        ArrayList<String> files = new ArrayList<>();
        Path path = Path.of("server/cloud-storage/" + login);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                LOGGER.error("Error while creating directory at cloud storage: " + path); //TODO проверить что выдает при ошибке
                e.printStackTrace();
            }
        }

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            files.add(path.relativize(file).toString());
                            return FileVisitResult.CONTINUE;
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }


        return files;
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
