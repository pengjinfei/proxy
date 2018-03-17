package com.pengjinfei.proxy.server.handler;

import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.message.TransferData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@RequiredArgsConstructor
public class FacadeServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final Integer port;
    private  final Channel proxyChannel;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf buf) throws Exception {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        ProxyMessage<TransferData> message = new ProxyMessage<TransferData>();
        TransferData data = new TransferData();
        data.setPort(port);
        data.setData(bytes);
        message.setBody(data);
        message.setMessageType(MessageType.DATA);
        proxyChannel.writeAndFlush(message);
    }
}
