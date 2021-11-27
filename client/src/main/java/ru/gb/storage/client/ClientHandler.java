package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.message.FileEndMessage;
import ru.gb.storage.commons.message.FileMessage;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.message.TextMessage;

import java.io.RandomAccessFile;

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
            ctx.close();
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
