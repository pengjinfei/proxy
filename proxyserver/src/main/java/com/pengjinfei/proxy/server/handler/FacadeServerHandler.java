package com.pengjinfei.proxy.server.handler;

import com.pengjinfei.proxy.channel.ChannelManager;
import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.message.TransferData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@RequiredArgsConstructor
@Slf4j
@ChannelHandler.Sharable
public class FacadeServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

	private final Integer port;
	private final Channel proxyChannel;
	private final ChannelManager manager;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		boolean add = manager.add(ctx.channel());
		if (add) {
			super.channelActive(ctx);
		} else {
			log.error("Never should happen, two channel have same id");
			ctx.channel().close();
		}
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		byte[] bytes = new byte[msg.readableBytes()];
		msg.readBytes(bytes);
		ProxyMessage<TransferData> message = new ProxyMessage<TransferData>();
		TransferData data = new TransferData();
		data.setPort(port);
		data.setData(bytes);
		data.setReqId(ctx.channel().id().asLongText());
		message.setBody(data);
		message.setMessageType(MessageType.DATA);
		proxyChannel.writeAndFlush(message);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("error occurred", cause);
		ctx.channel().close();
	}
}
