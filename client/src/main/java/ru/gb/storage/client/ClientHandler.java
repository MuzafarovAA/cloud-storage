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
    private Executor executor;

    public ClientHandler(Executor executor) {
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof TextMessage) {
            System.out.println("Received from server: " + ((TextMessage) msg).getText());
        }
        if (msg instanceof FileMessage) {
            FileMessage message = (FileMessage) msg;
            String fileName = message.getFileName();
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, "rw")) {
                randomAccessFile.seek(message.getStartPosition());
                randomAccessFile.write(message.getContent());
                System.out.println("Received file part from server.");
            }

        }
        if (msg instanceof FileEndMessage) {
            FileEndMessage message = (FileEndMessage) msg;
            System.out.println("Received file from server " + message.getFileName());
        }

        if (msg instanceof FileRequestMessage) {
            FileRequestMessage message = (FileRequestMessage) msg;
            String login = message.getLogin();
            Path filePath = message.getFilePath();
            if (uploadFile(login, filePath, ctx)) {
                System.out.println("File sent to client: " + login);
            } else {
                System.out.println("Failed to send file to client: " + login);
            }

        }

        if (msg instanceof AuthOkMessage) {
            AuthOkMessage message = (AuthOkMessage) msg;
            System.out.println("Auth Ok received. Login: " + message.getLogin());
        }

        if (msg instanceof AuthErrorMessage) {
            AuthErrorMessage message = (AuthErrorMessage) msg;
            if (message.isLoginError()) {
                System.out.println("Wrong login.");
            } else if (message.isPasswordError()) {
                System.out.println("Wrong password.");
            }
            ctx.close();
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
