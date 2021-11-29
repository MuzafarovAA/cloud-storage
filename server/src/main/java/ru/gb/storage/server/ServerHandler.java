package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.storage.commons.message.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);

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
            LOGGER.info("Received new StorageUpdateMessage");
            StorageUpdateMessage message = (StorageUpdateMessage) msg;
            String login = message.getLogin();
            List<String> files = getFileList(login);
            LOGGER.info("File list updated.");
            StorageFileListMessage messageOutput = new StorageFileListMessage();
            messageOutput.setFiles(files);
            ctx.writeAndFlush(messageOutput);
            LOGGER.info("Sent file list to client.");

        }
        if (msg instanceof StorageFileAddMessage) {
            StorageFileAddMessage message = (StorageFileAddMessage) msg;
            //TODO запрос добавления файла
        }
        if (msg instanceof StorageFileDeleteMessage) {
            LOGGER.info("Received new StorageFileDeleteMessage");
            StorageFileDeleteMessage message = (StorageFileDeleteMessage) msg;
            String login = message.getLogin();
            String fileName = message.getFileName();
            LOGGER.info("Requested deleting: " + fileName);
            if (deleteFile(login, fileName)) {
                LOGGER.info("File " + fileName + " deleted.");
                ctx.writeAndFlush(new FileOkMessage());
            } else {
                LOGGER.info("Failed to delete file " + fileName);
                FileErrorMessage errorMessage = new FileErrorMessage();
                errorMessage.setDeleteError(true);
                ctx.writeAndFlush(errorMessage);
            }
            List<String> files = getFileList(login);
            StorageFileListMessage fileListMessage = new StorageFileListMessage();
            fileListMessage.setFiles(files);
            ctx.writeAndFlush(fileListMessage);
            LOGGER.info("Sent updated file list to client.");

        }
        if (msg instanceof StorageFileDownloadMessage) {
            StorageFileDownloadMessage message = (StorageFileDownloadMessage) msg;
            LOGGER.info("File request message received of File: " + message.getPath());
            //TODO запрос загрузки файла с хранилища
        }




        }

    private boolean deleteFile(String login, String fileName) {
        Path path = Paths.get("server/cloud-storage/" + login + "/" + fileName);
        try {
            Files.delete(path);
            return true;
        } catch (NoSuchFileException e) {
            LOGGER.error("NoSuchFileException while deleting file: " + path);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("IOException while deleting file: " + path);
            e.printStackTrace();
        }
        return false;
    }


    private List<String> getFileList(String login) {
        List<String> files = new ArrayList<>();
        Path path = Path.of("server/cloud-storage/" + login);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                LOGGER.error("IOException while creating directory at cloud storage: " + path); //TODO проверить что выдает при ошибке
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
            LOGGER.error("IOException while walkFileTree");
            e.printStackTrace();
        }

        if (files.size() == 0) {
            return null;
        }

        return files;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Channel registered.");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Channel unregistered.");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Channel is active.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Channel is inactive.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Exception while channel activity.");
        cause.printStackTrace();
        ctx.close();
    }
}
