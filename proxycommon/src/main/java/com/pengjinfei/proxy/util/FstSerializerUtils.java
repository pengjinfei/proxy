package com.pengjinfei.proxy.util;

import lombok.experimental.UtilityClass;
import org.nustaq.serialization.FSTConfiguration;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@UtilityClass
public class FstSerializerUtils {

    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    public static byte[] serialize(Object o) {
        if (o == null) {
            return null;
        }
        return conf.asByteArray(o);
    }

    public static Object deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return conf.asObject(bytes);
    }
}
