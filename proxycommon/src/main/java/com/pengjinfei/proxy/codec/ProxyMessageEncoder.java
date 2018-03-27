package com.pengjinfei.proxy.codec;

import com.pengjinfei.proxy.constants.NettyConstant;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.util.FstSerializerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@Slf4j
public class ProxyMessageEncoder extends MessageToByteEncoder<ProxyMessage>{

    private final byte[] passwd;

    private static final byte[] LENGTH_PLACEHOLDER = new byte[NettyConstant.FIELD_LENGTH];

    public ProxyMessageEncoder(byte[] passwd) {
        this.passwd = passwd;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ProxyMessage proxyMessage, ByteBuf byteBuf) throws Exception {
        if (proxyMessage == null ) {
            throw new Exception("The encode message is null");
        }
        try {
            int writerIndex = byteBuf.writerIndex();
            byteBuf.writeBytes(LENGTH_PLACEHOLDER);
            byte[] bytes =FstSerializerUtils.serialize(proxyMessage);
            byteBuf.writeBytes(bytes);
            byteBuf.setInt(writerIndex, byteBuf.writerIndex() - writerIndex - NettyConstant.FIELD_LENGTH);
        } catch (Exception e) {
            log.error("encode error",e);
            channelHandlerContext.channel().close();
        }
    }
}
