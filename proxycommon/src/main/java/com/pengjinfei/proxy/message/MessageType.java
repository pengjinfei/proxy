package com.pengjinfei.proxy.message;

import java.io.Serializable;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
public enum  MessageType implements Serializable{
    /*
    客户端连接请求
     */
    CONNECT_REQ,
    CONNECT_RESP,
    DATA,
    HEART_BEAT_REQ,
    HEART_BEAT_RESP;
}
