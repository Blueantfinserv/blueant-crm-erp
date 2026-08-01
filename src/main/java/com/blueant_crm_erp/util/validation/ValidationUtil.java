package com.blueant_crm_erp.util.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * ==============================================================
 * Validation Utility
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Central validation utility that delegates validation
 * to specialized validator classes.
 *
 * Thread Safe : Yes
 * ==============================================================
 *
 * @author BlueAnt CRM ERP
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationUtil {

    /**
     * Email Validation
     */
    public static boolean isValidEmail(String email) {
        return EmailValidator.isValid(email);
    }

    /**
     * Mobile Validation
     */
    public static boolean isValidMobile(String mobile) {
        return MobileValidator.isValid(mobile);
    }

    /**
     * Password Validation
     */
    public static boolean isValidPassword(String password) {
        return PasswordValidator.isValid(password);
    }

   /**
//     * PAN Validation
//     */
//    public static boolean isValidPAN(String pan) {
//        return PANValidator.isValid(pan);
//    }
//
//    /**
//     * Aadhaar Validation
//     */
//    public static boolean isValidAadhaar(String aadhaar) {
//        return AadhaarValidator.isValid(aadhaar);
//    }
//
//    /**
//     * IFSC Validation
//     */
//    public static boolean isValidIFSC(String ifsc) {
//        return IFSCValidator.isValid(ifsc);
//    }
//
//    /**
//     * Bank Account Validation
//     */
//    public static boolean isValidBankAccount(String accountNumber) {
//        return BankAccountValidator.isValid(accountNumber);
//    }

    /**
     * PIN Code Validation
     */
    public static boolean isValidPinCode(String pinCode) {
        return PinCodeValidator.isValid(pinCode);
    }

    /**
     * UUID Validation
     */
    public static boolean isValidUUID(String uuid) {
        return UUIDValidator.isValid(uuid);
    }

    /**
     * UUID Validation
     */
    public static boolean isValidUUID(UUID uuid) {
        return uuid != null;
    }

    /**
     * Date Validation
     */
    public static boolean isValidDate(String date) {
        return DateValidator.isValidIsoDate(date);
    }

    /**
     * Date Validation using formatter
     */
    public static boolean isValidDate(String date,
                                      DateTimeFormatter formatter) {
        return DateValidator.isValid(date, formatter);
    }

    /**
     * LocalDate Validation
     */
    public static boolean isValidDate(LocalDate date) {
        return date != null;
    }

    /**
     * LocalDateTime Validation
     */
    public static boolean isValidDate(LocalDateTime dateTime) {
        return dateTime != null;
    }

    /**
     * File Validation
     */
    public static boolean isValidFile(MultipartFile file) {
        return FileValidator.isValid(file);
    }

    /**
     * Amount Validation
     */
    public static boolean isValidAmount(BigDecimal amount) {

        return amount != null
                && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * String Validation
     */
    public static boolean hasText(String value) {

        return value != null
                && !value.trim().isEmpty();
    }

    /**
     * Object Validation
     */
    public static boolean isNotNull(Object object) {
        return object != null;
    }

    /**
     * Collection Validation
     */
    public static boolean isNotEmpty(Iterable<?> iterable) {

        return iterable != null
                && iterable.iterator().hasNext();
    }

}