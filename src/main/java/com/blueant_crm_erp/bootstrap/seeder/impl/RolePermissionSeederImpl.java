package com.blueant_crm_erp.bootstrap.seeder.impl;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;
import com.blueant_crm_erp.bootstrap.seeder.BootstrapSeeder;
import com.blueant_crm_erp.bootstrap.config.BootstrapProperties;
import com.blueant_crm_erp.permission.entity.Permission;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.entity.RolePermission;
import com.blueant_crm_erp.role.repository.RolePermissionRepository;
import com.blueant_crm_erp.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RolePermissionSeederImpl implements BootstrapSeeder {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final BootstrapProperties properties;

    @Override
    public String module() {
        return BootstrapConstants.ROLE_PERMISSION_SEEDER;
    }

    @Override
    public int order() {
        return BootstrapConstants.ORDER_LEVEL_2;
    }

    @Override
    public boolean enabled() {
        return properties.isEnabled();
    }

    @Override
    @Transactional
    public SeederResult seed() {
        log.info("Starting Role-Permission Seeder...");
        MDC.put("module", module());
        long startTime = System.currentTimeMillis();
        int createdCount = 0;
        int skippedCount = 0;
        int totalExpected = 0;

        List<Role> allRoles = roleRepository.findAll();
        List<Permission> allPermissions = permissionRepository.findAll();

        if (allRoles.isEmpty() || allPermissions.isEmpty()) {
            log.warn("Roles or Permissions not found. Cannot seed mappings.");
            return SeederResult.builder()
                    .moduleName("Role-Permissions")
                    .status("FAILED: Roles or Permissions missing")
                    .build();
        }

        // Fetch existing mappings and convert to a Set for O(1) lookup
        List<RolePermission> existingMappings = rolePermissionRepository.findAll();
        Set<String> existingKeys = existingMappings.stream()
                .map(rp -> rp.getRole().getId() + "_" + rp.getPermission().getId())
                .collect(Collectors.toSet());

        List<RolePermission> mappingsToSave = new ArrayList<>();

        for (Role role : allRoles) {
            for (Permission p : allPermissions) {
                // SUPER_ADMIN gets all permissions
                // Other roles logic can be customized here based on enterprise hierarchy
                // For now, mapping everything to SUPER_ADMIN, and read-only to others as an example
                boolean shouldMap = false;

                if (role.getCode().equals(BootstrapConstants.ROLE_SUPER_ADMIN)) {
                    shouldMap = true;
                } else if (role.getCode().equals(BootstrapConstants.ROLE_SALES_COORDINATOR)) {
                    if (p.getCode().equals("MEETING_READ") || p.getCode().equals("MEETING_VERIFY")) {
                        shouldMap = true;
                    }
                } else if (role.getCode().equals(BootstrapConstants.ROLE_EMPLOYEE)) {
                    if (p.getCode().equals("LEAD_CREATE") || p.getCode().equals("LEAD_READ") || p.getCode().equals("LEAD_UPDATE") ||
                        p.getCode().equals("MEETING_CREATE") || p.getCode().equals("MEETING_READ") || p.getCode().equals("MEETING_UPDATE")) {
                        shouldMap = true;
                    } else if (p.getCode().contains("_READ")) {
                        shouldMap = true;
                    }
                } else if (p.getCode().contains("_READ")) {
                    shouldMap = true; // Simple logic: everyone can read
                }

                if (shouldMap) {
                    totalExpected++;
                    String key = role.getId() + "_" + p.getId();
                    boolean alreadyExists = existingKeys.contains(key);

                    if (!alreadyExists) {
                        mappingsToSave.add(RolePermission.builder().role(role).permission(p).build());
                        createdCount++;
                    } else {
                        skippedCount++;
                    }
                }
            }
        }

        if (!mappingsToSave.isEmpty()) {
            rolePermissionRepository.saveAll(mappingsToSave);
        }

        long executionTime = System.currentTimeMillis() - startTime;
        
        log.info("\n--------------------------------------------------\n" +
                 "[BOOTSTRAP]\n" +
                 "RunId : {}\n" +
                 "CorrelationId : {}\n" +
                 "Module : {}\n" +
                 "Thread : {}\n" +
                 "Execution Time : {} ms\n" +
                 "Inserted : {}\n" +
                 "Skipped : {}\n" +
                 "Failed : 0\n" +
                 "Status : SUCCESS\n" +
                 "--------------------------------------------------",
                 MDC.get("runId"), MDC.get("correlationId"), module(), Thread.currentThread().getName(), executionTime, createdCount, skippedCount);

        MDC.remove("module");

        return SeederResult.builder()
                .moduleName(module())
                .totalCount(totalExpected)
                .insertedCount(createdCount)
                .skippedCount(skippedCount)
                .executionTime(executionTime)
                .status("SUCCESS")
                .build();
    }
}
