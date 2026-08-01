package com.blueant_crm_erp.bootstrap.service.impl;

import com.blueant_crm_erp.bootstrap.dto.request.BootstrapRequest;
import com.blueant_crm_erp.bootstrap.dto.response.BootstrapExecutionReport;
import com.blueant_crm_erp.bootstrap.dto.response.BootstrapStatusResponse;
import com.blueant_crm_erp.bootstrap.dto.response.SeederResult;
import com.blueant_crm_erp.bootstrap.event.*;
import com.blueant_crm_erp.bootstrap.seeder.BootstrapSeeder;
import com.blueant_crm_erp.bootstrap.service.BootstrapService;
import com.blueant_crm_erp.bootstrap.config.BootstrapProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * Bootstrap Service Implementation
 * =============================================================================
 *
 * Implements BootstrapService to orchestrate execution of all Seeders.
 *
 * Project : BlueAnt CRM ERP Platform
 * Module  : Bootstrap
 *
 * @author BlueAnt CRM ERP Team
 * @since 1.0.0
 * =============================================================================
 */
@Slf4j
@Service
public class BootstrapServiceImpl implements BootstrapService {

    private final List<BootstrapSeeder> seeders;
    private final com.blueant_crm_erp.bootstrap.validator.BootstrapValidator bootstrapValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final BootstrapProperties properties;
    private final MeterRegistry meterRegistry;
    private final Executor applicationTaskExecutor;
    private final Environment environment;

    public BootstrapServiceImpl(
            List<BootstrapSeeder> seeders,
            com.blueant_crm_erp.bootstrap.validator.BootstrapValidator bootstrapValidator,
            ApplicationEventPublisher eventPublisher,
            BootstrapProperties properties,
            MeterRegistry meterRegistry,
            @Qualifier("taskExecutor") Executor applicationTaskExecutor,
            Environment environment) {
        this.seeders = seeders;
        this.bootstrapValidator = bootstrapValidator;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.applicationTaskExecutor = applicationTaskExecutor;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void bootstrap() {
        bootstrap(BootstrapRequest.builder()
                .force(false)
                .masterData(true)
                .securityData(true)
                .users(true)
                .build());
    }

    @Override
    public BootstrapExecutionReport bootstrap(BootstrapRequest request) {
        if (!properties.isEnabled()) {
            log.warn("Bootstrap process is disabled in configuration.");
            return BootstrapExecutionReport.builder().warningDetails(List.of("Bootstrap is disabled.")).build();
        }

        String runId = UUID.randomUUID().toString();
        String tempCorrelationId = MDC.get("correlationId");
        final String correlationId = tempCorrelationId != null ? tempCorrelationId : UUID.randomUUID().toString();
        
        MDC.put("runId", runId);
        MDC.put("correlationId", correlationId);

        log.info("Starting Enterprise Bootstrap Process. RunId: {}", runId);
        eventPublisher.publishEvent(new BootstrapStartedEvent(this, runId, correlationId));

        long startTime = System.currentTimeMillis();
        Timer.Sample timerSample = Timer.start(meterRegistry);

        List<String> completedModules = Collections.synchronizedList(new ArrayList<>());
        List<String> skippedModules = Collections.synchronizedList(new ArrayList<>());
        Map<String, String> failureDetails = Collections.synchronizedMap(new HashMap<>());
        
        // Group seeders by order and filter enabled ones
        Map<Integer, List<BootstrapSeeder>> groupedSeeders = seeders.stream()
                .filter(BootstrapSeeder::enabled)
                .collect(Collectors.groupingBy(BootstrapSeeder::order, TreeMap::new, Collectors.toList()));

        int totalInserted = 0;
        int totalSkippedRecords = 0;

        try {
            for (Map.Entry<Integer, List<BootstrapSeeder>> entry : groupedSeeders.entrySet()) {
                int order = entry.getKey();
                List<BootstrapSeeder> orderSeeders = entry.getValue();
                
                log.info("Executing Seeders for Order Level: {}", order);

                if (properties.isParallel()) {
                    List<CompletableFuture<SeederResult>> futures = orderSeeders.stream()
                            .map(seeder -> executeSeederAsync(seeder, runId, correlationId))
                            .collect(Collectors.toList());

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                    for (CompletableFuture<SeederResult> future : futures) {
                        try {
                            SeederResult result = future.get();
                            totalInserted += result.getInsertedCount();
                            totalSkippedRecords += result.getSkippedCount();
                            handleSeederResult(result, completedModules, skippedModules, failureDetails, runId, correlationId);
                        } catch (Exception e) {
                            log.error("Error retrieving seeder result", e);
                        }
                    }
                } else {
                    for (BootstrapSeeder seeder : orderSeeders) {
                        SeederResult result = executeSeederSync(seeder, runId, correlationId);
                        totalInserted += result.getInsertedCount();
                        totalSkippedRecords += result.getSkippedCount();
                        handleSeederResult(result, completedModules, skippedModules, failureDetails, runId, correlationId);
                    }
                }
            }
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            timerSample.stop(meterRegistry.timer("bootstrap.duration"));
            log.info("Enterprise Bootstrap Process Completed in {} ms", executionTime);

            eventPublisher.publishEvent(new BootstrapCompletedEvent(this, runId, correlationId, executionTime));
            MDC.clear();
        }

        return BootstrapExecutionReport.builder()
                .totalModules(seeders.size())
                .executedModules(completedModules.size() + skippedModules.size() + failureDetails.size())
                .successfulModules(completedModules.size())
                .skippedModules(skippedModules.size())
                .failedModules(failureDetails.size())
                .totalRecordsInserted(totalInserted)
                .totalRecordsSkipped(totalSkippedRecords)
                .totalRecordsFailed(0)
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .completedModules(completedModules)
                .failureDetails(failureDetails)
                .timestamp(LocalDateTime.now())
                .javaVersion(System.getProperty("java.version"))
                .springBootVersion(environment.getProperty("spring.boot.version", "3.3.0"))
                .applicationVersion("1.0.0")
                .databaseVersion("PostgreSQL/MySQL")
                .build();
    }

    private CompletableFuture<SeederResult> executeSeederAsync(BootstrapSeeder seeder, String runId, String correlationId) {
        return CompletableFuture.supplyAsync(() -> executeSeederSync(seeder, runId, correlationId), applicationTaskExecutor);
    }

    private SeederResult executeSeederSync(BootstrapSeeder seeder, String runId, String correlationId) {
        MDC.put("runId", runId);
        MDC.put("correlationId", correlationId);
        eventPublisher.publishEvent(new SeederStartedEvent(this, runId, correlationId, seeder.module()));
        try {
            return seeder.seed();
        } catch (Exception e) {
            log.error("Seeder failed for module: {}", seeder.module(), e);
            eventPublisher.publishEvent(new SeederFailedEvent(this, runId, correlationId, seeder.module(), e));
            if (!properties.isContinueOnFailure()) {
                throw new RuntimeException("Bootstrap failed at module: " + seeder.module(), e);
            }
            return SeederResult.builder().moduleName(seeder.module()).status("FAILED").build();
        } finally {
            MDC.clear();
        }
    }

    private void handleSeederResult(SeederResult result, List<String> completedModules, List<String> skippedModules, Map<String, String> failureDetails, String runId, String correlationId) {
        eventPublisher.publishEvent(new SeederCompletedEvent(this, runId, correlationId, result.getModuleName(), result));
        if ("SUCCESS".equals(result.getStatus())) {
            completedModules.add(result.getModuleName());
            meterRegistry.counter("bootstrap.insert.count", "module", result.getModuleName()).increment(result.getInsertedCount());
            meterRegistry.counter("bootstrap.skip.count", "module", result.getModuleName()).increment(result.getSkippedCount());
        } else {
            failureDetails.put(result.getModuleName(), result.getStatus());
            skippedModules.add(result.getModuleName());
            meterRegistry.counter("bootstrap.failure.count", "module", result.getModuleName()).increment();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BootstrapStatusResponse getBootstrapStatus() {
        log.info("Retrieving Bootstrap Status...");
        
        List<String> completed = new ArrayList<>();
        List<String> pending = new ArrayList<>();
        
        if (bootstrapValidator.hasDepartments()) completed.add("Departments"); else pending.add("Departments");
        if (bootstrapValidator.hasDesignations()) completed.add("Designations"); else pending.add("Designations");
        if (bootstrapValidator.hasTeams()) completed.add("Teams"); else pending.add("Teams");
        if (bootstrapValidator.hasRoles()) completed.add("Roles"); else pending.add("Roles");
        if (bootstrapValidator.hasPermissions()) completed.add("Permissions"); else pending.add("Permissions");
        if (bootstrapValidator.hasSuperAdmin()) completed.add("Users"); else pending.add("Users");
        
        boolean isCompleted = bootstrapValidator.isBootstrapCompleted();

        return BootstrapStatusResponse.builder()
                .bootstrapCompleted(isCompleted)
                .databaseInitialized(isCompleted)
                .superAdminCreated(bootstrapValidator.hasSuperAdmin())
                .totalModules(6)
                .completedModules(completed)
                .pendingModules(pending)
                .lastBootstrapTime(LocalDateTime.now())
                .message(isCompleted ? "System is already bootstrapped." : "System bootstrap is pending or incomplete.")
                .build();
    }
}
