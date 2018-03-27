package com.pengjinfei.proxy.client.handler;

import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.message.TransferData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

/**
 * @author PENGJINFEI533
 * @since 2018-03-20
 */
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class RealServerHander extends SimpleChannelInboundHandler<ByteBuf> {

	private final int port;

	private final String reqId;

	private final Channel proxyChannel;

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		ProxyMessage<TransferData> message = new ProxyMessage<>();
		TransferData data = new TransferData();
		data.setPort(port);
		byte[] bytes = new byte[msg.readableBytes()];
		msg.readBytes(bytes);
		data.setReqId(reqId);
		data.setData(bytes);
		message.setMessageType(MessageType.DATA);
		message.setBody(data);
		proxyChannel.writeAndFlush(message);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ProxyMessage<TransferData> msg = new ProxyMessage<>();
		msg.setMessageType(MessageType.DISCONNECT);
		TransferData data = new TransferData();
		data.setPort(port);
		data.setReqId(reqId);
		msg.setBody(data);
		proxyChannel.writeAndFlush(msg);
	}
}
