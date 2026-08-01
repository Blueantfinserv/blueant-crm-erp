package com.blueant_crm_erp.bootstrap.seeder.impl;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;
import com.blueant_crm_erp.bootstrap.seeder.BootstrapSeeder;
import com.blueant_crm_erp.bootstrap.config.BootstrapProperties;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.role.entity.Role;
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
public class RoleSeederImpl implements BootstrapSeeder {

    private final RoleRepository roleRepository;
    private final BootstrapProperties properties;

    @Override
    public String module() {
        return BootstrapConstants.ROLE_SEEDER;
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
        log.info("Starting Role Seeder...");
        MDC.put("module", module());
        long startTime = System.currentTimeMillis();
        int createdCount = 0;
        int skippedCount = 0;

        List<Role> roles = List.of(
            buildRole("Super Admin", BootstrapConstants.ROLE_SUPER_ADMIN, 1, true, false),
            buildRole("Admin", BootstrapConstants.ROLE_ADMIN, 2, true, false),
            buildRole("Business Head", BootstrapConstants.ROLE_BUSINESS_HEAD, 3, true, false),
            buildRole("Sales Manager", BootstrapConstants.ROLE_SALES_MANAGER, 4, true, false),
            buildRole("Team Leader", BootstrapConstants.ROLE_TEAM_LEADER, 5, true, false),
            buildRole("Relationship Manager", BootstrapConstants.ROLE_RELATIONSHIP_MANAGER, 6, true, false),
            buildRole("Employee", BootstrapConstants.ROLE_EMPLOYEE, 7, true, true)
        );

        List<Role> rolesToSave = new ArrayList<>();

        Set<String> existingCodes = roleRepository.findAll().stream()
                .map(r -> r.getCode().toUpperCase())
                .collect(Collectors.toSet());

        for (Role role : roles) {
            if (!existingCodes.contains(role.getCode().toUpperCase())) {
                rolesToSave.add(role);
            } else {
                skippedCount++;
            }
        }

        if (!rolesToSave.isEmpty()) {
            roleRepository.saveAll(rolesToSave);
            createdCount = rolesToSave.size();
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
                .totalCount(roles.size())
                .insertedCount(createdCount)
                .skippedCount(skippedCount)
                .executionTime(executionTime)
                .status("SUCCESS")
                .build();
    }

    private Role buildRole(String name, String code, Integer order, Boolean isSystem, Boolean isDefault) {
        return Role.builder()
                .name(name)
                .code(code)
                .displayOrder(order)
                .systemRole(isSystem)
                .defaultRole(isDefault)
                .status(Status.ACTIVE)
                .description(name + " Role")
                .remarks("System Bootstrapped")
                .build();
    }
}
