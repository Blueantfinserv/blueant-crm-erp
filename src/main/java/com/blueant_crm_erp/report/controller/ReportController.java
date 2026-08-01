package com.blueant_crm_erp.report.controller;

import com.blueant_crm_erp.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/daily")
    public ResponseEntity<Object> getDailyReport() {
        return ResponseEntity.ok(reportService.generateDailyReport());
    }

    @GetMapping("/salesperson")
    public ResponseEntity<Object> getSalespersonReport(@RequestParam Long salespersonId) {
        return ResponseEntity.ok(reportService.generateSalespersonReport(salespersonId));
    }
}
