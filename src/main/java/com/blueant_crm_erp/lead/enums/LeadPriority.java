package com.blueant_crm_erp.lead.enums;

/**
 * ============================================================================
 * Lead Priority
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * System  : CRM and Sales Management System
 * Module  : Lead Management
 *
 * Description:
 * Represents the business priority assigned to a Lead.
 *
 * Lead Priority helps determine:
 * - Follow-up urgency
 * - Assignment preference
 * - Dashboard highlighting
 * - Escalation rules
 * - Sales KPI tracking
 * - Notification priority
 *
 * ============================================================================
 */
public enum LeadPriority {

    /**
     * Lowest business priority.
     * Follow-up can be scheduled normally.
     */
    LOW,

    /**
     * Normal business priority.
     * Default priority for most newly created leads.
     */
    MEDIUM,

    /**
     * High business priority.
     * Requires quicker follow-up.
     */
    HIGH,

    /**
     * Critical business priority.
     * Must be contacted immediately.
     */
    CRITICAL,

    /**
     * Premium / VIP client.
     * Highest business priority.
     */
    VIP

}