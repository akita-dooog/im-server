package com.akita.im.session;

import com.akita.im.constants.PlatformType;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionHolder {
    private static final Map<String, NioSocketChannel> CHANNEL_MAP = new ConcurrentHashMap<>(16);

    private static AttributeKey<Long> BEAT_TIME = AttributeKey.valueOf("beatTime");
    private static AttributeKey<String> USERID_PLATFORM = AttributeKey.valueOf("useridAtPlatform");

    public static void join(String userid, String platform, NioSocketChannel channel) {
        String useridAtPlatform = userid + "@" + platform;
        channel.attr(USERID_PLATFORM).set(useridAtPlatform);
        CHANNEL_MAP.put(useridAtPlatform, channel);
    }

    public static void leave(NioSocketChannel channel) {
        CHANNEL_MAP.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == channel)
                .forEach(entry -> CHANNEL_MAP.remove(entry.getKey()));
    }



    public static void updateBeatTime(Channel channel, Long time){
        channel.attr(BEAT_TIME).set(time);
    }

    public static List<String> getAllChannelIdByUserid(String userid) {
        List<String> channelIds = new ArrayList<>();
        for (PlatformType type : PlatformType.values()) {
            channelIds.add(userid + "@" + type);
        }
        return channelIds;
    }

    public static List<String> getOnlineChannelIdByUserid(String userid) {
        List<String> channelIds = new ArrayList<>();
        for (String channelId : getAllChannelIdByUserid(userid)) {
            if (CHANNEL_MAP.get(channelId) != null) {
                channelIds.add(channelId);
            }
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
}
