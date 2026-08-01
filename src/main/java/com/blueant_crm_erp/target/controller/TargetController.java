package com.blueant_crm_erp.target.controller;

import com.blueant_crm_erp.target.entity.Target;
import com.blueant_crm_erp.target.service.TargetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/targets")
@RequiredArgsConstructor
public class TargetController {

    private final TargetService targetService;

    @PostMapping
    @PreAuthorize("hasAuthority('target:write') or hasRole('LEADER') or hasRole('ADMIN')")
    public ResponseEntity<com.blueant_crm_erp.target.dto.response.TargetResponse> setMonthlyTarget(
            @RequestParam Long userId,
            @RequestParam String targetMonth,
            @RequestParam BigDecimal revenue,
            @RequestParam Integer meetings,
            @RequestParam Integer leads,
            @RequestParam Integer followups,
            @RequestParam String currentUser) {
        
        Target target = targetService.setMonthlyTarget(userId, targetMonth, revenue, meetings, leads, followups, currentUser);
        return ResponseEntity.ok(org.mapstruct.factory.Mappers.getMapper(com.blueant_crm_erp.target.mapper.TargetMapper.class).toResponse(target));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('target:read') or hasRole('SALES') or hasRole('LEADER')")
    public ResponseEntity<com.blueant_crm_erp.target.dto.response.TargetResponse> getMonthlyTarget(
            @RequestParam Long userId,
            @RequestParam String targetMonth) {
        return targetService.getTarget(userId, targetMonth)
                .map(target -> org.mapstruct.factory.Mappers.getMapper(com.blueant_crm_erp.target.mapper.TargetMapper.class).toResponse(target))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
