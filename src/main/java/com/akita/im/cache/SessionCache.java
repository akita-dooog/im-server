package com.akita.im.cache;

import com.akita.im.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionCache {
    private static final Map<String, NioSocketChannel> CHANNEL_MAP = new ConcurrentHashMap<>(16);

    private static AttributeKey<String> CHANNEL_ID = AttributeKey.valueOf("channelId");

    public static void join(String channelId, NioSocketChannel channel) {
        channel.attr(CHANNEL_ID).set(channelId);
        CHANNEL_MAP.put(channelId, channel);
    }

    public static void leave(NioSocketChannel channel) {
        CHANNEL_MAP.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == channel)
                .forEach(entry -> CHANNEL_MAP.remove(entry.getKey()));
    }

    public static List<String> getAllChannelIdByUserid(String userid) {
        List<String> channelIds = new ArrayList<>();
        for (Message.PlatformType type : Message.PlatformType.values()) {
            if (type == Message.PlatformType.UNRECOGNIZED) continue;
            channelIds.add(userid + "@" + type);
        }
        return channelIds;
    }

    public static List<Channel> getOnlineChannelsByUserid(String userid) {
        List<Channel> channels = new ArrayList<>();
        for (String channelId : getAllChannelIdByUserid(userid)) {
            if (CHANNEL_MAP.get(channelId) != null) {
                channels.add(CHANNEL_MAP.get(channelId));
            }
        }
        return channels;
    }

    public static Channel getOnlineChannel(String channelId) {
        return CHANNEL_MAP.get(channelId);
    }

    public static String getChannelIdByUserid(String userid, String platform) {
        return  userid + "@" + platform;
    }
}
