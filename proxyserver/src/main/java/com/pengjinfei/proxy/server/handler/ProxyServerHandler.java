package com.pengjinfei.proxy.server.handler;

import com.pengjinfei.proxy.message.*;
import com.pengjinfei.proxy.util.NetUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
public class ProxyServerHandler extends SimpleChannelInboundHandler<ProxyMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage) throws Exception {
        MessageType messageType = proxyMessage.getMessageType();
        switch (messageType) {
            case CONNECT_REQ:
                handleReq(channelHandlerContext, proxyMessage);
                break;
            default:
                break;
        }
    }

    private void handleReq(ChannelHandlerContext context, ProxyMessage message) {
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
            if (integer == null || NetUtils.isPortInuse(integer) || !ProxyChannelManager.isPortAvailable(integer)) {
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
        for (Integer succPort : succPorts) {

        }
    }
}
