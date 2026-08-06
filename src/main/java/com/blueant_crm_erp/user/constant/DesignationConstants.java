package com.blueant_crm_erp.user.constant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DesignationConstants {
    public static final String API_BASE = "/v1/designations";
    public static final String DESIGNATION_ID = "/{designationId}";
    public static final String STATUS = "/{designationId}/status";
    public static final String RESTORE = "/{designationId}/restore";
    public static final String SEARCH = "/search";
    public static final String DROPDOWN = "/dropdown";
    public static final String ALL = "/all";
    public static final String NAME = "/name/{designationName}";
}
