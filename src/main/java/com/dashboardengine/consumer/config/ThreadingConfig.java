package com.dashboardengine.consumer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadingConfig {

    private final ApplicationProperties applicationProperties;

    public ThreadingConfig(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean(name = "processingExecutor")
    public Executor processingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        ApplicationProperties.Threading config = applicationProperties.getThreading();
        
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds());
        executor.setThreadNamePrefix("Processing-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        return executor;
    }
}