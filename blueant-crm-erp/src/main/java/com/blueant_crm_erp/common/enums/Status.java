package com.blueant_crm_erp.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Generic Entity Status.
 *
 * Used By:
 * - User
 * - Role
 * - Department
 * - Team
 * - Designation
 * - Configuration
 * - Notification Template
 *
 * NOTE:
 * Business-specific statuses should use their own enums.
 *
 * Examples:
 * - LeadStatus
 * - ClientStatus
 * - MeetingStatus
 * - ServiceRequestStatus
 * - TransactionStatus
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public enum Status {

    ACTIVE("Active"),

    INACTIVE("Inactive"),

    PENDING("Pending"),

    SUSPENDED("Suspended"),

    ARCHIVED("Archived");

    /**
     * User-friendly display name.
     */
    private final String displayName;

    /**
     * Cached lookup map.
     */
    private static final Map<String, Status> LOOKUP;

    static {

        Map<String, Status> map = new HashMap<>();

        for (Status status : values()) {

            map.put(
                    status.name().toLowerCase(Locale.ENGLISH),
                    status
            );

            map.put(
                    status.displayName.toLowerCase(Locale.ENGLISH),
                    status
            );
        }

        LOOKUP = Collections.unmodifiableMap(map);
    }

    /**
     * Returns true if ACTIVE.
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Returns true if INACTIVE.
     */
    public boolean isInactive() {
        return this == INACTIVE;
    }

    /**
     * Returns true if PENDING.
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Returns true if SUSPENDED.
     */
    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    /**
     * Returns true if ARCHIVED.
     */
    public boolean isArchived() {
        return this == ARCHIVED;
    }

    /**
     * Returns Status from enum name or display name.
     *
     * Examples:
     *
     * ACTIVE
     * active
     * Active
     *
     * all return Status.ACTIVE
     */
    public static Status from(String value) {

        Objects.requireNonNull(
                value,
                "Status value cannot be null."
        );

        Status status = LOOKUP.get(
                value.trim().toLowerCase(Locale.ENGLISH)
        );

        if (status == null) {
            throw new IllegalArgumentException(
                    "Invalid Status: " + value
            );
        }

        return status;
    }

}