package com.blueant_crm_erp.bootstrap.seeder.impl;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;
import com.blueant_crm_erp.bootstrap.seeder.BootstrapSeeder;
import com.blueant_crm_erp.bootstrap.config.BootstrapProperties;
import com.blueant_crm_erp.common.enums.Gender;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.role.entity.Role;
import com.blueant_crm_erp.role.repository.RoleRepository;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Designation;
import com.blueant_crm_erp.user.entity.Team;
import com.blueant_crm_erp.user.entity.User;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.DesignationRepository;
import com.blueant_crm_erp.user.repository.TeamRepository;
import com.blueant_crm_erp.user.repository.UserRepository;
import com.blueant_crm_erp.util.auth.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSeederImpl implements BootstrapSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final TeamRepository teamRepository;
    private final BootstrapProperties properties;

    @Override
    public String module() {
        return BootstrapConstants.USER_SEEDER;
    }

    @Override
    public int order() {
        return BootstrapConstants.ORDER_LEVEL_3;
    }

    @Override
    public boolean enabled() {
        return properties.isEnabled();
    }

    @Override
    @Transactional
    public SeederResult seed() {
        log.info("Starting User Seeder...");
        MDC.put("module", module());
        long startTime = System.currentTimeMillis();
        int createdCount = 0;
        int skippedCount = 0;
        int totalExpected = 1; // Only Super Admin

        if (userRepository.existsByEmployeeCodeIgnoreCase(BootstrapConstants.SUPER_ADMIN_EMPLOYEE_CODE)) {
            skippedCount++;
            log.info("Super Admin already exists. Skipping user seeding...");
        } else {
            log.info("Seeding default Super Admin user...");

            // Optimize: Load everything into maps to avoid findBy calls
            Map<String, Role> roleMap = roleRepository.findAll().stream()
                    .collect(Collectors.toMap(r -> r.getCode().toUpperCase(), r -> r, (r1, r2) -> r1));
            Map<String, Department> deptMap = departmentRepository.findAll().stream()
                    .collect(Collectors.toMap(d -> d.getCode().toUpperCase(), d -> d, (d1, d2) -> d1));
            Map<String, Designation> desigMap = designationRepository.findAll().stream()
                    .collect(Collectors.toMap(d -> d.getCode().toUpperCase(), d -> d, (d1, d2) -> d1));
            Map<String, Team> teamMap = teamRepository.findAll().stream()
                    .collect(Collectors.toMap(t -> t.getTeamCode().toUpperCase(), t -> t, (t1, t2) -> t1));

            Role superAdminRole = roleMap.get(BootstrapConstants.ROLE_SUPER_ADMIN.toUpperCase());
            Department dept = deptMap.get(BootstrapConstants.DEPT_SALES.toUpperCase());
            Designation desig = desigMap.get(BootstrapConstants.DESIG_BH.toUpperCase());
            Team team = teamMap.get(BootstrapConstants.TEAM_ST1.toUpperCase());

            if (superAdminRole == null || dept == null || desig == null || team == null) {
                log.warn("Required dependencies not found. Cannot seed Super Admin.");
                MDC.remove("module");
                return SeederResult.builder()
                        .moduleName(module())
                        .status("FAILED: Dependencies missing")
                        .build();
            }

            User superAdmin = User.builder()
                    .employeeCode(BootstrapConstants.SUPER_ADMIN_EMPLOYEE_CODE)
                    .firstName(BootstrapConstants.SUPER_ADMIN_FIRST_NAME)
                    .lastName(BootstrapConstants.SUPER_ADMIN_LAST_NAME)
                    .email(BootstrapConstants.SUPER_ADMIN_EMAIL)
                    .mobileNumber(BootstrapConstants.SUPER_ADMIN_MOBILE)
                    .password(PasswordUtil.encode(BootstrapConstants.SUPER_ADMIN_PASSWORD))
                    .gender(Gender.MALE)
                    .joiningDate(LocalDate.now())
                    .status(Status.ACTIVE)
                    .role(superAdminRole)
                    .department(dept)
                    .designation(desig)
                    .team(team)
                    .accountEnabled(true)
                    .accountLocked(false)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .emailVerified(true)
                    .mobileVerified(true)
                    .firstLogin(false)
                    .remarks("System Bootstrapped Super Admin")
                    .build();

            userRepository.save(superAdmin);
            createdCount++;
            log.info("Successfully seeded Super Admin user.");
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
