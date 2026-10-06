package com.blueant_crm_erp.bootstrap.seeder.impl;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;
import com.blueant_crm_erp.bootstrap.seeder.BootstrapSeeder;
import com.blueant_crm_erp.bootstrap.config.BootstrapProperties;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.DesignationRepository;
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
public class DesignationSeederImpl implements BootstrapSeeder {

    private final DesignationRepository designationRepository;
    private final BootstrapProperties properties;

    @Override
    public String module() {
        return BootstrapConstants.DESIGNATION_SEEDER;
    }

    @Override
    public int order() {
        return BootstrapConstants.ORDER_LEVEL_2;
    }

    @Override
    public boolean enabled() {
        return properties.isEnabled();
    }
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public SeederResult seed() {
        log.info("Starting Designation Seeder...");
        MDC.put("module", module());
        long startTime = System.currentTimeMillis();
        int createdCount = 0;
        int skippedCount = 0;

        Department salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES).orElse(null);
        Department hrDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_HR).orElse(null);
        Department opsDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_OPS).orElse(null);
        
        if (salesDept == null || hrDept == null || opsDept == null) {
            log.warn("Required departments not found. Cannot seed designations.");
            return SeederResult.builder()
                    .moduleName("Designations")
                    .status("FAILED: Dependencies missing")
                    .build();
        }

        List<Designation> designations = new ArrayList<>();
        
        // Sales
        designations.add(buildDesignation("Business Head", BootstrapConstants.DESIG_BH, 1, salesDept));
        designations.add(buildDesignation("Team Leader", BootstrapConstants.DESIG_TL, 3, salesDept));
        designations.add(buildDesignation("Sales Coordinator", BootstrapConstants.DESIG_SC, 4, salesDept));
        designations.add(buildDesignation("Sales Manager", BootstrapConstants.DESIG_SM, 2, salesDept));
        designations.add(buildDesignation("Relationship Manager", BootstrapConstants.DESIG_RM, 4, salesDept));

        // Operations
        designations.add(buildDesignation("PC Coordinator", BootstrapConstants.DESIG_PC, 4, opsDept));

        List<Designation> designationsToSave = new ArrayList<>();

        Set<String> existingCodes = designationRepository.findAll().stream()
                .map(d -> d.getCode().toUpperCase())
                .collect(Collectors.toSet());

        for (Designation desig : designations) {
            if (!existingCodes.contains(desig.getCode().toUpperCase())) {
                designationsToSave.add(desig);
            } else {
                skippedCount++;
            }
        }

        if (!designationsToSave.isEmpty()) {
            designationRepository.saveAll(designationsToSave);
            createdCount = designationsToSave.size();
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
                .totalCount(designations.size())
                .insertedCount(createdCount)
                .skippedCount(skippedCount)
                .executionTime(executionTime)
                .status("SUCCESS")
                .build();
    }

    private Designation buildDesignation(String name, String code, Integer level, Department department) {
        return Designation.builder()
                .name(name)
                .code(code)
                .hierarchyLevel(level)
                .department(department)
                .status(Status.ACTIVE)
                .description(name + " Designation")
                .remarks("System Bootstrapped")
                .build();
    }
}
