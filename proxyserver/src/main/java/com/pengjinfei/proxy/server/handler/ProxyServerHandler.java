package com.pengjinfei.proxy.server.handler;

import com.pengjinfei.proxy.handler.AbstractProxyMessageHandler;
import com.pengjinfei.proxy.message.*;
import com.pengjinfei.proxy.util.NetUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.net.InetSocketAddress;
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
	protected void handleData(ChannelHandlerContext context, ProxyMessage message) {
		TransferData transferData = (TransferData) message.getBody();
		String reqId = transferData.getReqId();
		log.debug("get response id:{}",reqId);
		Channel facadeChannel = manager.find(reqId);
		if (facadeChannel == null) {
			//// TODO: 2018-03-20 应该返回消息，关闭失效的链路
			log.warn(String.format("Can't find channel of id:%s, it's probably closed.", reqId));
		} else {
			byte[] bytes = transferData.getData();
			ByteBuf buf = context.alloc().buffer(bytes.length);
			buf.writeBytes(bytes);
			facadeChannel.writeAndFlush(buf).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.debug("write response finished to id:{}", future.channel().id().asLongText());
                }
            });
		}
	}

	@Override
	protected void handleReq(ChannelHandlerContext context, ProxyMessage message) {
        InetSocketAddress socketAddress = ((InetSocketAddress) context.channel().remoteAddress());
        log.info("recieve connection fomr ip:{} port:{}", socketAddress.getHostName(), socketAddress.getPort());
		ConnectReq connectReq = (ConnectReq) message.getBody();
		List<Integer> portList = connectReq.getPortList();
		ProxyMessage<ConnectResp> respProxyMessage = new ProxyMessage<>();
		ConnectResp resp = new ConnectResp();
		respProxyMessage.setMessageType(MessageType.PROXY_RESP);
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
			bootstrap.group(channel.eventLoop().parent())
					.channel(EpollServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.childHandler(new FacadeServerHandler(succPort, channel, manager));
			bootstrap.bind(succPort).addListener((ChannelFutureListener) future -> manager.add(future.channel()));
		}
		resp.setRespType(RespType.SUCCESS);
		channel.writeAndFlush(resp);
	}

	@Override
	protected void handleHeartBeatReq(ChannelHandlerContext context, ProxyMessage message) {
		ProxyMessage<Boolean> resp = new ProxyMessage<>();
		resp.setMessageType(MessageType.HEART_BEAT_RESP);
		resp.setBody(true);
		context.channel().writeAndFlush(resp);
	}

	@Override
	protected void handleDisconnect(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
		TransferData transferData = (TransferData) proxyMessage.getBody();
		String reqId = transferData.getReqId();
		log.debug("disconnect id:{}",reqId);
		Channel facadeChannel = manager.find(reqId);
		if (facadeChannel != null) {
			facadeChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("error occured.",cause);
		ctx.channel().close();
	}
}
