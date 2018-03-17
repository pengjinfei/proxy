package com.pengjinfei.proxy.message;

import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

public class ProxyMessageTest {

    @Test
    public void getBody() {
        ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<Integer, String>(16);
        String s1 = map.putIfAbsent(123, "123");
        System.out.println(s1);
        String s = map.putIfAbsent(123, "234");
        System.out.println(s);
        System.out.println(map.get(123));
    }
}