package org.vced.ragutillib.authentication;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
@Slf4j
public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final byte[] SECRET_KEY = "g8#F@Lz7!vN2qP1dW4rT9eXb0sYmUcKh".getBytes(StandardCharsets.UTF_8); // 32 байта = AES-256
    private static final byte[] SECRET_PHRASE = "&Mj5£%v7<H0>Z$B&YR,1E4P9qe2u3{^3".getBytes(StandardCharsets.UTF_8);

    public static String encrypt(String data) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Используем URL-safe Base64 без padding
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Selected wrong Crypto configuration: {}. Fix Crypto configuration and then restart project!", e.getMessage());
            throw new IllegalStateException("Crypto failed to initialize. Shutting down.", e);
        }
    }
    public static String getEncryptedSecretPhrase() {
        return encrypt(Arrays.toString(SECRET_PHRASE));
    }
    public static String decrypt(String encryptedData) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encryptedData); // URL-safe decode
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Selected wrong Crypto configuration: {}. Fix Crypto configuration and then restart project!",
                    e.getMessage());
            throw new IllegalStateException("Crypto failed to initialize. Shutting down.", e);
        }
    }
    public static boolean isSecretPhraseValid(String encryptedData) {
        return decrypt(encryptedData).equals(Arrays.toString(SECRET_PHRASE));
    }
}
