package org.blogapp.dg_blogapp.utils;

import org.blogapp.dg_blogapp.exception.EncryptionException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AesGcmUtil {

    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;  // 128 bits = 16 bytes
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    //Encrypt
    public static String encrypt(String plainText, SecretKey key) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Plain text cannot be null or empty");
        }
        if (key == null) {
            throw new IllegalArgumentException("Encryption key cannot be null");
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);   //random IV every time

            //Initialize Cipher in AES-GCM mode
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            byte[] cipherText = cipher.doFinal(plainText.getBytes());

            //Combine IV + cipher text
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH_BYTES);  // Fixed: copy iv to combined
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    //DECRYPT
    public static String decrypt(String encryptedData, SecretKey key) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new IllegalArgumentException("Encrypted data cannot be null or empty");
        }
        if (key == null) {
            throw new IllegalArgumentException("Decryption key cannot be null");
        }

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            if (combined.length < IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid encrypted data format");
            }

            // extract IV from the front
            byte[] iv = new byte[IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            // extract ciphertext
            byte[] cipherText = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            return new String(cipher.doFinal(cipherText));
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation exceptions as-is
        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }

}
