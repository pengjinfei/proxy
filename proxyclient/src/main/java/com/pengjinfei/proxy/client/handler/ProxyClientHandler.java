package com.pengjinfei.proxy.client.handler;

import com.pengjinfei.proxy.client.configuration.PortMapping;
import com.pengjinfei.proxy.handler.AbstractProxyMessageHandler;
import com.pengjinfei.proxy.message.ConnectReq;
import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.message.TransferData;
import com.pengjinfei.proxy.util.MessageUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created on 3/18/18
 *
 * @author Pengjinfei
 */
@Slf4j
public class ProxyClientHandler extends AbstractProxyMessageHandler {

	private final Map<Integer, SocketAddress> portMap = new HashMap<>();

	public ProxyClientHandler(List<PortMapping> mapping) {
		for (PortMapping portMapping : mapping) {
			portMap.put(portMapping.getFacadePort(), new InetSocketAddress(portMapping.getRealIp(), portMapping.getRealPort()));
		}
	}

	@Override
	protected void handleHeartBeatResp(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
		log.debug("get response from server");
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.WRITER_IDLE) {
				ProxyMessage<Boolean> req = new ProxyMessage<>();
				req.setMessageType(MessageType.HEART_BEAT_REQ);
				req.setBody(true);
				ctx.channel().writeAndFlush(req);
			} else if (event.state() == IdleState.READER_IDLE) {
				log.warn("can not recieve heart beat from server,prepare to close channel");
				ctx.channel().close();
			}
		}
	}

	@Override
	protected void handleResp(ChannelHandlerContext handlerContext, ProxyMessage message) {
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ProxyMessage<ConnectReq> msg = new ProxyMessage<>();
		msg.setMessageType(MessageType.PROXY_REQ);
		ConnectReq connectReq = new ConnectReq();
		List<Integer> portList = new LinkedList<>();
		for (Map.Entry<Integer, SocketAddress> entry : portMap.entrySet()) {
			portList.add(entry.getKey());
		}
		connectReq.setPortList(portList);
		msg.setBody(connectReq);
		ctx.channel().writeAndFlush(msg);
	}

	@Override
	protected void handleConnect(ChannelHandlerContext context, ProxyMessage proxyMessage) {
		TransferData data = (TransferData) proxyMessage.getBody();
		String reqId = data.getReqId();
		int port = data.getPort();
		Channel proxyChannel = context.channel();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(proxyChannel.eventLoop())
				.channel(EpollSocketChannel.class)
				.handler(new RealServerHander(port, reqId, proxyChannel));
		bootstrap.connect(portMap.get(port)).addListener((ChannelFutureListener) future -> {
			if (future.isSuccess()) {
				Channel realChannel = future.channel();
				manager.add(reqId, realChannel);
				manager.setChannelAutoRead(realChannel,true);
				MessageUtils.writeConnect(proxyChannel, reqId, port);
			} else {
				MessageUtils.writeDisconnect(proxyChannel, reqId, port, null);
			}
		});
	}
}
