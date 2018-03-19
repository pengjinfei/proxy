package com.pengjinfei.proxy.server.handler;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.experimental.UtilityClass;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@UtilityClass
public class ProxyChannelManager {

    /**
     * ports of a client server
     */
    private static final AttributeKey<List<Channel>> PROXY_FACADE = AttributeKey.valueOf("proxy_facade");

    private static final ConcurrentHashMap<String, Channel> facadeChannelMapping = new ConcurrentHashMap<String, Channel>();

    public static void addFacadeChannel2ProxyChannel(Channel proxyChannel, Channel facadeChannel) {
        List<Channel> channels = proxyChannel.attr(PROXY_FACADE).get();
        if (channels == null) {
            channels = new LinkedList<Channel>();
        }
        channels.add(facadeChannel);
    }

    public static void addFacadeClientChannel(String reqId, Channel channel) {
        facadeChannelMapping.put(reqId, channel);
    }

    public static void removeFacadeClientChannel(String reqId) {
        facadeChannelMapping.remove(reqId);
    }

    public static Channel getFacadeClientChannelByReqid(String reqid) {
        return facadeChannelMapping.get(reqid);
    }
}
