package com.pengjinfei.proxy.util;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

public class AesUtilsTest {

    private byte[] passwd = RandomUtils.nextBytes(16);

    @Test
    public void encryptAndDecrypt() {
        String origin = "pjf";
        byte[] encrypt = AesUtils.encrypt(origin.getBytes(), passwd);
        System.out.println(encrypt.length);
        byte[] decrypt = AesUtils.decrypt(encrypt, passwd);
        Assert.assertEquals(origin,new String(decrypt));
    }
}
