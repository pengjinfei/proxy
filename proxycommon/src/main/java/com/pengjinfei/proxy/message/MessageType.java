package com.pengjinfei.proxy.message;

import java.io.Serializable;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
public enum  MessageType implements Serializable{
    CONNECT_REQ,
    CONNECT_RESP,
    DATA;
}
