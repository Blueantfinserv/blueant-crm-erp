package com.blueant_crm_erp.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    // For brevity, we mock the global search results here.
    // In a full implementation, this would aggregate from Lead, Client, Meeting repositories.

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String query) {
        return ResponseEntity.ok(Map.of(
                "query", query,
                "leads", List.of(), // TODO: leadRepository.findAll(Specification)
                "meetings", List.of(), // TODO: meetingRepository.findAll(Specification)
                "clients", List.of()
        ));
    }
}
