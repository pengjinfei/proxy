package com.pengjinfei.proxy.client.handler;

import com.pengjinfei.proxy.util.MessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author PENGJINFEI533
 * @since 2018-03-20
 */
@RequiredArgsConstructor
@ChannelHandler.Sharable
@Slf4j
public class RealServerHander extends SimpleChannelInboundHandler<ByteBuf> {

	private final int port;

	private final String reqId;

	private final Channel proxyChannel;

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		MessageUtils.writeData(proxyChannel,reqId,port,msg);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        MessageUtils.writeDisconnect(proxyChannel, reqId, port);
		super.channelInactive(ctx);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		MessageUtils.writeWriteFlag(proxyChannel, reqId, port, ctx);
		super.channelWritabilityChanged(ctx);
	}
}
