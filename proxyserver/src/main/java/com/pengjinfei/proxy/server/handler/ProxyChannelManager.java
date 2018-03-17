package com.pengjinfei.proxy.server.handler;

import io.netty.channel.Channel;
import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@UtilityClass
public class ProxyChannelManager {

    /**
     * key: facade server port
     * value: channel from client server
     */
    private static ConcurrentHashMap<Integer, Channel> portChannelMapping = new ConcurrentHashMap<Integer, Channel>();

    public static void addChannelMapping(Integer port, Channel channel) {
        Channel ch = portChannelMapping.putIfAbsent(port, channel);
        if (ch != null) {
            throw new RuntimeException("Port " + port + " is in use");
        }
    }

    public static boolean isPortAvailable(Integer port) {
        return !portChannelMapping.containsKey(port);
    }
}
