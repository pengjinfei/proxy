package com.pengjinfei.proxy.constants;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.List;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
public interface NettyConstant {

    int FIELD_LENGTH = 4;

    AttributeKey<List<Channel>> PROXY_FACADE = AttributeKey.valueOf("proxy_facade");

    AttributeKey<String> FACADE_REQID = AttributeKey.valueOf("facade_reqid");
}

