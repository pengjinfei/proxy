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
public class ConnectResp implements Serializable {

    private RespType respType;
    private List<Integer> failPortList;

}
