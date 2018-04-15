package com.pengjinfei.proxy.constants;

import io.netty.util.AttributeKey;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
public interface NettyConstant {

    int FIELD_LENGTH = 4;

    int MAX_FRAME_LENGTH = 10 * 1024 * 1024;

    int SERVER_READ_TIMEOUT = 23;

    int PROXY_READ_TIMEOUT = 25;

    int PROXY_WRITE_TIMEOUT = 11;

    AttributeKey<Boolean> REMOTE_DISCONNECT = AttributeKey.newInstance("remote_disconnect");
}

