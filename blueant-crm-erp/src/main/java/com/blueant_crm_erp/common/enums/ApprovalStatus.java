package com.blueant_crm_erp.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Generic Approval Status
 *
 * Used across all approval workflows in the application.
 *
 * Examples:
 * - User Approval
 * - Role Approval
 * - Lead Transfer Approval
 * - Client Onboarding Approval
 * - Document Verification
 * - Service Request Approval
 * - Incentive Approval
 * - Transaction Approval
 *
 * Author : BlueAnt CRM ERP
 * Version : 1.0
 */
@Getter
@RequiredArgsConstructor
public enum ApprovalStatus {

    PENDING("Pending"),

    APPROVED("Approved"),

    REJECTED("Rejected"),

    CANCELLED("Cancelled"),

    ON_HOLD("On Hold");

    /**
     * Display Name
     */
    private final String displayName;

    /**
     * Returns true if request is approved.
     */
    public boolean isApproved() {
        return this == APPROVED;
    }

    /**
     * Returns true if request is pending.
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Returns true if request is rejected.
     */
    public boolean isRejected() {
        return this == REJECTED;
    }

    /**
     * Convert String to Enum
     */
    public static ApprovalStatus from(String value) {

        return Arrays.stream(values())
                .filter(status ->
                        status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid Approval Status : " + value
                        ));
    }
}