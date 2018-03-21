package com.pengjinfei.proxy.codec;

import com.pengjinfei.proxy.constants.NettyConstant;
import com.pengjinfei.proxy.message.ProxyMessage;
import com.pengjinfei.proxy.util.AesUtils;
import com.pengjinfei.proxy.util.FstSerializerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
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
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writeBytes(LENGTH_PLACEHOLDER);
        byte[] bytes = AesUtils.encrypt(FstSerializerUtils.serialize(proxyMessage),passwd);
        byteBuf.writeBytes(bytes);
        byteBuf.setInt(writerIndex, byteBuf.writerIndex() - writerIndex - NettyConstant.FIELD_LENGTH);
    }
}
