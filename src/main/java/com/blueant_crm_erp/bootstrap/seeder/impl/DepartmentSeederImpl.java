package com.blueant_crm_erp.bootstrap.seeder.impl;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;
import com.blueant_crm_erp.bootstrap.seeder.BootstrapSeeder;
import com.blueant_crm_erp.bootstrap.config.BootstrapProperties;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
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
public class DepartmentSeederImpl implements BootstrapSeeder {

    private final DepartmentRepository departmentRepository;
    private final BootstrapProperties properties;

    @Override
    public String module() {
        return BootstrapConstants.DEPARTMENT_SEEDER;
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
        log.info("Starting Department Seeder...");
        MDC.put("module", module());
        long startTime = System.currentTimeMillis();
        int createdCount = 0;
        int skippedCount = 0;

        List<Department> defaultDepartments = List.of(
            buildDepartment("Sales", BootstrapConstants.DEPT_SALES, 1),
            buildDepartment("CRM", BootstrapConstants.DEPT_CRM, 2),
            buildDepartment("Operations", BootstrapConstants.DEPT_OPS, 3),
            buildDepartment("HR", BootstrapConstants.DEPT_HR, 4),
            buildDepartment("Accounts", BootstrapConstants.DEPT_ACC, 5),
            buildDepartment("Helpdesk", BootstrapConstants.DEPT_HELP, 6)
        );

        List<Department> departmentsToSave = new ArrayList<>();

        Set<String> existingCodes = departmentRepository.findAll().stream()
                .map(d -> d.getCode().toUpperCase())
                .collect(Collectors.toSet());

        for (Department dept : defaultDepartments) {
            if (!existingCodes.contains(dept.getCode().toUpperCase())) {
                departmentsToSave.add(dept);
            } else {
                skippedCount++;
            }
        }

        if (!departmentsToSave.isEmpty()) {
            departmentRepository.saveAll(departmentsToSave);
            createdCount = departmentsToSave.size();
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
                .totalCount(defaultDepartments.size())
                .insertedCount(createdCount)
                .skippedCount(skippedCount)
                .executionTime(executionTime)
                .status("SUCCESS")
                .build();
    }

    private Department buildDepartment(String name, String code, Integer displayOrder) {
        return Department.builder()
                .name(name)
                .code(code)
                .displayOrder(displayOrder)
                .status(Status.ACTIVE)
                .description(name + " Department")
                .remarks("System Bootstrapped")
                .build();
    }
}
