package com.blueant_crm_erp.user.constant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamConstants {
    public static final String API_BASE = "/api/v1/teams";
    public static final String TEAM_ID = "/{teamId}";
    public static final String STATUS = "/{teamId}/status";
    public static final String RESTORE = "/{teamId}/restore";
    public static final String SEARCH = "/search";
    public static final String DROPDOWN = "/dropdown";
    public static final String ALL = "/all";
    public static final String NAME = "/name/{teamName}";
}
