package com.blueant_crm_erp.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repairAndMigrateStrategy() {
        return flyway -> {
            // Realigns the checksums of applied migrations with available migrations
            flyway.repair();
            flyway.migrate();
        };
    }
}
