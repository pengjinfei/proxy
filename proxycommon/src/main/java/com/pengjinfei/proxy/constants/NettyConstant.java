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

    int READ_TIMEOUT = 50;

    AttributeKey<Boolean> REMOTE_DISCONNECT = AttributeKey.newInstance("remote_disconnect");
}

