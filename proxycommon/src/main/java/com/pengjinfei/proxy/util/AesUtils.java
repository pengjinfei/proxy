package com.pengjinfei.proxy.util;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@UtilityClass
@Slf4j
public class AesUtils {

    private static final String KEY_ALGORITHM = "AES";
    /**
     * 默认的加密算法
     */
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    public static byte[] encrypt(byte[] content, byte[] password) throws InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password));
            return cipher.doFinal(content);
        } catch (InvalidKeyException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt(byte[] content, byte[] password) throws InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password));
            return cipher.doFinal(content);
        } catch (InvalidKeyException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private static SecretKey getSecretKey(@NonNull byte[] bytes) {
        Assert.state(bytes.length==16,"SecretKey must be 16 length.");
        return new SecretKeySpec(bytes,KEY_ALGORITHM);
    }
}
