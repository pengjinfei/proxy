package com.pengjinfei.proxy.util;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.security.InvalidKeyException;

public class AesUtilsTest {

    private byte[] passwd = RandomUtils.nextBytes(16);

    @Test
    public void encryptAndDecrypt() throws InvalidKeyException {
        String origin = "pjf";
        byte[] encrypt = AesUtils.encrypt(origin.getBytes(), passwd);
        System.out.println(encrypt.length);
        byte[] decrypt = AesUtils.decrypt(encrypt, passwd);
        Assert.assertEquals(origin,new String(decrypt));
    }
}
