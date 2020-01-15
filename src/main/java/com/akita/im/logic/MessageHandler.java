package com.akita.im.logic;

import com.akita.im.logic.model.LastReadMessageInfo;
import com.akita.im.logic.model.MessageInfo;
import com.akita.im.cache.SessionCache;
import com.akita.im.protobuf.Message;
import com.akita.im.logic.service.LastReadService;
import com.akita.im.logic.service.MessageInfoService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.akita.im.cache.SessionCache.*;
import static com.akita.im.protobuf.Message.MsgType.*;

/**
 * 消息处理
 * */
@Component
public class MessageHandler {
    @Autowired
    MessageInfoService messageService;

    @Autowired
    LastReadService readService;

    /**
     * 接收消息
     * */
    public void receive(ChannelHandlerContext ctx, Message.Call call) throws InvalidProtocolBufferException, UnsupportedEncodingException {
        switch (call.getType()) {
            case Message.CallType.AUTH_VALUE:
                Message.Auth auth = Message.Auth.parseFrom(call.getContent());
                String userid = auth.getUserid();
                String platform = auth.getPlatform().name();
                String channelId = SessionCache.getChannelIdByUserid(userid, platform);
                SessionCache.join(channelId, (NioSocketChannel) ctx.channel());
                // ack
                Message.Call ackAuth = Message.Call.newBuilder()
                        .setType(Message.CallType.AUTH_VALUE)
                        .build();
                send(ctx.channel(), ackAuth);
                break;
            case Message.CallType.HEARTBEAT_VALUE:
                // 心跳直接ack
                send(ctx.channel(), call);
                break;
            case Message.CallType.MESSAGE_VALUE:
                // 投送消息
                deliver(ctx.channel(), call);
                break;
            case Message.CallType.MESSAGE_READ_VALUE:
                // 消息已读 更新last_time(messageUid)
                read(ctx.channel(), call);
                break;
            default:
                // 弃用的类型
                break;
        }
    }

    /**
     * 投递消息
     * */
    private void deliver(Channel sender, Message.Call call) throws InvalidProtocolBufferException, UnsupportedEncodingException {
        Message.MsgData msgData = Message.MsgData.parseFrom(call.getContent());
        // 存储消息到数据库
        if (saveMessage(msgData)) {
            // 成功则判断对方是否在线
            List<Channel> onlineChannels = getOnlineChannelsByUserid(msgData.getTo().toString());
            if (onlineChannels.size() > 0) {
                // 推送消息给在线用户
                send(onlineChannels, call);
            } else {
                // 已经存入离线消息， 反馈给发送者发送成功
                Message.Call ack = Message.Call.newBuilder()
                        .setType(Message.CallType.ACK_MESSAGE_VALUE)
                        .setContent(ByteString.copyFromUtf8(msgData.getUid()))
                        .build();
                send(sender, ack);
            }
        }
    }

    /**
     *
     * */
    private void read(Channel channel, Message.Call call) throws InvalidProtocolBufferException {
        Message.MsgRead read = Message.MsgRead.parseFrom(call.getContent());
        // 更新最新读取的消息
        if (updateLastReadMessageId(read)) {
            // 更新成功通知读取者
            Message.Call ack = Message.Call.newBuilder()
                    .setType(Message.CallType.ACK_MESSAGE_READ_VALUE)
                    .setContent(ByteString.copyFromUtf8(read.getUid()))
                    .build();
            send(channel, ack);

            // 通知发送者
            List<Channel> sender = getOnlineChannelsByUserid(read.getFrom().toString());
            Message.Call ackSender = Message.Call.newBuilder()
                    .setType(Message.CallType.ACK_MESSAGE_VALUE)
                    .setContent(ByteString.copyFromUtf8(read.getUid()))
                    .build();
            send(sender, ack);
        }
    }

    /**
     * 发送消息
     * */
    private void send(Channel channel, Message.Call call) {
        channel.writeAndFlush(call);
    }

    private void send(List<Channel> channels, Message.Call call) {
        for (Channel channel : channels) {
            channel.writeAndFlush(call);
        }
    }

    /**
     * 存储消息
     * */
    private boolean saveMessage(Message.MsgData msgData) throws InvalidProtocolBufferException, UnsupportedEncodingException {
        MessageInfo.MessageInfoBuilder msgBuilder = MessageInfo.builder();
        String msgFrom = msgData.getFrom();
        msgBuilder.uid(msgData.getUid())
                .type(msgData.getType())
                .msgFrom(msgFrom)
                .msgTo(msgData.getTo())
                .groupId(msgData.getGroupid());

        switch (msgData.getType()) {
            case PICTURE_VALUE:
            case FILE_VALUE:
                Message.MsgContentFile file = Message.MsgContentFile.parseFrom(msgData.getContent());
                msgBuilder.content(file.getFileName())
                        .size(Integer.toUnsignedLong(file.getFileSize()))
                        .url(file.getFileUrl())
                        .fileType(file.getFileType());
            case MEDIA_VOICE_VALUE:
            case MEDIA_VIDEO_VALUE:
                Message.MsgContentMedia media = Message.MsgContentMedia.parseFrom(msgData.getContent());
                msgBuilder.url(media.getMediaUrl())
                        .duration(Integer.toUnsignedLong(media.getDuration()))
                        .size(Integer.toUnsignedLong(media.getFileSize()))
                        .trumbnail(media.getThumbnailUrl())
                        .fileType(media.getMediaType().name());
            default:
                Message.MsgContentText text = Message.MsgContentText.parseFrom(msgData.getContent());
                msgBuilder.content(text.getText());
                break;
        }

        msgBuilder.createdUser(msgFrom)
                .updatedUser(msgFrom);
        MessageInfo info = msgBuilder.build();
        return messageService.save(info);
    }

    private boolean updateLastReadMessageId(Message.MsgRead read) {
        LastReadMessageInfo info = LastReadMessageInfo.builder()
                .userid(read.getTo())
                .platform(read.getPlatform().name())
                .lastReadMessageId(read.getUid())
                .build();

        UpdateWrapper<LastReadMessageInfo> update = new UpdateWrapper<>();
        update.eq("userid", info.getUserid())
                .eq("platform", info.getPlatform());

        return readService.saveOrUpdate(info, update);
    }
}
