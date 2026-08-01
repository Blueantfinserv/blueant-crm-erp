package com.blueant_crm_erp.lead.enums;

/**
 * ============================================================================
 * Duplicate Lead Status
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * System  : CRM and Sales Management System
 * Module  : Lead Management
 *
 * Description:
 * Represents the duplicate verification and ownership status of a Lead.
 *
 * This status is used to enforce duplicate lead ownership,
 * transfer policy, and 40-day business rule.
 *
 * ============================================================================
 */
public enum DuplicateLeadStatus {

    /**
     * Lead is unique and has no duplicate record.
     */
    ORIGINAL,

    /**
     * Duplicate verification is in progress.
     */
    UNDER_VERIFICATION,

    /**
     * Duplicate lead found and currently owned
     * by another Sales Person.
     */
    DUPLICATE,

    /**
     * Duplicate lead cannot be transferred because
     * the current owner is actively working on it.
     */
    TRANSFER_RESTRICTED,

    /**
     * Duplicate lead is eligible for transfer
     * based on business rules.
     */
    ELIGIBLE_FOR_TRANSFER,

    /**
     * Lead transferred to another Sales Person.
     */
    TRANSFERRED

}