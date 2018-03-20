package com.pengjinfei.proxy.client.handler;

import com.pengjinfei.proxy.channel.ChannelManager;
import com.pengjinfei.proxy.handler.AbstractProxyMessageHandler;
import com.pengjinfei.proxy.message.ConnectReq;
import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.message.TransferData;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created on 3/18/18
 *
 * @author Pengjinfei
 */
@RequiredArgsConstructor
public class ProxyClientHandler extends AbstractProxyMessageHandler{

	private ChannelManager manager=new ChannelManager();

	private final Map<Integer,SocketAddress> portMapping;


    @Override
    protected void handleResp(ChannelHandlerContext handlerContext, ProxyMessage message) {

    }

    @Override
    protected void handleData(ChannelHandlerContext context, ProxyMessage message) {
        TransferData data = (TransferData) message.getBody();
        String reqId = data.getReqId();
        Channel channel = manager.find(reqId);
        if (channel == null) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(context.channel().eventLoop())
                    .channel(NioSocketChannel.class)
                    .handler(new RealServerHander(data.getPort(), reqId, context.channel()));
            bootstrap.connect(portMapping.get(data.getPort())).addListener((ChannelFutureListener) future -> {
				Channel realChannel = future.channel();
				manager.add(reqId, realChannel);
				realChannel.writeAndFlush(data.getData());
			});
        } else {
            channel.writeAndFlush(data.getData());
        }
    }

    @Override
    protected void handleReq(ChannelHandlerContext context, ProxyMessage message) {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ProxyMessage<ConnectReq> msg = new ProxyMessage<>();
        msg.setMessageType(MessageType.CONNECT_REQ);
        ConnectReq connectReq = new ConnectReq();
        List<Integer> portList = new LinkedList<>();
        for (Map.Entry<Integer, SocketAddress> entry : portMapping.entrySet()) {
            portList.add(entry.getKey());
        }
        connectReq.setPortList(portList);
        msg.setBody(connectReq);
        ctx.channel().writeAndFlush(msg);
    }
}
