package com.pengjinfei.proxy.handler;

import com.pengjinfei.proxy.channel.ChannelManager;
import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Created on 3/18/18
 *
 * @author Pengjinfei
 */
@Slf4j
public abstract class AbstractProxyMessageHandler extends SimpleChannelInboundHandler<ProxyMessage> {

	protected ChannelManager manager = new ChannelManager();

	@Override
	protected void messageReceived(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) throws Exception {
		MessageType messageType = proxyMessage.getMessageType();
		switch (messageType) {
			case PROXY_REQ:
				handleReq(channelHandlerContext, proxyMessage);
				break;
			case DATA:
				handleData(channelHandlerContext, proxyMessage);
				break;
			case PROXY_RESP:
				handleResp(channelHandlerContext, proxyMessage);
			case HEART_BEAT_REQ:
				handleHeartBeatReq(channelHandlerContext, proxyMessage);
				break;
			case HEART_BEAT_RESP:
				handleHeartBeatResp(channelHandlerContext, proxyMessage);
				break;
			case DISCONNECT:
				handleDisconnect(channelHandlerContext, proxyMessage);
				break;
			default:
				break;
		}
	}

	protected void handleDisconnect(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
	}

	protected void handleHeartBeatResp(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
	}

	protected void handleHeartBeatReq(ChannelHandlerContext context, ProxyMessage message) {

	}

	protected void handleResp(ChannelHandlerContext handlerContext, ProxyMessage message) {

	}

	protected abstract void handleData(ChannelHandlerContext context, ProxyMessage message);

	protected void handleReq(ChannelHandlerContext context, ProxyMessage message) {

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		manager.close();
		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	    log.error("error occurred.",cause);
	    ctx.channel().close();
	}
}
