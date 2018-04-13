package com.pengjinfei.proxy.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.PlatformDependent;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author PENGJINFEI533
 * @since 2018-03-20
 */
public class ChannelManager {

	private ChannelManager() {

	}
	public static ChannelManager getInstance() {
		return INSTANCE;
	}

	private static final ChannelManager INSTANCE = new ChannelManager();

	private final ConcurrentMap<String, Channel> channels = PlatformDependent.newConcurrentHashMap();

	private final ChannelFutureListener remover = future -> remove(future.channel());

	private volatile boolean isProxyWritable = true;

	private static final AttributeKey<Boolean> CHANNEL_WRITEABLE = AttributeKey.newInstance("channel_writeable");

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

	public Channel remove(Object o) {
		Channel c = null;
		if (o instanceof String) {
			c = channels.remove(o);
		} else if (o instanceof Channel) {
			c = (Channel) o;
			c = channels.remove(c.id().asLongText());
		}
		if (c == null) {
			return null;
		}
		c.closeFuture().removeListener(remover);
		return c;
	}

	public void setProxyWritable(boolean autoRead) {
		isProxyWritable = autoRead;
		channels.forEach((id,channel)->{
			Attribute<Boolean> attr = channel.attr(CHANNEL_WRITEABLE);
			if (autoRead && (attr == null || attr.get() == null || attr.get())) {
				channel.config().setAutoRead(true);
			} else {
				channel.config().setAutoRead(false);
			}
		});
	}

	public void setChannelAutoRead(Channel channel,boolean autoRead) {
		channel.attr(CHANNEL_WRITEABLE).set(autoRead);
		if (isProxyWritable && autoRead) {
			channel.config().setAutoRead(true);
		} else {
			channel.config().setAutoRead(false);
		}
	}

	public void setChannelAutoRead(String reqId, boolean autoRead) {
		Channel channel = channels.get(reqId);
		if (channel != null) {
			setChannelAutoRead(channel, autoRead);
		}
	}
}
