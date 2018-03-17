package com.pengjinfei.proxy.util;

import lombok.experimental.UtilityClass;

import java.net.Socket;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@UtilityClass
public class NetUtils {

    public static boolean isPortInuse(int port) {
        try {
            new Socket("127.0.0.1", port);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }
}
