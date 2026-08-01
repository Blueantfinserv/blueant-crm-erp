package com.blueant_crm_erp.bootstrap.seeder.impl;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;
import com.blueant_crm_erp.bootstrap.seeder.BootstrapSeeder;
import com.blueant_crm_erp.bootstrap.config.BootstrapProperties;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.permission.entity.Permission;
import com.blueant_crm_erp.permission.repository.PermissionRepository;
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
public class PermissionSeederImpl implements BootstrapSeeder {

    private final PermissionRepository permissionRepository;
    private final BootstrapProperties properties;

    @Override
    public String module() {
        return BootstrapConstants.PERMISSION_SEEDER;
    }

    @Override
    public int order() {
        return BootstrapConstants.ORDER_LEVEL_1;
    }

    @Override
    public boolean enabled() {
        return properties.isEnabled();
    }

    @Override
    @Transactional
    public SeederResult seed() {
        log.info("Starting Permission Seeder...");
        MDC.put("module", module());
        long startTime = System.currentTimeMillis();
        int createdCount = 0;
        int skippedCount = 0;

        String[] modules = {
            "USER", "ROLE", "PERMISSION", "DEPARTMENT", "DESIGNATION", 
            "TEAM", "LEAD", "CUSTOMER", "OPPORTUNITY", "SALE",
            "PRODUCT", "SERVICE", "INVOICE", "PAYMENT", "CONTRACT",
            "TICKET", "CAMPAIGN", "REPORT", "DASHBOARD", "SETTINGS",
            "AUDIT", "NOTIFICATION", "DOCUMENT", "TASK", "MEETING"
        };

        List<Permission> permissions = new ArrayList<>();
        for (String module : modules) {
            permissions.add(buildPermission(module + "_CREATE", "Create " + module, module));
            permissions.add(buildPermission(module + "_READ", "Read " + module, module));
            permissions.add(buildPermission(module + "_UPDATE", "Update " + module, module));
            permissions.add(buildPermission(module + "_DELETE", "Delete " + module, module));
        }

        List<Permission> permissionsToSave = new ArrayList<>();

        Set<String> existingCodes = permissionRepository.findAll().stream()
                .map(p -> p.getCode().toUpperCase())
                .collect(Collectors.toSet());

        for (Permission perm : permissions) {
            if (!existingCodes.contains(perm.getCode().toUpperCase())) {
                permissionsToSave.add(perm);
            } else {
                skippedCount++;
            }
        }

        if (!permissionsToSave.isEmpty()) {
            permissionRepository.saveAll(permissionsToSave);
            createdCount = permissionsToSave.size();
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
                .totalCount(permissions.size())
                .insertedCount(createdCount)
                .skippedCount(skippedCount)
                .executionTime(executionTime)
                .status("SUCCESS")
                .build();
    }

    private Permission buildPermission(String code, String name, String module) {
        return Permission.builder()
                .code(code)
                .name(name)
                .module(module)
                .status(Status.ACTIVE)
                .displayOrder(1)
                .description(name + " Permission")
                .remarks("System Bootstrapped")
                .build();
    }
}
