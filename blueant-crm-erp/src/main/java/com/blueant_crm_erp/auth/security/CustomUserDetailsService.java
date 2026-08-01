package com.blueant_crm_erp.auth.security;

import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * =============================================================================
 * Custom User Details Service
 * =============================================================================
 *
 * Loads authenticated users for Spring Security.
 *
 * Supported Login Identifiers
 * -----------------------------------------------------------------------------
 * • Employee Code
 * • Email Address
 * • Mobile Number
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Authentication
 * =============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads authenticated user.
     */
    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        log.debug("Loading user using identifier: {}", identifier);

        User user = userRepository
                .findByEmployeeCodeIgnoreCaseOrEmailIgnoreCaseOrMobileNumberAndDeletedFalse(
                        identifier,
                        identifier,
                        identifier
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid employee code, email or mobile number."
                        ));

        return new CustomUserDetails(
                user,
                buildAuthorities(user)
        );
    }

    /**
     * Builds Spring Security Authorities.
     */
    private Collection<? extends GrantedAuthority> buildAuthorities(User user) {

        Set<GrantedAuthority> authorities = new HashSet<>();

        Role role = user.getRole();

        if (role != null && Status.ACTIVE.equals(role.getStatus())) {

            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + role.getCode()
                    )
            );

        }

        return authorities;
    }

}