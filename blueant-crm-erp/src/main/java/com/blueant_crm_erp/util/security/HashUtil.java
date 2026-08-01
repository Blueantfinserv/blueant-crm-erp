package com.blueant_crm_erp.util.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * ==============================================================
 * Hash Utility
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Utility class for generating cryptographic hashes.
 *
 * NOTE:
 * - DO NOT use this class for password hashing.
 * - Use PasswordUtil (BCrypt) for passwords.
 *
 * Supported Algorithms:
 * - SHA-256
 * - SHA-384
 * - SHA-512
 *
 * Thread Safe : Yes
 * ==============================================================
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HashUtil {

    private static final String SHA_256 = "SHA-256";
    private static final String SHA_384 = "SHA-384";
    private static final String SHA_512 = "SHA-512";

    /**
     * Generates SHA-256 hash.
     *
     * @param value input text
     * @return SHA-256 hash
     */
    public static String sha256(String value) {
        return hash(value, SHA_256);
    }

    /**
     * Generates SHA-384 hash.
     *
     * @param value input text
     * @return SHA-384 hash
     */
    public static String sha384(String value) {
        return hash(value, SHA_384);
    }

    /**
     * Generates SHA-512 hash.
     *
     * @param value input text
     * @return SHA-512 hash
     */
    public static String sha512(String value) {
        return hash(value, SHA_512);
    }

    /**
     * Generates hash using the specified algorithm.
     *
     * @param value input text
     * @param algorithm hashing algorithm
     * @return hashed value
     */
    private static String hash(String value, String algorithm) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            byte[] hashedBytes = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashedBytes);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "Unsupported hashing algorithm: " + algorithm,
                    ex
            );
        }
    }

}