package com.pengjinfei.proxy.message;

import lombok.Data;

import java.io.Serializable;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@Data
public class TransferData implements Serializable{
    private int port;
    private byte[] data;
    private String reqId;
}
