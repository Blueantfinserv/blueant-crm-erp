package com.blueant_crm_erp.auth.mapper;

import com.blueant_crm_erp.auth.dto.response.CurrentUserResponse;
import com.blueant_crm_erp.auth.dto.response.LoginResponse;
import com.blueant_crm_erp.auth.dto.response.RefreshTokenResponse;
import com.blueant_crm_erp.auth.dto.response.TokenResponse;
import com.blueant_crm_erp.auth.dto.response.UserSessionResponse;
import com.blueant_crm_erp.auth.entity.RefreshToken;
import com.blueant_crm_erp.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * =============================================================================
 * Auth Mapper
 * =============================================================================
 *
 * MapStruct mapper responsible for converting Authentication
 * entities into response DTOs.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • User -> LoginResponse
 * • User -> CurrentUserResponse
 * • RefreshToken -> RefreshTokenResponse
 * • Token information -> TokenResponse
 *
 * This mapper DOES NOT:
 * -----------------------------------------------------------------------------
 * • Generate JWT tokens
 * • Encrypt passwords
 * • Validate credentials
 * • Access database
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AuthMapper {

    // =========================================================================
    // User -> Login Response
    // =========================================================================

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "employeeCode", source = "employeeCode")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "mobileNumber", source = "mobileNumber")
    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "department", source = "department.name")
    @Mapping(target = "designation", source = "designation.name")
    @Mapping(target = "team", source = "team.teamName")
    @Mapping(target = "reportingManager", expression = "java(user.getReportingManager() != null ? user.getReportingManager().getFullName() : null)")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    LoginResponse toLoginResponse(User user);

    // =========================================================================
    // User -> Current User Response
    // =========================================================================

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "employeeCode", source = "employeeCode")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "mobileNumber", source = "mobileNumber")
    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "department", source = "department.name")
    @Mapping(target = "designation", source = "designation.name")
    @Mapping(target = "team", source = "team.teamName")
    @Mapping(target = "reportingManager", expression = "java(user.getReportingManager() != null ? user.getReportingManager().getFullName() : null)")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "permissions", ignore = true)
    CurrentUserResponse toCurrentUserResponse(User user);

    // =========================================================================
    // RefreshToken -> Response
    // =========================================================================

    @Mapping(target = "refreshToken", source = "token")
    @Mapping(target = "refreshTokenExpiry", source = "expiryDate")
    RefreshTokenResponse toRefreshTokenResponse(RefreshToken refreshToken);

    // =========================================================================
    // RefreshToken -> User Session Response
    // =========================================================================

    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "deviceName", source = "deviceName")
    @Mapping(target = "browser", source = "browser")
    @Mapping(target = "operatingSystem", source = "operatingSystem")
    @Mapping(target = "ipAddress", source = "ipAddress")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "loginAt", source = "createdAt")
    @Mapping(target = "lastActivityAt", source = "lastActivityAt")
    UserSessionResponse toUserSessionResponse(RefreshToken refreshToken);

    // =========================================================================
    // Token Response
    // =========================================================================

    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "tokenType", ignore = true)
    @Mapping(target = "expiresIn", ignore = true)
    TokenResponse toTokenResponse(User user);

}