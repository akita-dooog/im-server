package com.akita.im.chat;

import com.akita.im.proto.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

@Component("WebChatServerInitializer")
public class WebChatServerInitializer extends ChannelInitializer<NioSocketChannel> {
    @Autowired
    private ChatServerHandler handler;


    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        ChannelPipeline pipeline = nioSocketChannel.pipeline();
        // websocket协议本身是基于http协议的，所以这边也要使用http解编码器
        pipeline.addLast(new HttpServerCodec());
        // 用于向客户端发送Html5文件，主要用于支持浏览器和服务端进行WebSocket通信
        pipeline.addLast(new ChunkedWriteHandler());
        // netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
        pipeline.addLast(new HttpObjectAggregator(1024*64));
        pipeline.addLast(new WebSocketServerProtocolHandler("/chat"));
        pipeline.addLast(new MessageToMessageDecoder<WebSocketFrame>() {
            @Override
            protected void decode(ChannelHandlerContext channelHandlerContext, WebSocketFrame frame, List<Object> in) throws Exception {
                ByteBuf buf = ((BinaryWebSocketFrame) frame).content();
                in.add(buf);
                buf.retain();
            }
        });
        pipeline.addLast(new MessageToMessageEncoder<Message.CallOrBuilder>() {
            @Override
            protected void encode(ChannelHandlerContext channelHandlerContext, Message.CallOrBuilder call, List<Object> out) throws Exception {
                ByteBuf result = null;
                if (call instanceof Message.Call) {
                    result = wrappedBuffer(((Message.Call) call).toByteArray());
                } else if (call instanceof Message.Call.Builder) {
                    result = wrappedBuffer(((Message.Call.Builder) call).build().toByteArray());
                }
                if (result != null) {
                    WebSocketFrame frame = new BinaryWebSocketFrame(result);
                    out.add(frame);
                }
            }
        });
        //解码器，通过Google Protocol Buffers序列化框架动态的切割接收到的ByteBuf
//        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        //服务器端接收的是客户端OPtion，所以这边将接收对象进行解码生产实列
        pipeline.addLast(new ProtobufDecoder(Message.Call.getDefaultInstance()));
        //Google Protocol Buffers编码器
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());

        pipeline.addLast(handler);
    }
}
