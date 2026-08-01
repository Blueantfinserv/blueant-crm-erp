package com.blueant_crm_erp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Thread pool for @Async methods
     *
     * Why we need this:
     * When a lead is converted, multiple things need to happen simultaneously:
     *   1. Notification → SM ko congratulations
     *   2. Onboarding → checklist start
     *   3. Incentive → calculation queue
     *   4. AuditLog → record entry
     *
     * Without async: all 4 run one after another, user waits
     * With async: all 4 run in parallel background threads, user gets response instantly
     */
    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);        // always-ready threads
        executor.setMaxPoolSize(15);        // max threads under load
        executor.setQueueCapacity(100);     // queued tasks before rejecting
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("crm-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        return executor;
    }

    /**
     * Handles exceptions thrown inside @Async methods
     * Without this, exceptions silently disappear
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> log.error("Async method '{}' threw exception: {}",
                method.getName(), throwable.getMessage(), throwable);
    }
}
