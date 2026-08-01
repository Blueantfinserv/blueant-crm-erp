package com.blueant_crm_erp.util.auth;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility class for generic token operations.
 *
 * Responsibilities:
 * - Extract Bearer Token
 * - Validate Bearer Header
 * - Generate Secure Random Token
 * - Generate Refresh Token Value
 * - Generate Verification Token
 * - Mask Token for Logging
 *
 * NOTE:
 * JWT creation and validation are handled by JwtUtil.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class TokenUtil {

    /**
     * Authorization Header Prefix.
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * Secure Random Generator.
     */
    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private TokenUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Extracts JWT token from Authorization header.
     *
     * Example:
     *
     * Authorization:
     * Bearer eyJhbGciOiJIUzI1NiJ9....
     */
    public static String extractBearerToken(String authorizationHeader) {

        if (!hasBearerToken(authorizationHeader)) {
            return null;
        }

        return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
    }

    /**
     * Returns true if Authorization header
     * contains Bearer token.
     */
    public static boolean hasBearerToken(String authorizationHeader) {

        return authorizationHeader != null
                && authorizationHeader.startsWith(BEARER_PREFIX);
    }

    /**
     * Generates secure random token.
     *
     * Used for:
     * - Email Verification
     * - Password Reset
     * - API Token
     */
    public static String generateSecureToken() {

        byte[] bytes = new byte[32];

        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    /**
     * Generates refresh token value.
     */
    public static String generateRefreshToken() {

        return UUID.randomUUID().toString()
                + "-"
                + generateSecureToken();
    }

    /**
     * Compare two tokens.
     */
    public static boolean matches(
            String tokenOne,
            String tokenTwo) {

        return Objects.equals(tokenOne, tokenTwo);
    }

    /**
     * Mask token for logging.
     *
     * Example:
     * eyJhb*************c8D
     */
    public static String mask(String token) {

        if (token == null || token.length() <= 10) {
            return "********";
        }

        return token.substring(0, 5)
                + "*************"
                + token.substring(token.length() - 3);
    }

}