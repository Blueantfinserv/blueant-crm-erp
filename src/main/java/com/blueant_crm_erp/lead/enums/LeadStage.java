package com.blueant_crm_erp.lead.enums;

/**
 * ============================================================================
 * Lead Stage
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * System  : CRM and Sales Management System
 * Module  : Lead Management
 *
 * Description:
 * Represents the current processing stage of a Lead in its lifecycle.
 *
 * Note:
 * Stage indicates WHERE the lead is in the business process,
 * while LeadStatus indicates WHAT is the business outcome.
 *
 * ============================================================================
 */
public enum LeadStage {

    /**
     * Lead submitted by Sales Person.
     */
    LEAD_CREATED,

    /**
     * Lead assigned to Sales Person.
     */
    LEAD_ASSIGNED,

    /**
     * Duplicate verification in progress.
     */
    DUPLICATE_CHECK,

    /**
     * Initial contact with client.
     */
    FIRST_CONTACT,

    /**
     * Introductory meeting.
     */
    INTRO_MEETING,

    /**
     * Introductory meeting planned.
     */
    INTRO_MEETING_SCHEDULED,

    /**
     * Introductory meeting completed.
     */
    INTRO_MEETING_COMPLETED,

    /**
     * Follow-up scheduled.
     */
    FOLLOW_UP,

    /**
     * Financial requirement analysis.
     */
    NEED_ANALYSIS,

    /**
     * Product discussion with client.
     */
    PRODUCT_DISCUSSION,

    /**
     * Investment proposal shared.
     */
    PROPOSAL_SHARED,

    /**
     * KYC / PAN / Documents collection.
     */
    DOCUMENT_COLLECTION,

    /**
     * Investment confirmation received.
     */
    INVESTMENT_CONFIRMED,

    /**
     * Service Request submitted.
     */
    SERVICE_REQUEST_CREATED,

    /**
     * CRM Head received documents.
     */
    CRM_HANDOVER,

    /**
     * Process Coordinator verification.
     */
    PC_VERIFICATION,

    /**
     * Client onboarding completed.
     */
    CLIENT_ONBOARDED,

    /**
     * Lead process completed.
     */
    COMPLETED

}