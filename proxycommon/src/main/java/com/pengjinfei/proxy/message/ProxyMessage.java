package com.pengjinfei.proxy.message;

import lombok.Data;

import java.io.Serializable;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@Data
public class ProxyMessage<T extends Serializable> implements Serializable {

    private MessageType messageType;
    private T body;
}
