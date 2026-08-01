package com.blueant_crm_erp.util.auth;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utility class for One-Time Password (OTP) operations.
 *
 * Responsibilities:
 * - Generate Numeric OTP
 * - Generate Alphanumeric OTP
 * - Validate OTP Format
 * - Compare OTP
 * - Generate Expiry Time
 *
 * NOTE:
 * This class DOES NOT:
 * - Store OTP
 * - Send OTP
 * - Verify Expiry from Database/Redis
 *
 * Those responsibilities belong to OtpService.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class OtpUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String NUMBERS = "0123456789";

    private static final String ALPHA_NUMERIC =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "abcdefghijklmnopqrstuvwxyz"
                    + "0123456789";

    private static final Pattern NUMERIC_OTP_PATTERN =
            Pattern.compile("^\\d{4,8}$");

    private OtpUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generates numeric OTP.
     *
     * Example:
     * 483921
     */
    public static String generateNumericOtp(int length) {

        validateLength(length);

        StringBuilder otp = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            otp.append(
                    NUMBERS.charAt(
                            RANDOM.nextInt(NUMBERS.length())
                    )
            );
        }

        return otp.toString();
    }

    /**
     * Generates alphanumeric OTP.
     *
     * Example:
     * A9X2M7
     */
    public static String generateAlphaNumericOtp(int length) {

        validateLength(length);

        StringBuilder otp = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            otp.append(
                    ALPHA_NUMERIC.charAt(
                            RANDOM.nextInt(ALPHA_NUMERIC.length())
                    )
            );
        }

        return otp.toString();
    }

    /**
     * Returns OTP expiry time.
     */
    public static LocalDateTime expiryAfterMinutes(int minutes) {

        if (minutes <= 0) {
            throw new IllegalArgumentException(
                    "Expiry minutes must be greater than zero."
            );
        }

        return LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * Validate OTP format.
     */
    public static boolean isValidOtp(String otp) {

        if (otp == null) {
            return false;
        }

        return NUMERIC_OTP_PATTERN
                .matcher(otp)
                .matches();
    }

    /**
     * Compare OTP values.
     */
    public static boolean matches(
            String enteredOtp,
            String actualOtp) {

        return Objects.equals(
                enteredOtp,
                actualOtp
        );
    }

    /**
     * Mask OTP for logs.
     *
     * Example:
     * ****21
     */
    public static String mask(String otp) {

        if (otp == null || otp.length() < 2) {
            return "****";
        }

        return "*".repeat(otp.length() - 2)
                + otp.substring(otp.length() - 2);
    }

    /**
     * Validate OTP length.
     */
    private static void validateLength(int length) {

        if (length < 4 || length > 8) {

            throw new IllegalArgumentException(
                    "OTP length must be between 4 and 8 digits."
            );
        }
    }

}