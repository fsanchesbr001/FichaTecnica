package com.fabriciosanches.fichatecnica.infrastructure.adapters.out.mail;

import com.fabriciosanches.fichatecnica.core.domain.MensagemEmail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EmailPayloadCipher {

    private final ObjectMapper objectMapper;
    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public EmailPayloadCipher(ObjectMapper objectMapper,
                              @Value("${ficha-tecnica.mail.outbox.encryption-key}") String encryptionKey) {
        this.objectMapper = objectMapper;
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(encryptionKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("OUTBOX_ENCRYPTION_KEY deve ser Base64 de 32 bytes");
        }
        if (decoded.length != 32) {
            throw new IllegalArgumentException("OUTBOX_ENCRYPTION_KEY deve ser Base64 de 32 bytes");
        }
        key = new SecretKeySpec(decoded, "AES");
    }

    public String encrypt(MensagemEmail mensagem) {
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(objectMapper.writeValueAsBytes(mensagem));
            return Base64.getEncoder().encodeToString(
                    ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array());
        } catch (GeneralSecurityException | JsonProcessingException e) {
            throw new IllegalStateException("Falha ao proteger payload de email", e);
        }
    }

    public MensagemEmail decrypt(String payload) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(payload);
            if (encrypted.length < 28) {
                throw new IllegalArgumentException("Payload de email invalido");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, encrypted, 0, 12));
            return objectMapper.readValue(cipher.doFinal(encrypted, 12, encrypted.length - 12), MensagemEmail.class);
        } catch (GeneralSecurityException | java.io.IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Falha ao ler payload protegido de email", e);
        }
    }
}
