package com.pengjinfei.proxy.server.handler;

import com.pengjinfei.proxy.handler.AbstractProxyMessageHandler;
import com.pengjinfei.proxy.message.*;
import com.pengjinfei.proxy.util.NetUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
public class ProxyServerHandler extends AbstractProxyMessageHandler {

    private DefaultChannelGroup channelGroup;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelGroup = new DefaultChannelGroup(ctx.channel().parent().id().asLongText(),ctx.executor(),true);
        super.channelActive(ctx);
    }

    @Override
    protected void handleResp(ChannelHandlerContext handlerContext, ProxyMessage message) {

    }

    @Override
    protected void handleData(ChannelHandlerContext context, ProxyMessage message) {
        TransferData transferData = (TransferData) message.getBody();
        String reqId = transferData.getReqId();
        Channel facadeClientChannel = ProxyChannelManager.getFacadeClientChannelByReqid(reqId);
        facadeClientChannel.writeAndFlush(transferData.getData());
    }

    @Override
    protected void handleReq(ChannelHandlerContext context, ProxyMessage message) {
        ConnectReq connectReq = (ConnectReq) message.getBody();
        List<Integer> portList = connectReq.getPortList();
        ProxyMessage<ConnectResp> respProxyMessage = new ProxyMessage<ConnectResp>();
        ConnectResp resp = new ConnectResp();
        respProxyMessage.setMessageType(MessageType.CONNECT_RESP);
        respProxyMessage.setBody(resp);
        if (CollectionUtils.isEmpty(portList)) {
            resp.setRespType(RespType.PORT_NOT_AVALIABLE);
            context.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
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
            context.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        final Channel channel = context.channel();
        for (Integer succPort : succPorts) {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(channel.eventLoop())
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childHandler(new FacadeServerHandler(succPort, channel,channelGroup));
            bootstrap.bind(succPort);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelGroup.close();
        super.channelInactive(ctx);
    }
}
