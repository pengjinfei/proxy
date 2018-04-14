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
	protected void handleReq(ChannelHandlerContext context, ProxyMessage message) {
        InetSocketAddress socketAddress = ((InetSocketAddress) context.channel().remoteAddress());
        log.debug("recieve connection from ip:{} port:{}", socketAddress.getHostName(), socketAddress.getPort());
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
			log.warn("port {} not avaliable,close channel.",failPorts);
			channel.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
			return;
		}
		for (Integer succPort : succPorts) {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(channel.eventLoop().parent())
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.childHandler(new FacadeServerHandler(succPort, channel, manager));
			bootstrap.bind(succPort).addListener((ChannelFutureListener) future -> manager.add(future.channel()));
		}
		log.debug("request ports {} passed",succPorts);
		resp.setRespType(RespType.SUCCESS);
		channel.writeAndFlush(resp);
	}

    @Override
    protected void handleConnect(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) {
        TransferData data = (TransferData) proxyMessage.getBody();
        String reqId = data.getReqId();
        manager.setChannelAutoRead(reqId, true);
    }

    @Override
	protected void handleHeartBeatReq(ChannelHandlerContext context, ProxyMessage message) {
		ProxyMessage<Boolean> resp = new ProxyMessage<>();
		resp.setMessageType(MessageType.HEART_BEAT_RESP);
		resp.setBody(true);
		context.channel().writeAndFlush(resp);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("error occured.",cause);
		ctx.channel().close();
	}
}
