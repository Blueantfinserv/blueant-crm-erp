package com.blueant_crm_erp.common.enums;

/**
 * ============================================================================
 * Data Access Level
 * ============================================================================
 *
 * Defines the scope of data visibility for a user based on
 * their assigned role in the BlueAnt CRM ERP Platform.
 *
 * This enum is used by:
 * - Role Module
 * - User Module
 * - Lead Module
 * - Client Module
 * - Dashboard Module
 * - Reports Module
 * - Spring Security
 *
 * Examples:
 *
 * SUPER_ADMIN  -> ALL
 * DIRECTOR     -> ALL
 * SALES_MANAGER-> HIERARCHY
 * TEAM_LEADER  -> TEAM
 * SALES_PERSON -> SELF
 *
 * Project : BlueAnt CRM ERP Platform
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 */
public enum DataAccessLevel {

    /**
     * User can access only their own records.
     *
     * Example:
     * Sales Person
     */
    SELF,

    /**
     * User can access their own records and the
     * records of users directly reporting to them.
     *
     * Example:
     * Team Leader
     */
    TEAM,

    /**
     * User can access all records within their
     * complete reporting hierarchy.
     *
     * Example:
     * Sales Manager
     */
    HIERARCHY,

    /**
     * User has unrestricted access to all records
     * across the organization.
     *
     * Example:
     * Director
     * Super Admin
     */
    ALL

}