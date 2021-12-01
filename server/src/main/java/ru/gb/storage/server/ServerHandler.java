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

    private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);
    private static final int BUFFER_SIZE = 65536;
    private final Executor executor;

    public ServerHandler(Executor executor) {
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        LOGGER.info("Channel read. ID: " + ctx.channel().id());
        if (msg instanceof AuthRequestMessage) {
            AuthRequestMessage message = (AuthRequestMessage) msg;
            String login = message.getLogin();
            String password = message.getPassword();
            LOGGER.info("Received new AuthMessage from user: " + login + ". ID: " + ctx.channel().id());
            AuthService authService = new AuthService();
            authService.connectToDatabase();
            AuthErrorMessage authErrorMessage = new AuthErrorMessage();
            if (authService.checkLogin(login)) {
                if (authService.checkPassword(login, password)) {
                    AuthOkMessage authOkMessage = new AuthOkMessage(login);
                    ctx.writeAndFlush(authOkMessage);
                    LOGGER.info("Successful authentication. Login: " + login + ". ID: " + ctx.channel().id());
                } else {
                    authErrorMessage.setLoginError(false);
                    authErrorMessage.setPasswordError(true);
                    ctx.writeAndFlush(authErrorMessage);
                    LOGGER.info("Authentication failed. Incorrect password for login: " + login + ". ID: " + ctx.channel().id());
                }
            } else {
                authErrorMessage.setLoginError(true);
                ctx.writeAndFlush(authErrorMessage);
                LOGGER.info("Authentication failed. Incorrect login: " + login + ". ID: " + ctx.channel().id());
            }
            authService.disconnectFromDatabase();
        }

        if (msg instanceof AuthRegisterMessage) {
            AuthRegisterMessage message = (AuthRegisterMessage) msg;
            String login = message.getLogin();
            String password = message.getPassword();
            LOGGER.info("Received new AuthRegisterMessage from user: " + login + ". ID: " + ctx.channel().id());
            AuthService authService = new AuthService();
            authService.connectToDatabase();
            AuthErrorMessage authErrorMessage = new AuthErrorMessage();
            if (!authService.checkLogin(login)) {
                if (authService.registerUser(login, password)) {
                    AuthOkMessage authOkMessage = new AuthOkMessage(login);
                    ctx.writeAndFlush(authOkMessage);
                    LOGGER.info("Registered new user. Login: " + login + ". ID: " + ctx.channel().id());
                } else {
                    authErrorMessage.setUnknownError(true);
                    ctx.writeAndFlush(authErrorMessage);
                    LOGGER.info("User registration failed." + ". ID: " + ctx.channel().id());
                }
            } else {
                authErrorMessage.setLoginError(true);
                ctx.writeAndFlush(authErrorMessage);
                LOGGER.info("User registration failed. Login is already exist: " + login + ". ID: " + ctx.channel().id());
            }
            authService.disconnectFromDatabase();
        }

        if (msg instanceof StorageUpdateMessage) {
            StorageUpdateMessage message = (StorageUpdateMessage) msg;
            String login = message.getLogin();
            LOGGER.info("Received new StorageUpdateMessage from user: " + login + ". ID: " + ctx.channel().id());
            StorageFileListMessage fileListMessage = new StorageFileListMessage();
            fileListMessage.setFiles(getFileList(login));
            ctx.writeAndFlush(fileListMessage);
            LOGGER.info("Updated file list sent to user: " + login + ". ID: " + ctx.channel().id());
        }

        if (msg instanceof StorageFileAddMessage) {
            StorageFileAddMessage message = (StorageFileAddMessage) msg;
            String login = message.getLogin();
            Path filePath = message.getFileName();
            LOGGER.info("Received new StorageFileAddMessage from user: " + login + ". ID: " + ctx.channel().id());
            String fileName = String.valueOf(filePath.getFileName());
            System.out.println(fileName);
            Path path = Paths.get("server/cloud-storage/" + login + "/" + fileName);
            if (Files.exists(path)) {
                LOGGER.info("File is already exist." + " ID: " + ctx.channel().id());
                FileErrorMessage errorMessage = new FileErrorMessage();
                errorMessage.setAlreadyExists(true);
                ctx.writeAndFlush(errorMessage);
            } else {
                FileRequestMessage fileRequestMessage = new FileRequestMessage();
                fileRequestMessage.setLogin(login);
                fileRequestMessage.setFilePath(filePath);
                ctx.writeAndFlush(fileRequestMessage);
                LOGGER.info("File request sent to user " + login + " to upload file " + filePath.toString() + ". ID: " + ctx.channel().id());
            }
        }

        if (msg instanceof FileMessage) {
            FileMessage message = (FileMessage) msg;
            String login = message.getLogin();
            String fileName = message.getFileName();
            LOGGER.info("Received new FileMessage from user: " + login + ". ID: " + ctx.channel().id());
            Path path = Paths.get("server/cloud-storage/" + login + "/" + fileName);
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(String.valueOf(path), "rw")) {
                randomAccessFile.seek(message.getStartPosition());
                randomAccessFile.write(message.getContent());
                LOGGER.info("Received file part of " + fileName + " from user " + fileName + ". ID: " + ctx.channel().id());
            } catch (FileNotFoundException e) {
                LOGGER.error("FileNotFound exception while writing a file.");
                e.printStackTrace();
            } catch (IOException e) {
                LOGGER.error("IO exception while writing a file.");
                e.printStackTrace();
            }
        }

        if (msg instanceof FileEndMessage) {
            FileEndMessage message = (FileEndMessage) msg;
            String login = message.getLogin();
            String fileName = message.getFileName();
            LOGGER.info("Received new FileEndMessage from user: " + login + ". ID: " + ctx.channel().id());
            LOGGER.info("Received file " + fileName + " from user " + login + ". ID: " + ctx.channel().id());
            StorageFileListMessage fileListMessage = new StorageFileListMessage();
            fileListMessage.setFiles(getFileList(login));
            ctx.writeAndFlush(fileListMessage);
            LOGGER.info("Updated file list sent to user: " + login + ". ID: " + ctx.channel().id());
        }

        if (msg instanceof StorageFileDeleteMessage) {
            StorageFileDeleteMessage message = (StorageFileDeleteMessage) msg;
            String login = message.getLogin();
            String fileName = message.getFileName();
            LOGGER.info("Received new StorageFileDeleteMessage from user: " + login + ". ID: " + ctx.channel().id());
            LOGGER.info("Requested deleting: " + fileName + " from user " + login + ". ID: " + ctx.channel().id());
            if (deleteFile(login, fileName)) {
                LOGGER.info("File " + fileName + " deleted." + ". ID: " + ctx.channel().id());
                ctx.writeAndFlush(new FileOkMessage());
                LOGGER.info("FileOkMessage sent to user " + login + ". ID: " + ctx.channel().id());
            } else {
                LOGGER.info("Failed to delete file " + fileName + ". ID: " + ctx.channel().id());
                FileErrorMessage errorMessage = new FileErrorMessage();
                errorMessage.setDeleteError(true);
                ctx.writeAndFlush(errorMessage);
                LOGGER.info("FileErrorMessage sent to user: " + login + ". ID: " + ctx.channel().id());
            }
            StorageFileListMessage fileListMessage = new StorageFileListMessage();
            fileListMessage.setFiles(getFileList(login));
            ctx.writeAndFlush(fileListMessage);
            LOGGER.info("Updated file list sent to user: " + login + ". ID: " + ctx.channel().id());
        }

        if (msg instanceof FileRequestMessage) {
            FileRequestMessage message = (FileRequestMessage) msg;
            String login = message.getLogin();
            String fileName = message.getFileName();
            LOGGER.info("Received new StorageFileDownloadMessage from user: " + login + ". ID: " + ctx.channel().id());
            LOGGER.info("Requested " + fileName + " from user " + login + ". ID: " + ctx.channel().id());
            if (downloadFile(login, fileName, ctx)) {
                LOGGER.info("File sent to client: " + login + ". ID: " + ctx.channel().id());
            } else {
                LOGGER.info("Failed to send file to client: " + login + ". ID: " + ctx.channel().id());
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
                    LOGGER.info("FileEndMessage sent to user " + login);

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
        LOGGER.info("Channel registered. ID: " + ctx.channel().id());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Channel unregistered. ID: " + ctx.channel().id());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Channel is active. ID: " + ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Channel is inactive. ID: " + ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getClass().getSimpleName() + " while connection. ID: " + ctx.channel().id());
        cause.printStackTrace();
        ctx.close();
    }
}
