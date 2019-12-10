package com.akita.im.chat;

import com.akita.im.proto.Message;
import com.akita.im.session.SessionHolder;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

@Component("ChatServerHandler")
@ChannelHandler.Sharable
public class ChatServerHandler extends SimpleChannelInboundHandler<Message.Call> {
    private static ChannelGroup channels= new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive！！！");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelInactive！！！");
        SessionHolder.leave((NioSocketChannel) ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message.Call call) throws Exception {
        System.err.println("[channelRead0]:"+ call.getType());
        switch (call.getType()) {
            case AUTH:
                Message.Auth auth = Message.Auth.parseFrom(call.getContent());
                String userid = auth.getUserid().toString();
                String token = auth.getToken().toString();
                String platform = auth.getPlatform().toString();
                SessionHolder.join(userid, platform, (NioSocketChannel) ctx.channel());
                Message.Call ackAuth = Message.Call.newBuilder()
                        .setType(Message.CallType.ACK_AUTH)
                        .build();
                ctx.writeAndFlush(ackAuth);
                break;
            case HEARTBEAT:
                SessionHolder.updateBeatTime(ctx.channel(), System.currentTimeMillis());
                Message.HeartBeat heartbeat = Message.HeartBeat.parseFrom(call.getContent());
                Message.HeartBeat ackHeartbeat = Message.HeartBeat.newBuilder()
                        .setTimestamp(heartbeat.getTimestamp())
                        .setServerReplyTimestamp(Math.toIntExact(System.currentTimeMillis()))
                        .build();
                Message.Call ackHeartbeatCall = Message.Call.newBuilder()
                        .setType(Message.CallType.ACK_HEARTBEAT)
                        .setContent(ackHeartbeat.toByteString())
                        .build();
                ctx.channel().writeAndFlush(ackHeartbeatCall);
                break;
            case MESSAGE:
                Message.MsgData message = Message.MsgData.parseFrom(call.getContent());
                System.out.println(message);
                // 投送消息
                MessageHandler.deliver(message);
                // 答复
                Message.Call replay = Message.Call.newBuilder()
                        .setType(Message.CallType.ACK_MESSAGE)
                        .setContent(message.getUid())
                        .build();
                ctx.channel().writeAndFlush(replay);
                break;
            case MESSAGE_READ:
                // 消息已读 更新last_time(messageUid)
                Message.MsgRead read = Message.MsgRead.parseFrom(call.getContent());
                MessageHandler.read(read);
                Message.Call ack = Message.Call.newBuilder()
                        .setType(Message.CallType.ACK_MESSAGE_READ)
                        .setContent(read.getUid())
                        .build();
                ctx.channel().writeAndFlush(ack);
                break;
            default:
                // 弃用的类型
                break;
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("exceptionCaught！！！");
        cause.printStackTrace();
        ctx.close();
    }
}
