package com.blueant_crm_erp.lead.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ============================================================================
 * Best Time to Meet (Lead-level client preference)
 * ============================================================================
 *
 * Project : BlueAnt CRM ERP
 * Module  : Lead Management
 *
 * Description:
 * Represents the preferred meeting time window for a physical lead.
 *
 * Allowed options:
 * 1. 9:00 AM - 12:00 PM  (NINE_TO_TWELVE)
 * 2. 12:00 PM - 3:00 PM  (TWELVE_TO_THREE)
 * 3. 3:00 PM - 6:00 PM   (THREE_TO_SIX)
 * 4. 6:00 PM - 9:00 PM   (SIX_TO_NINE)
 *
 * ============================================================================
 */
@Getter
@RequiredArgsConstructor
public enum BestTimeToMeet {

    NINE_TO_TWELVE("9:00 AM - 12:00 PM"),
    TWELVE_TO_THREE("12:00 PM - 3:00 PM"),
    THREE_TO_SIX("3:00 PM - 6:00 PM"),
    SIX_TO_NINE("6:00 PM - 9:00 PM");

    private final String displayName;

}
