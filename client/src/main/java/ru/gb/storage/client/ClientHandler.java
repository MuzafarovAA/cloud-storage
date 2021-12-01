package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.message.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    private static final int BUFFER_SIZE = 65536;
    private final Executor executor;
    private final ClientApp clientApp;
    private String errorText;

    public ClientHandler(Executor executor, ClientApp clientApp) {
        this.executor = executor;
        this.clientApp = clientApp;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        //TODO файл с таким именем уже существует
        if (msg instanceof FileMessage) {
            FileMessage message = (FileMessage) msg;
            String login = message.getLogin();
            String fileName = message.getFileName();
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(("local-storage/" + login + "/" + fileName), "rw")) {
                randomAccessFile.seek(message.getStartPosition());
                randomAccessFile.write(message.getContent());
                System.out.println("Received file part from server.");
            }

        }
        if (msg instanceof FileEndMessage) {
            FileEndMessage message = (FileEndMessage) msg;
            String login = message.getLogin();
            System.out.println("Received file from server " + message.getFileName());
            clientApp.updateLocalFiles(login);
        }

        if (msg instanceof FileRequestMessage) {
            FileRequestMessage message = (FileRequestMessage) msg;
            String login = message.getLogin();
            Path filePath = message.getFilePath();
            if (uploadFile(login, filePath, ctx)) {
                System.out.println("File sent to server.");
            } else {
                System.out.println("Failed to send file to server.");
            }

        }

        if (msg instanceof AuthOkMessage) {
            AuthOkMessage message = (AuthOkMessage) msg;
            String login = message.getLogin();
            System.out.println("Auth Ok received. Login: " + login);
            clientApp.setAuthOk(login);
            StorageUpdateMessage storageUpdateMessage = new StorageUpdateMessage(login);
        }

        if (msg instanceof AuthErrorMessage) {
            AuthErrorMessage message = (AuthErrorMessage) msg;
            if (message.isLoginError()) {
                errorText = "Wrong login.";
            } else if (message.isPasswordError()) {
                errorText = "Wrong password.";
            } else if (message.isUnknownError()) {
                errorText = "Unknown error.";
            }
            System.out.println(errorText);
            clientApp.setAuthError(errorText);
        }

        if (msg instanceof StorageFileListMessage) {
            StorageFileListMessage message = (StorageFileListMessage) msg;
            List<String> files = message.getFiles();
            if (files == null) {
                System.out.println("Empty.");
            } else {
                System.out.println("Files in storage:");
                for (int i = 0; i < files.size(); i++) {
                    System.out.println(files.get(i));
                }
            }
            clientApp.setStorageFileList(files);
        }

        if (msg instanceof FileErrorMessage) {
            FileErrorMessage message = (FileErrorMessage) msg;
            if (message.isDeleteError()) {
                System.out.println("Failed to delete file.");
            }
            if (message.isAlreadyExists()) {
                System.out.println("File is already exists.");
            }
        }

        if (msg instanceof FileOkMessage) {
            System.out.println("File operation succeed.");
        }


    }

    private boolean uploadFile(String login, Path filePath, ChannelHandlerContext ctx) {
        String fileName = filePath.getFileName().toString();
        if (Files.exists(filePath)) {
            executor.execute(() -> {

                try (RandomAccessFile randomAccessFile = new RandomAccessFile(String.valueOf(filePath), "r")) {
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
                        fileMessage.setLogin(login);
                        fileMessage.setFileName(fileName);
                        fileMessage.setContent(bytes);
                        fileMessage.setStartPosition(position);
                        ctx.writeAndFlush(fileMessage).sync();
                        System.out.println("Sent file part to server.");
                    } while (randomAccessFile.getFilePointer() < fileLength);

                    ctx.writeAndFlush(new FileEndMessage(login, fileName));
                    System.out.println("Sent file to server.");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            return true;
        }
        return false;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Connected to server.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
