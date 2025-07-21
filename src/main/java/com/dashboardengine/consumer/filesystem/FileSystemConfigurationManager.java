package com.dashboardengine.consumer.filesystem;

import com.dashboardengine.consumer.api.dto.FileSystemConfigDto;
import com.dashboardengine.consumer.api.dto.FileSystemStatusDto;
import com.dashboardengine.consumer.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileSystemConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemConfigurationManager.class);
    
    private final Map<String, FileSystemConfigDto> configurations = new ConcurrentHashMap<>();
    private final Map<String, IsolatedFileSystemConsumer> consumers = new ConcurrentHashMap<>();
    private final IsolatedFileSystemConsumerFactory consumerFactory;
    
    public FileSystemConfigurationManager(IsolatedFileSystemConsumerFactory consumerFactory,
                                         ApplicationProperties properties) {
        this.consumerFactory = consumerFactory;
        
        // Initialize with existing configurations from properties
        initializeFromProperties(properties);
    }

    private void initializeFromProperties(ApplicationProperties properties) {
        if (properties.getFilesystem() != null) {
            properties.getFilesystem().forEach((name, config) -> {
                FileSystemConfigDto dto = convertToDto(config);
                configurations.put(name, dto);
                
                // Auto-start enabled configurations
                if (dto.isEnabled()) {
                    try {
                        startConsumer(name);
                    } catch (Exception e) {
                        logger.error("Failed to auto-start filesystem consumer: {}", name, e);
                    }
                }
            });
        }
    }

    public Map<String, FileSystemConfigDto> getAllConfigurations() {
        return Map.copyOf(configurations);
    }

    public Optional<FileSystemConfigDto> getConfiguration(String configName) {
        return Optional.ofNullable(configurations.get(configName));
    }

    public FileSystemConfigDto createConfiguration(String configName, FileSystemConfigDto config) {
        validateConfiguration(config);
        
        if (configurations.containsKey(configName)) {
            throw new IllegalArgumentException("Configuration already exists: " + configName);
        }
        
        configurations.put(configName, config);
        logger.info("Created filesystem configuration: {}", configName);
        
        return config;
    }

    public FileSystemConfigDto updateConfiguration(String configName, FileSystemConfigDto config) {
        validateConfiguration(config);
        
        if (!configurations.containsKey(configName)) {
            throw new IllegalArgumentException("Configuration not found: " + configName);
        }
        
        // Stop existing consumer if running
        if (consumers.containsKey(configName)) {
            stopConsumer(configName);
        }
        
        configurations.put(configName, config);
        logger.info("Updated filesystem configuration: {}", configName);
        
        // Restart if enabled
        if (config.isEnabled()) {
            startConsumer(configName);
        }
        
        return config;
    }

    public boolean deleteConfiguration(String configName) {
        if (!configurations.containsKey(configName)) {
            return false;
        }
        
        // Stop consumer if running
        if (consumers.containsKey(configName)) {
            stopConsumer(configName);
        }
        
        configurations.remove(configName);
        logger.info("Deleted filesystem configuration: {}", configName);
        
        return true;
    }

    public FileSystemStatusDto startConsumer(String configName) {
        FileSystemConfigDto config = configurations.get(configName);
        if (config == null) {
            throw new IllegalArgumentException("Configuration not found: " + configName);
        }
        
        if (consumers.containsKey(configName)) {
            throw new IllegalArgumentException("Consumer already running: " + configName);
        }
        
        try {
            IsolatedFileSystemConsumer consumer = consumerFactory.createConsumer(configName, config);
            consumer.start();
            consumers.put(configName, consumer);
            
            logger.info("Started filesystem consumer: {}", configName);
            return getConsumerStatusInternal(configName, consumer);
            
        } catch (Exception e) {
            logger.error("Failed to start filesystem consumer: {}", configName, e);
            throw new IllegalArgumentException("Failed to start consumer: " + e.getMessage());
        }
    }

    public FileSystemStatusDto stopConsumer(String configName) {
        IsolatedFileSystemConsumer consumer = consumers.remove(configName);
        if (consumer == null) {
            throw new IllegalArgumentException("Consumer not running: " + configName);
        }
        
        try {
            consumer.stop();
            logger.info("Stopped filesystem consumer: {}", configName);
            return createStoppedStatus(configName);
            
        } catch (Exception e) {
            logger.error("Error stopping filesystem consumer: {}", configName, e);
            throw new IllegalArgumentException("Failed to stop consumer: " + e.getMessage());
        }
    }

    public FileSystemStatusDto restartConsumer(String configName) {
        if (consumers.containsKey(configName)) {
            stopConsumer(configName);
        }
        return startConsumer(configName);
    }

    public Map<String, FileSystemStatusDto> getAllStatus() {
        Map<String, FileSystemStatusDto> statuses = new ConcurrentHashMap<>();
        
        configurations.forEach((name, config) -> {
            IsolatedFileSystemConsumer consumer = consumers.get(name);
            if (consumer != null) {
                statuses.put(name, getConsumerStatusInternal(name, consumer));
            } else {
                statuses.put(name, createStoppedStatus(name));
            }
        });
        
        return statuses;
    }

    public Optional<FileSystemStatusDto> getConsumerStatus(String configName) {
        if (!configurations.containsKey(configName)) {
            return Optional.empty();
        }
        
        IsolatedFileSystemConsumer consumer = consumers.get(configName);
        if (consumer != null) {
            return Optional.of(getConsumerStatusInternal(configName, consumer));
        } else {
            return Optional.of(createStoppedStatus(configName));
        }
    }

    private FileSystemStatusDto getConsumerStatusInternal(String configName, IsolatedFileSystemConsumer consumer) {
        FileSystemConfigDto config = configurations.get(configName);
        var consumerStatus = consumer.getStatus();
        
        return new FileSystemStatusDto(
            configName,
            config.getPath(),
            consumer.isRunning(),
            consumerStatus.healthy(),
            consumerStatus.message(),
            consumerStatus.lastCheck(),
            consumerStatus.processedCount(),
            consumerStatus.errorCount(),
            consumer.getCurrentQueueSize(),
            consumer.getActiveThreadCount(),
            consumer.getAverageProcessingTime()
        );
    }

    private FileSystemStatusDto createStoppedStatus(String configName) {
        FileSystemConfigDto config = configurations.get(configName);
        return new FileSystemStatusDto(
            configName,
            config.getPath(),
            false,
            false,
            "Stopped",
            LocalDateTime.now(),
            0,
            0,
            0,
            0,
            0.0
        );
    }

    private void validateConfiguration(FileSystemConfigDto config) {
        if (config.getPath() == null || config.getPath().trim().isEmpty()) {
            throw new IllegalArgumentException("Path is required");
        }
        
        if (!Files.exists(Paths.get(config.getPath()))) {
            try {
                Files.createDirectories(Paths.get(config.getPath()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot create directory: " + config.getPath());
            }
        }
        
        if (config.getPollIntervalMs() < 1000) {
            throw new IllegalArgumentException("Poll interval must be at least 1000ms");
        }
        
        if (config.getMaxConcurrentFiles() < 1) {
            throw new IllegalArgumentException("Max concurrent files must be at least 1");
        }
    }

    private FileSystemConfigDto convertToDto(ApplicationProperties.FileSystemConfig config) {
        return new FileSystemConfigDto(
            config.getPath(),
            config.getPatterns(),
            config.getPollIntervalMs(),
            config.getArchiveDir(),
            config.isDeleteAfterProcess(),
            true, // enabled by default
            10,   // default max concurrent files
            100 * 1024 * 1024 // default max file size 100MB
        );
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down all filesystem consumers");
        consumers.values().forEach(consumer -> {
            try {
                consumer.stop();
            } catch (Exception e) {
                logger.error("Error stopping consumer during shutdown", e);
            }
        });
        consumers.clear();
    }
}