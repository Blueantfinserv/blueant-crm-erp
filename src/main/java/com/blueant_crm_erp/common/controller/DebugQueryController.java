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
    public List<Map<String, Object>> runQuery(@RequestParam String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
