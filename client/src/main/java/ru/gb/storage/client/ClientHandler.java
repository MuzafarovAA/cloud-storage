package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.message.*;

import java.io.RandomAccessFile;
import java.util.ArrayList;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof TextMessage) {
            System.out.println("Received from server: " + ((TextMessage) msg).getText());
        }
        if (msg instanceof FileMessage) {
            FileMessage message = (FileMessage) msg;
            try (RandomAccessFile randomAccessFile = new RandomAccessFile("1.txt", "rw")) {
                randomAccessFile.seek(message.getStartPosition());
                randomAccessFile.write(message.getContent());
                System.out.println("Received file part from server.");
            }

        }
        if (msg instanceof FileEndMessage) {
            System.out.println("Received file from server.");
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
            ArrayList<String> files = message.getFiles();
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
        }

        if (msg instanceof FileOkMessage) {
            System.out.println("File operation succeed.");
        }
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
