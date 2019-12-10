package com.akita.im.chat;

import com.akita.im.proto.Message;
import com.akita.im.session.SessionHolder;
import io.netty.channel.Channel;

public class MessageHandler {
    public static void deliver(Message.MsgData msg) {
        // 存储消息到数据库
        // 发给to的所有设备
        for (Channel channel : SessionHolder.getOnlineChannelsByUserid(msg.getTo().toString())) {
            Message.Call messageCall =
                    Message.Call.newBuilder()
                            .setType(Message.CallType.MESSAGE)
                            .setContent(msg.toByteString())
                            .build();
            channel.writeAndFlush(messageCall);
        }
    }

    public static void read(Message.MsgRead read) {

    }
}
