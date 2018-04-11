package com.pengjinfei.proxy.client.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * Created on 4/10/18
 *
 * @author Pengjinfei
 */
@Data
@RequiredArgsConstructor
public class ExhaustedTimer {

    private final long time;

    private final TimeUnit timeUnit;

    private int num = 0;

    private long lastUpdate = 0;

    public synchronized int incrementAndGet() {
        long timeMillis = System.currentTimeMillis();
        long dis = timeUnit.toMillis(time);
        if (timeMillis - lastUpdate > dis) {
            num = 1;
        } else {
            num++;
        }
        lastUpdate = timeMillis;
        return num;
    }


}
