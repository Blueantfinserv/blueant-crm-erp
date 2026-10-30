package com.blueant_crm_erp.client.bulk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Read-Only Audit of Duplicate Mobile Numbers in Test Database")
class AuditDuplicateMobileTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Audit duplicate mobile numbers and soft-deleted records (no PII)")
    void auditDuplicateMobileNumbers() {
        // 1. Total clients count
        Integer totalClients = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients", Integer.class);

        // 2. Total distinct mobile numbers count
        Integer distinctMobiles = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT mobile_number) FROM clients", Integer.class);

        // 3. Count of mobile numbers that appear more than once
        Integer duplicateMobileGroups = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" +
                        "  SELECT mobile_number FROM clients " +
                        "  GROUP BY mobile_number HAVING COUNT(*) > 1" +
                        ") as dups", Integer.class);

        // 4. Total rows affected by duplicate mobile numbers
        Integer totalRowsInDuplicateGroups = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(grp_count), 0) FROM (" +
                        "  SELECT COUNT(*) as grp_count FROM clients " +
                        "  GROUP BY mobile_number HAVING COUNT(*) > 1" +
                        ") as dups", Integer.class);

        // 5. Soft-deleted vs Active breakdown
        Integer activeClients = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE is_deleted = 0", Integer.class);

        Integer deletedClients = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE is_deleted = 1", Integer.class);

        // 6. Max duplication frequency
        Integer maxOccurrences = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(cnt), 0) FROM (" +
                        "  SELECT COUNT(*) as cnt FROM clients GROUP BY mobile_number" +
                        ") as counts", Integer.class);

        System.out.println("=================================================");
        System.out.println("AUDIT RESULT - TEST DATABASE CLIENT MOBILE REPORT");
        System.out.println("=================================================");
        System.out.println("Total Client Records: " + totalClients);
        System.out.println("Distinct Mobile Numbers: " + distinctMobiles);
        System.out.println("Duplicate Mobile Number Groups: " + duplicateMobileGroups);
        System.out.println("Total Rows in Duplicate Groups: " + totalRowsInDuplicateGroups);
        System.out.println("Active Clients (is_deleted = 0): " + activeClients);
        System.out.println("Deleted Clients (is_deleted = 1): " + deletedClients);
        System.out.println("Max Duplication Count for Any Single Mobile: " + maxOccurrences);
        System.out.println("=================================================");

        assertThat(totalClients).isNotNull();
    }
}

