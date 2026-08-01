package com.blueant_crm_erp.common.constants;

/**
 * Sort Constants
 *
 * Centralized sorting fields used across the application.
 *
 * Used By:
 * - PaginationUtil
 * - User Module
 * - Role Module
 * - Lead Module
 * - Meeting Module
 * - Client Module
 * - Reports Module
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public final class SortConstants {

    private SortConstants() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * ==========================================================
     * AUDIT FIELDS
     * ==========================================================
     */

    public static final String ID = "id";

    public static final String CREATED_AT = "createdAt";

    public static final String UPDATED_AT = "updatedAt";

    public static final String CREATED_BY = "createdBy";

    public static final String UPDATED_BY = "updatedBy";

    /*
     * ==========================================================
     * USER MODULE
     * ==========================================================
     */

    public static final String FIRST_NAME = "firstName";

    public static final String LAST_NAME = "lastName";

    public static final String FULL_NAME = "fullName";

    public static final String EMAIL = "email";

    public static final String MOBILE_NUMBER = "mobileNumber";

    public static final String USERNAME = "username";

    public static final String STATUS = "status";

    /*
     * ==========================================================
     * ROLE MODULE
     * ==========================================================
     */

    public static final String ROLE_NAME = "roleName";

    /*
     * ==========================================================
     * TEAM / HIERARCHY
     * ==========================================================
     */

    public static final String TEAM_NAME = "teamName";

    public static final String DEPARTMENT_NAME = "departmentName";

    public static final String DESIGNATION_NAME = "designationName";

    /*
     * ==========================================================
     * LEAD MODULE
     * ==========================================================
     */

    public static final String CLIENT_NAME = "clientName";

    public static final String COMPANY_NAME = "companyName";

    public static final String LOCATION = "location";

    public static final String LEAD_STATUS = "leadStatus";

    public static final String LEAD_STAGE = "leadStage";

    public static final String NEXT_PLAN_DATE = "nextPlanDate";

    public static final String LAST_CALL_DATE = "lastCallDate";

    /*
     * ==========================================================
     * MEETING MODULE
     * ==========================================================
     */

    public static final String MEETING_DATE = "meetingDate";

    public static final String MEETING_TYPE = "meetingType";

    /*
     * ==========================================================
     * CLIENT MODULE
     * ==========================================================
     */

    public static final String PAN_NUMBER = "panNumber";

    public static final String PRODUCT_TYPE = "productType";

    public static final String INVESTMENT_AMOUNT = "investmentAmount";

    /*
     * ==========================================================
     * TRANSACTION MODULE
     * ==========================================================
     */

    public static final String TRANSACTION_DATE = "transactionDate";

    public static final String PAYMENT_DATE = "paymentDate";

    public static final String AMOUNT = "amount";

    /*
     * ==========================================================
     * REPORT MODULE
     * ==========================================================
     */

    public static final String REPORT_DATE = "reportDate";

}