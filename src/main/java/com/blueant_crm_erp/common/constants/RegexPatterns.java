package com.blueant_crm_erp.common.constants;

/**
 * Regular Expression Patterns
 *
 * Centralized regex patterns used across the
 * BlueAnt CRM ERP Platform.
 *
 * Used By:
 * - ValidationUtil
 * - EmailValidator
 * - MobileValidator
 * - PasswordUtil
 * - PanValidator
 * - AadhaarValidator
 * - GSTValidator
 *
 * NOTE:
 * Never hardcode regex patterns inside services
 * or validators.
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class RegexPatterns {

    private RegexPatterns() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * ==========================================================
     * USER
     * ==========================================================
     */

    /**
     * Username
     * 4-30 characters
     */
    public static final String USERNAME =
            "^[A-Za-z0-9._]{4,30}$";

    /**
     * Employee Code
     * Example:
     * EMP0001
     */
    public static final String EMPLOYEE_CODE =
            "^[A-Z]{2,5}[0-9]{2,10}$";

    /*
     * ==========================================================
     * CONTACT
     * ==========================================================
     */

    /**
     * Email
     */
    public static final String EMAIL =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    /**
     * Indian Mobile Number
     */
    public static final String MOBILE =
            "^[6-9]\\d{9}$";

    /*
     * ==========================================================
     * AUTHENTICATION
     * ==========================================================
     */

    /**
     * Strong Password
     *
     * At least
     * 1 Uppercase
     * 1 Lowercase
     * 1 Digit
     * 1 Special Character
     * 8-50 characters
     */
    public static final String PASSWORD =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,50}$";

    /**
     * OTP
     */
    public static final String OTP =
            "^\\d{4,8}$";

    /*
     * ==========================================================
     * INDIA
     * ==========================================================
     */

    /**
     * PAN Number
     */
    public static final String PAN =
            "^[A-Z]{5}[0-9]{4}[A-Z]{1}$";

    /**
     * Aadhaar Number
     */
    public static final String AADHAAR =
            "^[2-9]{1}[0-9]{11}$";

    /**
     * GST Number
     */
    public static final String GST =
            "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";

    /**
     * IFSC Code
     */
    public static final String IFSC =
            "^[A-Z]{4}0[A-Z0-9]{6}$";

    /**
     * PIN Code
     */
    public static final String PIN_CODE =
            "^[1-9][0-9]{5}$";

    /*
     * ==========================================================
     * DATE
     * ==========================================================
     */

    /**
     * yyyy-MM-dd
     */
    public static final String DATE =
            "^\\d{4}-\\d{2}-\\d{2}$";

    /**
     * HH:mm:ss
     */
    public static final String TIME =
            "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$";

    /*
     * ==========================================================
     * FILE
     * ==========================================================
     */

    /**
     * Image Extensions
     */
    public static final String IMAGE =
            ".*\\.(jpg|jpeg|png|gif|webp)$";

    /**
     * PDF
     */
    public static final String PDF =
            ".*\\.pdf$";

    /*
     * ==========================================================
     * URL
     * ==========================================================
     */

    /**
     * HTTP/HTTPS URL
     */
    public static final String URL =
            "^(https?|ftp)://.*$";

}