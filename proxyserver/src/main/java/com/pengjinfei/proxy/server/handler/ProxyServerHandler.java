package com.pengjinfei.proxy.server.handler;

import com.pengjinfei.proxy.handler.AbstractProxyMessageHandler;
import com.pengjinfei.proxy.message.*;
import com.pengjinfei.proxy.util.NetUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@Slf4j
public class ProxyServerHandler extends AbstractProxyMessageHandler {

	@Override
	protected void handleResp(ChannelHandlerContext handlerContext, ProxyMessage message) {

	}

	@Override
	protected void handleData(ChannelHandlerContext context, ProxyMessage message) {
		TransferData transferData = (TransferData) message.getBody();
		String reqId = transferData.getReqId();
		Channel facadeChannel = manager.find(reqId);
		if (facadeChannel == null) {
			//// TODO: 2018-03-20 应该返回消息，关闭失效的链路
			log.warn(String.format("Can't find channel of id:%s, it's probably closed.", reqId));
		} else {
			facadeChannel.writeAndFlush(transferData.getData());
		}
	}

	@Override
	protected void handleReq(ChannelHandlerContext context, ProxyMessage message) {
		ConnectReq connectReq = (ConnectReq) message.getBody();
		List<Integer> portList = connectReq.getPortList();
		ProxyMessage<ConnectResp> respProxyMessage = new ProxyMessage<>();
		ConnectResp resp = new ConnectResp();
		respProxyMessage.setMessageType(MessageType.CONNECT_RESP);
		respProxyMessage.setBody(resp);
		final Channel channel = context.channel();
		if (CollectionUtils.isEmpty(portList)) {
			resp.setRespType(RespType.PORT_NOT_AVALIABLE);
			channel.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
			return;
		}
		List<Integer> failPorts = new ArrayList<Integer>();
		List<Integer> succPorts = new ArrayList<Integer>();
		for (Integer integer : portList) {
			if (integer == null || NetUtils.isPortInuse(integer)) {
				failPorts.add(integer);
			} else {
				succPorts.add(integer);
			}
		}
		if (failPorts.size() > 0 || succPorts.size() == 0) {
			resp.setRespType(RespType.PORT_NOT_AVALIABLE);
			resp.setFailPortList(failPorts);
			channel.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
			return;
		}
		for (Integer succPort : succPorts) {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(channel.eventLoop())
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.childHandler(new FacadeServerHandler(succPort, channel, manager));
			bootstrap.bind(succPort);
		}
		resp.setRespType(RespType.SUCCESS);
		channel.writeAndFlush(resp);
	}

}
