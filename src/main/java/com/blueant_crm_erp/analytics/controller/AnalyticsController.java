package com.blueant_crm_erp.analytics.controller;

import com.blueant_crm_erp.analytics.service.AnalyticsService;
import com.blueant_crm_erp.auth.security.CustomUserDetails;
import com.blueant_crm_erp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/meetings")
    @PreAuthorize("hasAnyRole('SALES', 'LEADER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getMeetingAnalytics() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityUtil.getPrincipal();
        return ResponseEntity.ok(analyticsService.getMeetingAnalytics(userDetails.getUserId()));
    }

    @GetMapping("/sales-kpi")
    @PreAuthorize("hasAnyRole('SALES', 'LEADER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getSalesKpi() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityUtil.getPrincipal();
        return ResponseEntity.ok(analyticsService.getSalesKpi(userDetails.getUserId()));
    }
}
