package com.pengjinfei.proxy.handler;

import com.pengjinfei.proxy.channel.ChannelManager;
import com.pengjinfei.proxy.constants.NettyConstant;
import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.message.TransferData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
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

	protected ChannelManager manager = ChannelManager.getInstance();

	@Override
	protected void messageReceived(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) throws Exception {
		MessageType messageType = proxyMessage.getMessageType();
		switch (messageType) {
			case PROXY_REQ:
				log.debug("[MESSAGE] - PROXY_REQ");
				handleReq(channelHandlerContext, proxyMessage);
				break;
			case DATA:
				log.debug("[MESSAGE] - DATA");
				handleData(channelHandlerContext, proxyMessage);
				break;
			case PROXY_RESP:
				log.debug("[MESSAGE] - PROXY_RESP");
				handleResp(channelHandlerContext, proxyMessage);
			case HEART_BEAT_REQ:
				log.debug("[MESSAGE] - HEART_BEAT_REQ");
				handleHeartBeatReq(channelHandlerContext, proxyMessage);
				break;
			case HEART_BEAT_RESP:
				log.debug("[MESSAGE] - HEART_BEAT_RESP");
				handleHeartBeatResp(channelHandlerContext, proxyMessage);
				break;
			case DISCONNECT:
				log.debug("[MESSAGE] - DISCONNECT");
				handleDisconnect(channelHandlerContext, proxyMessage);
				break;
			case WRITE_FLAG:
				log.debug("[MESSAGE] - WRITE_FLAG");
				handleWriteFlag(channelHandlerContext, proxyMessage);
				break;
			case CONNECT:
				log.debug("[MESSAGE] - CONNECT");
				handleConnect(channelHandlerContext, proxyMessage);
				break;
			default:
				break;
		}
	}

	protected void handleConnect(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
	}

	private void handleWriteFlag(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
		TransferData transferData = (TransferData) proxyMessage.getBody();
		boolean autoRead = transferData.getData()[0] == 0x01;
		String reqId = transferData.getReqId();
		Channel channel = manager.find(reqId);
		if (channel != null) {
			channel.config().setAutoRead(autoRead);
		}
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		log.debug("proxy channel writablity changed to {}", ctx.channel().isWritable());
	    manager.setProxyWritable(ctx.channel().isWritable());
		super.channelWritabilityChanged(ctx);
	}

	protected void handleDisconnect(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
		TransferData transferData = (TransferData) proxyMessage.getBody();
		String reqId = transferData.getReqId();
		Channel channel = manager.remove(reqId);
		if (channel != null) {
			channel.attr(NettyConstant.REMOTE_DISCONNECT).set(true);
			channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}

	protected void handleHeartBeatResp(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
	}

	protected void handleHeartBeatReq(ChannelHandlerContext context, ProxyMessage message) {

	}

	protected void handleResp(ChannelHandlerContext handlerContext, ProxyMessage message) {

	}

	protected  void handleData(ChannelHandlerContext context, ProxyMessage message){
		TransferData transferData = (TransferData) message.getBody();
		String reqId = transferData.getReqId();
		Channel facadeChannel = manager.find(reqId);
		if (facadeChannel != null) {
			byte[] bytes = transferData.getData();
			ByteBuf buf = context.alloc().buffer(bytes.length);
			buf.writeBytes(bytes);
			facadeChannel.writeAndFlush(buf);
		}
	}

	protected void handleReq(ChannelHandlerContext context, ProxyMessage message) {

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debug("proxy channel not active");
		manager.close();
		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	    log.error("error occurred.",cause);
	    ctx.channel().close();
	}
}
