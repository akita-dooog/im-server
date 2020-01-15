package com.akita.im.router.gateway;

import com.akita.im.logic.MessageHandler;
import com.akita.im.cache.SessionCache;
import com.akita.im.protobuf.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component("ChatServerHandler")
@ChannelHandler.Sharable
public class ChatServerHandler extends SimpleChannelInboundHandler<Message.Call> {
    @Autowired
    MessageHandler messageHandler;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("channelInactive！！！");
        SessionCache.leave((NioSocketChannel) ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message.Call call) throws InvalidProtocolBufferException, UnsupportedEncodingException {
        System.err.println("[channelRead0]:"+ call.getType());
        messageHandler.receive(ctx, call);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("exceptionCaught！！！");
        cause.printStackTrace();
//        ctx.close();
    }
}
