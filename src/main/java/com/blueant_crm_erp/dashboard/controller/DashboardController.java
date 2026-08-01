package com.blueant_crm_erp.dashboard.controller;

import com.blueant_crm_erp.dashboard.dto.DashboardSummaryResponse;
import com.blueant_crm_erp.dashboard.dto.PipelineResponse;
import com.blueant_crm_erp.dashboard.service.DashboardService;
import com.blueant_crm_erp.auth.security.CustomUserDetails;
import com.blueant_crm_erp.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SALES', 'LEADER', 'ADMIN')")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityUtil.getPrincipal();
        return ResponseEntity.ok(dashboardService.getDashboardSummary(userDetails.getUserId()));
    }

    @GetMapping("/pipeline")
    @PreAuthorize("hasAnyRole('SALES', 'LEADER', 'ADMIN')")
    public ResponseEntity<PipelineResponse> getPipeline() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityUtil.getPrincipal();
        return ResponseEntity.ok(dashboardService.getPipelineSummary(userDetails.getUserId()));
    }
}
