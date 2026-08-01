package com.blueant_crm_erp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlywayTest {

    @Test
    void contextLoads() {
        // Will fail if Flyway migration fails or EntityManagerFactory fails to initialize
    }
}
