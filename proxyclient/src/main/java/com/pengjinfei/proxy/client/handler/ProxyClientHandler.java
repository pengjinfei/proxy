package com.pengjinfei.proxy.client.handler;

import com.pengjinfei.proxy.handler.AbstractProxyMessageHandler;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.message.TransferData;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 3/18/18
 *
 * @author Pengjinfei
 */
public class ProxyClientHandler extends AbstractProxyMessageHandler{

    private static final ConcurrentHashMap<Integer, Integer> portMapping = new ConcurrentHashMap<Integer, Integer>();

    @Override
    protected void handleResp(ChannelHandlerContext handlerContext, ProxyMessage message) {

    }

    @Override
    protected void handleData(ChannelHandlerContext context, ProxyMessage message) {
        TransferData data = (TransferData) message.getBody();
        String reqId = data.getReqId();
    }

    @Override
    protected void handleReq(ChannelHandlerContext context, ProxyMessage message) {

    }
}
