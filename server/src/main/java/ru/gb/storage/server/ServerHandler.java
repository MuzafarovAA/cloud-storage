package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.storage.commons.message.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    //TODO добавить в логгирование логин пользователя

    private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);
    private static final int BUFFER_SIZE = 65536;
    private final Executor executor;

    public ServerHandler(Executor executor) {
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
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
            StorageFileListMessage fileListMessage = new StorageFileListMessage();
            fileListMessage.setFiles(getFileList(login));
            LOGGER.info("File list updated.");
            ctx.writeAndFlush(fileListMessage);
            LOGGER.info("Sent file list to client.");

        }
        if (msg instanceof StorageFileAddMessage) {
            LOGGER.info("New StorageFileAddMessage received.");
            StorageFileAddMessage message = (StorageFileAddMessage) msg;
            String login = message.getLogin();
            Path filePath = message.getFileName();
            String fileName = String.valueOf(filePath.getFileName());
            System.out.println(fileName);
            Path path = Paths.get("server/cloud-storage/" + login + "/" + fileName);
            if (Files.exists(path)) {
                LOGGER.info("File is already exist.");
                FileErrorMessage errorMessage = new FileErrorMessage();
                errorMessage.setAlreadyExists(true);
                ctx.writeAndFlush(errorMessage);
            } else {
                FileRequestMessage fileRequestMessage = new FileRequestMessage();
                fileRequestMessage.setLogin(login);
                fileRequestMessage.setFilePath(filePath);
                ctx.writeAndFlush(fileRequestMessage);
                LOGGER.info("File request sent to user " + login + " to upload file " + filePath.toString());
            }
        }

        if (msg instanceof FileMessage) {
            FileMessage message = (FileMessage) msg;
            String login = message.getLogin();
            String fileName = message.getFileName();
            Path path = Paths.get("server/cloud-storage/" + login + "/" + fileName);
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(String.valueOf(path), "rw")) {
                randomAccessFile.seek(message.getStartPosition());
                randomAccessFile.write(message.getContent());
                LOGGER.info("Received file part from user " + login);
            } catch (FileNotFoundException e) {
                LOGGER.error("FileNotFound exception while writing a file.");
                e.printStackTrace();
            } catch (IOException e) {
                LOGGER.error("IO exception while writing a file.");
                e.printStackTrace();
            }

        }
        if (msg instanceof FileEndMessage) {
            LOGGER.info("Received new FileEndMessage.");
            FileEndMessage message = (FileEndMessage) msg;
            String login = message.getLogin();
            String fileName = message.getFileName();
            LOGGER.info("Received file " + fileName + " from user " + login);
            StorageFileListMessage fileListMessage = new StorageFileListMessage();
            fileListMessage.setFiles(getFileList(login));
            LOGGER.info("File list updated.");
            ctx.writeAndFlush(fileListMessage);
            LOGGER.info("File list sent to client: " + login);
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
            StorageFileListMessage fileListMessage = new StorageFileListMessage();
            fileListMessage.setFiles(getFileList(login));
            LOGGER.info("File list updated.");
            ctx.writeAndFlush(fileListMessage);
            LOGGER.info("File list sent to client: " + login);

        }
        if (msg instanceof FileRequestMessage) {
            LOGGER.info("Received new StorageFileDownloadMessage");
            FileRequestMessage message = (FileRequestMessage) msg;
            String login = message.getLogin();
            String fileName = message.getFileName();
            LOGGER.info("FileRequestMessage from " + login + ". File: " + fileName);

            if (downloadFile(login, fileName, ctx)) {
                LOGGER.info("File sent to client: " + login);
            } else {
                LOGGER.info("Failed to send file to client: " + login);
            }

        }




        }

    private boolean downloadFile(String login, String fileName, ChannelHandlerContext ctx) {
        Path path = Paths.get("server/cloud-storage/" + login + "/" + fileName);
        if (Files.exists(path)) {
            executor.execute(() -> {

                try (RandomAccessFile randomAccessFile = new RandomAccessFile(String.valueOf(path), "r")) {
                    long fileLength = randomAccessFile.length();
                    
                    do {
                        long position = randomAccessFile.getFilePointer();
                        long availableBytes = fileLength - position;
                        byte[] bytes;
                        if (availableBytes >= BUFFER_SIZE) {
                            bytes = new byte[BUFFER_SIZE];
                        } else {
                            bytes = new byte[(int) availableBytes];
                        }
                        randomAccessFile.read(bytes);
                        FileMessage fileMessage = new FileMessage();
                        fileMessage.setFileName(fileName);
                        fileMessage.setContent(bytes);
                        fileMessage.setStartPosition(position);
                        ctx.writeAndFlush(fileMessage).sync();
                        LOGGER.info("Sent file part to " + login);
                    } while (randomAccessFile.getFilePointer() < fileLength);

                    ctx.writeAndFlush(new FileEndMessage(login, fileName));
                    LOGGER.info("File transfer complete " + fileName + " to user " + login);

                } catch (FileNotFoundException e) {
                    LOGGER.error("FileNotFoundException while downloading file.");
                    e.printStackTrace();
                } catch (IOException e) {
                    LOGGER.error("IOException while downloading file.");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    LOGGER.error("InterruptedException while downloading file.");
                    e.printStackTrace();
                }
            });
            return true;
        }
        return false;
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
                LOGGER.error("IOException while creating directory at cloud storage: " + path);
                e.printStackTrace();
            }
        }

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
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
