package com.blueant_crm_erp.util.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * ==============================================================
 * Crypto Utility
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility for AES-GCM encryption and decryption of sensitive data.
 *
 * NOTE:
 * - DO NOT use this class for password hashing.
 * - Password hashing should always use PasswordUtil (BCrypt).
 *
 * Thread Safe : Yes
 * ==============================================================
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CryptoUtil {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";

    /**
     * 128-bit authentication tag.
     */
    private static final int TAG_LENGTH = 128;

    /**
     * 12 bytes IV recommended for GCM.
     */
    private static final int IV_LENGTH = 12;

    /**
     * AES-256 key (32 bytes).
     *
     * NOTE:
     * Replace this with a value loaded securely from application.yml,
     * environment variables, Vault, or AWS Secrets Manager.
     */
    private static final String SECRET_KEY =
            "ChangeThisToYour32CharacterSecretKey!";

    private static final SecretKey SECRET =
            new SecretKeySpec(
                    SECRET_KEY.substring(0, 32).getBytes(StandardCharsets.UTF_8),
                    AES
            );

    /**
     * Encrypts plain text.
     *
     * @param plainText plain text
     * @return encrypted Base64 string
     */
    public static String encrypt(String plainText) {

        if (plainText == null || plainText.isBlank()) {
            return null;
        }

        try {

            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    SECRET,
                    new GCMParameterSpec(TAG_LENGTH, iv)
            );

            byte[] encrypted = cipher.doFinal(
                    plainText.getBytes(StandardCharsets.UTF_8)
            );

            ByteBuffer buffer =
                    ByteBuffer.allocate(iv.length + encrypted.length);

            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getEncoder()
                    .encodeToString(buffer.array());

        } catch (Exception ex) {
            throw new IllegalStateException("Unable to encrypt data.", ex);
        }
    }

    /**
     * Decrypts encrypted text.
     *
     * @param encryptedText encrypted Base64 string
     * @return original text
     */
    public static String decrypt(String encryptedText) {

        if (encryptedText == null || encryptedText.isBlank()) {
            return null;
        }

        try {

            byte[] decoded =
                    Base64.getDecoder().decode(encryptedText);

            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(AES_GCM);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    SECRET,
                    new GCMParameterSpec(TAG_LENGTH, iv)
            );

            return new String(
                    cipher.doFinal(encrypted),
                    StandardCharsets.UTF_8
            );

        } catch (Exception ex) {
            throw new IllegalStateException("Unable to decrypt data.", ex);
        }
    }

}