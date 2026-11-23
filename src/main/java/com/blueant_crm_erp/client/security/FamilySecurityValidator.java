package com.blueant_crm_erp.client.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Security validator enforcing Family Head designation and transfer authorization.
 *
 * Confirmed Business Rule:
 * Only ROLE_CRM_ONBOARDING, ROLE_ADMIN, and ROLE_SUPER_ADMIN may designate,
 * appoint, or transfer an authoritative Family Head.
 *
 * PC_COORDINATOR (and authority PHYSICAL_LEAD_ASSIGN) must never confer
 * Family Head mutation permission.
 */
@Component
public class FamilySecurityValidator {

    private static final Set<String> AUTHORIZED_HEAD_ROLES = Set.of(
            "ROLE_CRM_ONBOARDING",
            "ROLE_ADMIN",
            "ROLE_SUPER_ADMIN"
    );

    /**
     * Enforces that only an authenticated user with ROLE_CRM_ONBOARDING,
     * ROLE_ADMIN, or ROLE_SUPER_ADMIN is allowed to designate or change
     * a Family Head.
     *
     * @throws AccessDeniedException if the current caller is unauthenticated,
     *         anonymous, or lacks one of the three authorized roles.
     */
    public void validateFamilyHeadDesignationAuthorized() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Authentication required to designate or change Family Head.");
        }

        boolean isAuthorized = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(AUTHORIZED_HEAD_ROLES::contains);

        if (!isAuthorized) {
            throw new AccessDeniedException("Only CRM_ONBOARDING, ADMIN, or SUPER_ADMIN is authorized to designate, appoint, or transfer a Family Head.");
        }
    }
}
