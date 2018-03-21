package com.pengjinfei.proxy.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@Data
public class ConnectReq implements Serializable {

    private List<Integer> portList;
}
