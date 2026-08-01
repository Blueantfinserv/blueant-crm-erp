package com.blueant_crm_erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Automatically fills @CreatedBy and @LastModifiedBy fields in every entity
     * Gets the currently logged-in user's email from Spring Security context
     *
     * Usage in entity:
     *   @CreatedDate
     *   private LocalDateTime createdAt;
     *
     *   @LastModifiedDate
     *   private LocalDateTime updatedAt;
     *
     *   @CreatedBy
     *   private String createdBy;
     *
     *   @LastModifiedBy
     *   private String updatedBy;
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication.getName().equals("anonymousUser")) {
                return Optional.of("SYSTEM");
            }

            return Optional.of(authentication.getName());
        };
    }
}
