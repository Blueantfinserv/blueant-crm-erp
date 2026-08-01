package com.blueant_crm_erp.util.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Utility class for password related operations.
 *
 * Responsibilities:
 * - Encode password
 * - Verify password
 * - Validate password strength
 * - Generate secure random password
 *
 * Business Rules:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 * - At least one special character
 *
 * Used In:
 * - Authentication Module
 * - User Module
 * - Password Reset
 * - Change Password
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class PasswordUtil {

    /**
     * BCrypt Password Encoder
     */
    private static final PasswordEncoder PASSWORD_ENCODER =
            new BCryptPasswordEncoder();

    /**
     * Secure Random Generator
     */
    private static final SecureRandom RANDOM =
            new SecureRandom();

    /**
     * Allowed characters for password generation.
     */
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "abcdefghijklmnopqrstuvwxyz"
                    + "0123456789"
                    + "@#$%&*!?";

    /**
     * Password Regex
     */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile(
                    "^(?=.*[a-z])" +
                            "(?=.*[A-Z])" +
                            "(?=.*\\d)" +
                            "(?=.*[@#$%&*!?])" +
                            "[A-Za-z\\d@#$%&*!?]{8,50}$"
            );

    private PasswordUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Encode raw password.
     *
     * @param rawPassword plain password
     * @return encrypted password
     */
    public static String encode(String rawPassword) {

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }

        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * Verify password.
     *
     * @param rawPassword plain password
     * @param encodedPassword encrypted password
     * @return true if password matches
     */
    public static boolean matches(
            String rawPassword,
            String encodedPassword) {

        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        return PASSWORD_ENCODER.matches(
                rawPassword,
                encodedPassword
        );
    }

    /**
     * Validate password strength.
     *
     * @param password raw password
     * @return true if valid
     */
    public static boolean isStrongPassword(String password) {

        if (password == null) {
            return false;
        }

        return PASSWORD_PATTERN
                .matcher(password)
                .matches();
    }

    /**
     * Generate secure random password.
     *
     * @param length password length
     * @return generated password
     */
    public static String generateRandomPassword(int length) {

        if (length < 8) {
            throw new IllegalArgumentException(
                    "Password length must be at least 8 characters."
            );
        }

        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            int index = RANDOM.nextInt(CHARACTERS.length());

            password.append(
                    CHARACTERS.charAt(index)
            );
        }

        return password.toString();
    }

}