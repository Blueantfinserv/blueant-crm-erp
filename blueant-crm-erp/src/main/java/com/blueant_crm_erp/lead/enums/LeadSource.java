package com.blueant_crm_erp.lead.enums;

/**
 * ============================================================================
 * Lead Source
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * System  : CRM and Sales Management System
 * Module  : Lead Management
 *
 * Description:
 * Represents the acquisition channel from which a Lead was generated.
 *
 * Used for:
 * - Sales Analytics
 * - Marketing ROI
 * - Campaign Tracking
 * - Lead Attribution
 * - Conversion Analysis
 *
 * ============================================================================
 */
public enum LeadSource {

    /**
     * Company Website
     */
    WEBSITE,

    /**
     * Google Search / Google Ads
     */
    GOOGLE,

    /**
     * Facebook Organic / Ads
     */
    FACEBOOK,

    /**
     * Instagram Organic / Ads
     */
    INSTAGRAM,

    /**
     * LinkedIn
     */
    LINKEDIN,

    /**
     * YouTube Campaign
     */
    YOUTUBE,

    /**
     * WhatsApp Campaign
     */
    WHATSAPP,

    /**
     * Walk-in Client
     */
    WALK_IN,

    /**
     * Existing Client Referral
     */
    REFERRAL,

    /**
     * Employee Referral
     */
    EMPLOYEE_REFERRAL,

    /**
     * Tele Calling Team
     */
    TELE_CALLING,

    /**
     * Direct Sales / Field Visit
     */
    FIELD_VISIT,

    /**
     * Branch Visit
     */
    BRANCH,

    /**
     * Seminar / Investor Awareness Program
     */
    SEMINAR,

    /**
     * Exhibition / Event
     */
    EVENT,

    /**
     * Email Marketing Campaign
     */
    EMAIL_CAMPAIGN,

    /**
     * SMS Marketing Campaign
     */
    SMS_CAMPAIGN,

    /**
     * Partner / Business Associate
     */
    CHANNEL_PARTNER,

    /**
     * Financial Advisor / Agent
     */
    BUSINESS_PARTNER,

    /**
     * Imported from External System
     */
    IMPORT,

    /**
     * API Integration
     */
    API,

    /**
     * Manually created by Sales Person
     */
    MANUAL,

    /**
     * Any unidentified source
     */
    OTHER

}