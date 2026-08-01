package com.blueant_crm_erp.user.constant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DepartmentConstants {
    public static final String API_BASE = "/api/v1/departments";
    public static final String DEPARTMENT_ID = "/{departmentId}";
    public static final String STATUS = "/{departmentId}/status";
    public static final String RESTORE = "/{departmentId}/restore";
    public static final String SEARCH = "/search";
    public static final String DROPDOWN = "/dropdown";
    public static final String ALL = "/all";
    public static final String NAME = "/name/{departmentName}";
}
