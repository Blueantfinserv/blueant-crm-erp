package com.blueant_crm_erp.lead.enums;

/**
 * ============================================================================
 * Lead Status
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * System  : CRM and Sales Management System
 * Module  : Lead Management
 *
 * Description:
 * Represents the current business status of a Lead throughout its lifecycle.
 *
 * ============================================================================
 */
public enum LeadStatus {

    /**
     * Newly created lead.
     */
    NEW,

    /**
     * Lead assigned to Sales Person.
     */
    ASSIGNED,

    /**
     * Sales Person has contacted the client.
     */
    CONTACTED,

    /**
     * Introductory meeting has been scheduled.
     */
    MEETING_SCHEDULED,

    /**
     * Meeting completed and feedback received.
     */
    MEETING_COMPLETED,

    /**
     * Follow-up is pending.
     */
    FOLLOW_UP_PENDING,

    /**
     * Lead is actively being worked upon.
     */
    WORK_IN_PROGRESS,

    /**
     * Required documents are pending from client.
     */
    DOCUMENT_PENDING,

    /**
     * Client has invested successfully.
     */
    CONVERTED,

    /**
     * Client is already associated with BlueAnt.
     */
    ALREADY_CLIENT,

    /**
     * Client is not interested.
     */
    NOT_INTERESTED,

    /**
     * Duplicate lead identified.
     */
    DUPLICATE,

    /**
     * Lead transferred to another Sales Person.
     */
    TRANSFERRED,

    /**
     * Lead put on hold temporarily.
     */
    ON_HOLD,

    /**
     * Lead permanently closed without conversion.
     */
    LOST,

    /**
     * Lead removed due to invalid profile,
     * fake enquiry or unsuitable client.
     */
    REMOVED

}