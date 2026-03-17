package org.blogapp.dg_blogapp.payment.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.exception.EncryptionException;
import org.blogapp.dg_blogapp.utils.AesGcmUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
@Component
@Slf4j
public class AccountNumberConverter implements AttributeConverter<String, String> {

    @Value("${encryption.secret-key}")
    private String encryptionKeyString;

    private SecretKey getEncryptionKey() {
        byte[] decodedKey = Base64.getDecoder().decode(encryptionKeyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        try {
            return AesGcmUtil.encrypt(attribute, getEncryptionKey());
        } catch (Exception e) {
            log.error("Failed to encrypt: {}", e.getMessage());
            throw new EncryptionException("Failed to encrypt sensitive data", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        try {
            return AesGcmUtil.decrypt(dbData, getEncryptionKey());
        } catch (Exception e) {
            log.error("Failed to decrypt: {}", e.getMessage());
            throw new EncryptionException("Failed to decrypt sensitive data", e);
        }
    }
}
