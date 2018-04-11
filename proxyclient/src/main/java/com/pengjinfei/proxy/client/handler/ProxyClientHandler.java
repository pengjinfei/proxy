package com.pengjinfei.proxy.client.handler;

import com.pengjinfei.proxy.channel.ChannelManager;
import com.pengjinfei.proxy.client.configuration.PortMapping;
import com.pengjinfei.proxy.handler.AbstractProxyMessageHandler;
import com.pengjinfei.proxy.message.ConnectReq;
import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.message.TransferData;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
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

	private ChannelManager manager = new ChannelManager();

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
			}
		}
	}

	@Override
	protected void handleResp(ChannelHandlerContext handlerContext, ProxyMessage message) {
	}

	@Override
	protected void handleData(ChannelHandlerContext context, ProxyMessage message) {
		TransferData data = (TransferData) message.getBody();
		String reqId = data.getReqId();
        int port = data.getPort();
        Channel channel = manager.find(reqId);
        byte[] bytes = data.getData();
        ByteBuf buf = context.alloc().buffer(bytes.length);
        buf.writeBytes(bytes);
        Channel proxyChannel = context.channel();
        if (channel == null ){
            newChannelAndWrite(reqId, port, buf, proxyChannel);
		} else if (!channel.isWritable()) {
        	log.info("realChannel:{} is not writable",channel.id().asLongText());
		} else {
			channel.writeAndFlush(buf).addListener(future -> {
				if (!future.isSuccess()) {
					log.warn("channel exists but write failed.");
					newChannelAndWrite(reqId, port, buf, proxyChannel);
				}
			});
		}
	}

    private void newChannelAndWrite(String reqId, int port, ByteBuf buf, Channel proxyChannel) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(proxyChannel.eventLoop())
                .channel(EpollSocketChannel.class)
                .handler(new RealServerHander(port, reqId, proxyChannel));
        bootstrap.connect(portMap.get(port)).addListener((ChannelFutureListener) future -> {
            //// TODO: 3/22/18  如果服务器连接失败 关闭远程端口
            Channel realChannel = future.channel();
            manager.add(reqId, realChannel);
            realChannel.writeAndFlush(buf);
        });
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
}
