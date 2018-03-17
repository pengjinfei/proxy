package com.pengjinfei.proxy.codec;

import com.pengjinfei.proxy.util.AesUtils;
import com.pengjinfei.proxy.util.FstSerializerUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@Slf4j
public class ProxyMessageDecoder extends LengthFieldBasedFrameDecoder {

    private final byte[] passwd;

    public ProxyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,byte[] passwd) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
        this.passwd = passwd;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        int size = in.readInt();
        byte[] bytes = new byte[size];
        in.readBytes(bytes, 0, size);
        return FstSerializerUtils.deserialize(AesUtils.decrypt(bytes,passwd));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("error occurred.", cause);
    }
}
