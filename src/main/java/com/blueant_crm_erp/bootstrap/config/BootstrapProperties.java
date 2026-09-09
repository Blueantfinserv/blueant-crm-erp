package com.blueant_crm_erp.bootstrap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * =============================================================================
 * Bootstrap Properties
 * =============================================================================
 *
 * Externalized configuration for Database Bootstrap Module.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bootstrap")
public class BootstrapProperties {
    
    private boolean enabled = true;
    
    private boolean parallel = true;
    
    private int batchSize = 100;
    
    private boolean continueOnFailure = false;
    
}
