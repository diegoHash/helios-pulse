package com.helios.platform.pulse.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
@Component
public class AesEncryptorConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";
    private static byte[] KEY;

    @Value("${app.encryption.key}")
    public void setKey(String key) {
        if (key == null || key.length() != 32) {
            throw new IllegalArgumentException("app.encryption.key must contain exactly 32 characters");
        }
        AesEncryptorConverter.KEY = key.getBytes();
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(KEY, ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting attribute", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(KEY, ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (IllegalArgumentException e) {
            // Not Base64
            return dbData;
        } catch (Exception e) {
            // Decryption failed (e.g. wrong key, not encrypted)
            return dbData;
        }
    }
}
