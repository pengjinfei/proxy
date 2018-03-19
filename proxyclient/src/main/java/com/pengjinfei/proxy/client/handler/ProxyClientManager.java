package com.pengjinfei.proxy.client.handler;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 3/18/18
 *
 * @author Pengjinfei
 */
@UtilityClass
public class ProxyClientManager {

    private static final ConcurrentHashMap<Integer, Integer> portMapping = new ConcurrentHashMap<Integer, Integer>();

    public Integer getMappingPort(Integer serverPort) {
        return portMapping.get(serverPort);
    }
}
