package com.blueant_crm_erp.util.transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * ==============================================================
 * Transaction Utility
 * Project : BlueAnt CRM ERP Platform
 *
 * Purpose:
 * Provides reusable helper methods for transaction operations.
 *
 * NOTE:
 * Business rules (Commission, Incentive, Payment Processing)
 * should NOT be implemented here.
 *
 * Thread Safe : Yes
 * ==============================================================
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionUtil {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * Generates unique transaction reference.
     *
     * Example:
     * TXN-8F5D3C9A2B7E
     */
    public static String generateTransactionReference() {
        return "TXN-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();
    }

    /**
     * Generates receipt number.
     *
     * Example:
     * RCPT-20260706143055123
     */
    public static String generateReceiptNumber() {
        return "RCPT-" + System.currentTimeMillis();
    }

    /**
     * Checks whether amount is valid.
     */
    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null
                && amount.compareTo(ZERO) > 0;
    }

    /**
     * Calculates remaining amount.
     */
    public static BigDecimal calculateRemainingAmount(
            BigDecimal totalAmount,
            BigDecimal paidAmount) {

        if (totalAmount == null) {
            totalAmount = ZERO;
        }

        if (paidAmount == null) {
            paidAmount = ZERO;
        }

        BigDecimal remaining = totalAmount.subtract(paidAmount);

        return remaining.max(ZERO);
    }

    /**
     * Calculates net payable amount.
     */
    public static BigDecimal calculateNetAmount(
            BigDecimal amount,
            BigDecimal tax,
            BigDecimal discount) {

        BigDecimal base = Objects.requireNonNullElse(amount, ZERO);
        BigDecimal taxAmount = Objects.requireNonNullElse(tax, ZERO);
        BigDecimal discountAmount = Objects.requireNonNullElse(discount, ZERO);

        return base
                .add(taxAmount)
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Rounds amount to two decimal places.
     */
    public static BigDecimal round(BigDecimal amount) {

        if (amount == null) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Formats amount in Indian currency.
     *
     * Example:
     * ₹1,25,000.00
     */
    public static String formatCurrency(BigDecimal amount) {

        if (amount == null) {
            amount = ZERO;
        }

        NumberFormat formatter =
                NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        return formatter.format(amount);
    }

    /**
     * Returns true if transaction is successful.
     */
    public static boolean isSuccessStatus(String status) {
        return "SUCCESS".equalsIgnoreCase(status);
    }

    /**
     * Returns true if transaction is pending.
     */
    public static boolean isPendingStatus(String status) {
        return "PENDING".equalsIgnoreCase(status);
    }

    /**
     * Returns true if transaction is failed.
     */
    public static boolean isFailedStatus(String status) {
        return "FAILED".equalsIgnoreCase(status);
    }

    /**
     * Returns true if refund can be initiated.
     */
    public static boolean isRefundEligible(String status) {
        return isSuccessStatus(status);
    }

}