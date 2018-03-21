package com.pengjinfei.proxy.channel;

import io.netty.channel.*;
import io.netty.util.internal.PlatformDependent;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author PENGJINFEI533
 * @since 2018-03-20
 */
public class ChannelManager {

	private final ConcurrentMap<String, Channel> channels = PlatformDependent.newConcurrentHashMap();

	private final ChannelFutureListener remover = future -> remove(future.channel());

	public Channel find(String id) {
		return channels.get(id);
	}

	public void close() {
		for (Map.Entry<String, Channel> entry : channels.entrySet()) {
			entry.getValue().close();
		}
	}

	public boolean add(Channel channel) {
		return add(channel.id().asLongText(),channel);
	}

	public boolean add(String id, Channel channel) {
		boolean add = channels.putIfAbsent(id, channel) == null;
		if (add) {
			channel.closeFuture().addListener(remover);
		}
		return add;
	}

	private boolean remove(Object o) {
		Channel c = null;
		if (o instanceof String) {
			c = channels.remove(o);
		} else if (o instanceof Channel) {
			c = (Channel) o;
			c = channels.remove(c.id().asLongText());
		}
		if (c == null) {
			return false;
		}
		c.closeFuture().removeListener(remover);
		return true;
	}
}
