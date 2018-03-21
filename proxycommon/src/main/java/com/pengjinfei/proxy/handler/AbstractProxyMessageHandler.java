package com.pengjinfei.proxy.handler;

import com.pengjinfei.proxy.channel.ChannelManager;
import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created on 3/18/18
 *
 * @author Pengjinfei
 */
public abstract class AbstractProxyMessageHandler extends SimpleChannelInboundHandler<ProxyMessage> {

	protected ChannelManager manager = new ChannelManager();

	@Override
	protected void messageReceived(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) throws Exception {
		MessageType messageType = proxyMessage.getMessageType();
		switch (messageType) {
			case CONNECT_REQ:
				handleReq(channelHandlerContext, proxyMessage);
				break;
			case DATA:
				handleData(channelHandlerContext, proxyMessage);
				break;
			case CONNECT_RESP:
				handleResp(channelHandlerContext, proxyMessage);
			case HEART_BEAT_REQ:
				handleHeartBeatReq(channelHandlerContext, proxyMessage);
				break;
			case HEART_BEAT_RESP:
				handleHeartBeatResp(channelHandlerContext, proxyMessage);
				break;
			default:
				break;
		}
	}

	protected void handleHeartBeatResp(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
	}

	protected void handleHeartBeatReq(ChannelHandlerContext context, ProxyMessage message) {

	}

	protected void handleResp(ChannelHandlerContext handlerContext, ProxyMessage message) {

	}

	protected abstract void handleData(ChannelHandlerContext context, ProxyMessage message);

	protected void writeData2RealServer(ChannelHandlerContext ctx, Channel channel, byte[] data) {
		ByteBuf buf = ctx.alloc().buffer(data.length);
		buf.writeBytes(data);
		channel.writeAndFlush(buf);
	}

	protected void handleReq(ChannelHandlerContext context, ProxyMessage message) {

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		manager.close();
		super.channelInactive(ctx);
	}
}
