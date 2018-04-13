package com.pengjinfei.proxy.server.handler;

import com.pengjinfei.proxy.channel.ChannelManager;
import com.pengjinfei.proxy.util.MessageUtils;
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
		log.debug("new connections id:{}",ctx.channel().id().asLongText());
		boolean add = manager.add(ctx.channel());
		if (add) {
		    manager.setChannelAutoRead(ctx.channel(),false);
		    MessageUtils.writeConnect(proxyChannel,ctx.channel().id().asLongText(),port);
			super.channelActive(ctx);
		} else {
			log.error("Never should happen, two channel have same id");
			ctx.channel().close();
		}
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		log.debug("new request id:{}",ctx.channel().id().asLongText());
		MessageUtils.writeData(proxyChannel, ctx.channel().id().asLongText(), port, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("error occurred", cause);
		ctx.channel().close();
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		MessageUtils.writeWriteFlag(proxyChannel,ctx.channel().id().asLongText(),port,ctx);
		super.channelWritabilityChanged(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		MessageUtils.writeDisconnect(proxyChannel, ctx.channel().id().asLongText(), port, ctx);
		super.channelInactive(ctx);
	}
}
