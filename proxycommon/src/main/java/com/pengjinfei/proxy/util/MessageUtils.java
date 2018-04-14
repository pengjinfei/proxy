package com.pengjinfei.proxy.util;

import com.pengjinfei.proxy.constants.NettyConstant;
import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.message.TransferData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Created on 4/12/18
 *
 * @author Pengjinfei
 */
@UtilityClass
@Slf4j
public class MessageUtils {

    public static void writeDisconnect(Channel proxyChannel, String reqId, int port, ChannelHandlerContext ctx) {
        if (ctx != null) {
            Boolean isRemote = ctx.channel().attr(NettyConstant.REMOTE_DISCONNECT).get();
            if (isRemote != null && isRemote) {
                return;
            }
        }
        log.debug("write disconnect to reqId[{}] port[{}]", reqId, port);
        ProxyMessage<TransferData> msg = new ProxyMessage<>();
        msg.setMessageType(MessageType.DISCONNECT);
        TransferData data = new TransferData();
        data.setPort(port);
        data.setReqId(reqId);
        msg.setBody(data);
        proxyChannel.writeAndFlush(msg);
    }

    public static void writeConnect(Channel proxyChannel, String reqId, int port) {
        log.debug("write connect to reqId[{}] port[{}]", reqId, port);
        ProxyMessage<TransferData> msg = new ProxyMessage<>();
        msg.setMessageType(MessageType.CONNECT);
        TransferData data = new TransferData();
        data.setPort(port);
        data.setReqId(reqId);
        msg.setBody(data);
        proxyChannel.writeAndFlush(msg);
    }

    public static void writeWriteFlag(Channel proxyChannel, String reqId, int port, ChannelHandlerContext ctx) {
        log.debug("write writeFlag[{}] to reqId[{}] port[{}]", ctx.channel().isWritable(), reqId, port);
        ProxyMessage<TransferData> msg = new ProxyMessage<>();
        msg.setMessageType(MessageType.WRITE_FLAG);
        TransferData data = new TransferData();
        data.setPort(port);
        data.setReqId(reqId);
        byte[] body = ctx.channel().isWritable() ? new byte[]{0x01} : new byte[]{0x00};
        data.setData(body);
        msg.setBody(data);
        proxyChannel.writeAndFlush(msg);
    }

    public static void writeData(Channel proxyChannel, String reqId, int port, ByteBuf msg) {
        log.debug("write data to reqId[{}] port[{}]", reqId, port);
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        ProxyMessage<TransferData> message = new ProxyMessage<>();
        TransferData data = new TransferData();
        data.setPort(port);
        data.setData(bytes);
        data.setReqId(reqId);
        message.setBody(data);
        message.setMessageType(MessageType.DATA);
        proxyChannel.writeAndFlush(message);
    }
}
