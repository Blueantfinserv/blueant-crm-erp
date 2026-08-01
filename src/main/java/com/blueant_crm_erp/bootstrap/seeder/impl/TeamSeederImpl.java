package com.blueant_crm_erp.bootstrap.seeder.impl;

import com.blueant_crm_erp.bootstrap.constant.BootstrapConstants;
import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;
import com.blueant_crm_erp.bootstrap.seeder.BootstrapSeeder;
import com.blueant_crm_erp.bootstrap.config.BootstrapProperties;
import com.blueant_crm_erp.common.enums.Status;
import com.blueant_crm_erp.user.entity.Department;
import com.blueant_crm_erp.user.entity.Team;
import com.blueant_crm_erp.user.repository.DepartmentRepository;
import com.blueant_crm_erp.user.repository.TeamRepository;
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
public class TeamSeederImpl implements BootstrapSeeder {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;
    private final BootstrapProperties properties;

    @Override
    public String module() {
        return BootstrapConstants.TEAM_SEEDER;
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
        log.info("Starting Team Seeder...");
        MDC.put("module", module());
        long startTime = System.currentTimeMillis();
        int createdCount = 0;
        int skippedCount = 0;

        Department salesDept = departmentRepository.findByCodeIgnoreCase(BootstrapConstants.DEPT_SALES).orElse(null);
        
        if (salesDept == null) {
            log.warn("Required departments not found. Cannot seed teams.");
            return SeederResult.builder()
                    .moduleName("Teams")
                    .status("FAILED: Dependency missing")
                    .build();
        }

        List<Team> teams = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            teams.add(buildTeam("Sales Team " + i, "ST" + i, salesDept));
        }

        List<Team> teamsToSave = new ArrayList<>();

        Set<String> existingCodes = teamRepository.findAll().stream()
                .map(t -> t.getTeamCode().toUpperCase())
                .collect(Collectors.toSet());

        for (Team team : teams) {
            if (!existingCodes.contains(team.getTeamCode().toUpperCase())) {
                teamsToSave.add(team);
            } else {
                skippedCount++;
            }
        }

        if (!teamsToSave.isEmpty()) {
            teamRepository.saveAll(teamsToSave);
            createdCount = teamsToSave.size();
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
                .totalCount(teams.size())
                .insertedCount(createdCount)
                .skippedCount(skippedCount)
                .executionTime(executionTime)
                .status("SUCCESS")
                .build();
    }

    private Team buildTeam(String name, String code, Department department) {
        return Team.builder()
                .teamName(name)
                .teamCode(code)
                .department(department)
                .status(Status.ACTIVE)
                .description(name + " Team")
                .build();
    }
}
