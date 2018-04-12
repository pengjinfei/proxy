package com.pengjinfei.proxy.util;

import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.message.TransferData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.experimental.UtilityClass;

/**
 * Created on 4/12/18
 *
 * @author Pengjinfei
 */
@UtilityClass
public class MessageUtils {

    public static void writeDisconnect(Channel proxyChannel, String reqId, int port) {
        ProxyMessage<TransferData> msg = new ProxyMessage<>();
        msg.setMessageType(MessageType.DISCONNECT);
        TransferData data = new TransferData();
        data.setPort(port);
        data.setReqId(reqId);
        msg.setBody(data);
        proxyChannel.writeAndFlush(msg);
    }

    public static void writeConnect(Channel proxyChannel, String reqId, int port) {
        ProxyMessage<TransferData> msg = new ProxyMessage<>();
        msg.setMessageType(MessageType.CONNECT);
        TransferData data = new TransferData();
        data.setPort(port);
        data.setReqId(reqId);
        msg.setBody(data);
        proxyChannel.writeAndFlush(msg);
    }

    public static void writeWriteFlag(Channel proxyChannel, String reqId, int port, ChannelHandlerContext ctx) {
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
