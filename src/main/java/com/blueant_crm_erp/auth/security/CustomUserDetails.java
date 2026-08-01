package com.blueant_crm_erp.auth.security;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

/**
 * =============================================================================
 * Custom User Details
 * =============================================================================
 *
 * Spring Security implementation of {@link UserDetails}.
 *
 * Acts as an adapter between the application's User entity
 * and Spring Security authentication framework.
 *
 * Responsibilities
 * -----------------------------------------------------------------------------
 * • Provide authenticated user details
 * • Provide granted authorities
 * • Expose account status
 * • Expose account lock status
 * • Expose credential status
 * • Support Role Based Access Control (RBAC)
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Getter
@RequiredArgsConstructor
public final class CustomUserDetails implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Authenticated User.
     */
    private final User user;

    /**
     * Granted Authorities.
     */
    private final Collection<? extends GrantedAuthority> authorities;

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Returns User Id.
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * Returns Employee Code.
     */
    public String getEmployeeCode() {
        return user.getEmployeeCode();
    }

    /**
     * Returns Full Name.
     */
    public String getFullName() {
        return user.getFullName();
    }

    /**
     * Returns Official Email.
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Returns Mobile Number.
     */
    public String getMobileNumber() {
        return user.getMobileNumber();
    }

    /**
     * Returns Role Code.
     *
     * Example:
     * SUPER_ADMIN
     * ADMIN
     * SALES_MANAGER
     */
    public String getRoleCode() {

        return user.getRole() == null
                ? null
                : user.getRole().getCode();

    }

    /**
     * Returns Role Name.
     *
     * Example:
     * Super Administrator
     * Sales Manager
     */
    public String getRoleName() {

        return user.getRole() == null
                ? null
                : user.getRole().getName();

    }

    /**
     * Returns Department.
     */
    public String getDepartment() {
        return user.getDepartment() == null ? null : user.getDepartment().getName();
    }

    /**
     * Returns Designation.
     */
    public String getDesignation() {
        return user.getDesignation() == null ? null : user.getDesignation().getName();
    }

    /**
     * Returns Team.
     */
    public String getTeam() {
        return user.getTeam() == null ? null : user.getTeam().getTeamName();
    }

    /**
     * Checks whether authenticated user has the given authority.
     *
     * @param authority Permission Code
     * @return true if authority exists
     */
    public boolean hasAuthority(String authority) {

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);

    }

    /**
     * Checks whether authenticated user has the given role.
     *
     * Role comparison is performed using Role Code.
     *
     * @param roleCode Role Code
     * @return true if role exists
     */
    public boolean hasRole(String roleCode) {

        return getRoleCode() != null
                && getRoleCode().equalsIgnoreCase(roleCode);

    }

    // =========================================================================
    // UserDetails Implementation
    // =========================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Employee Code is used as Spring Security Principal.
     */
    @Override
    public String getUsername() {
        return user.getEmployeeCode();
    }

    /**
     * Indicates whether account has expired.
     */
    @Override
    public boolean isAccountNonExpired() {
        return Boolean.TRUE.equals(user.getAccountNonExpired());
    }

    /**
     * Indicates whether account is locked.
     */
    @Override
    public boolean isAccountNonLocked() {
        return !user.isLocked();
    }

    /**
     * Indicates whether credentials have expired.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return Boolean.TRUE.equals(user.getCredentialsNonExpired());
    }

    /**
     * Indicates whether account is enabled.
     *
     * User must:
     * • Be ACTIVE
     * • Be Account Enabled
     */
    @Override
    public boolean isEnabled() {

        return user.isAccountEnabled()
                && Status.ACTIVE.equals(user.getStatus());

    }

}