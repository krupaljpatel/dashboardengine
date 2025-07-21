package com.dashboardengine.consumer.api;

import com.dashboardengine.consumer.api.dto.FileSystemConfigDto;
import com.dashboardengine.consumer.api.dto.FileSystemStatusDto;
import com.dashboardengine.consumer.filesystem.FileSystemConfigurationManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/filesystem")
@Tag(name = "File System Configuration", description = "Manage file system consumer configurations")
public class FileSystemConfigController {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemConfigController.class);
    
    private final FileSystemConfigurationManager configManager;

    public FileSystemConfigController(FileSystemConfigurationManager configManager) {
        this.configManager = configManager;
    }

    @Operation(summary = "Get all file system configurations")
    @GetMapping("/configs")
    public ResponseEntity<Map<String, FileSystemConfigDto>> getAllConfigurations() {
        Map<String, FileSystemConfigDto> configs = configManager.getAllConfigurations();
        return ResponseEntity.ok(configs);
    }

    @Operation(summary = "Get specific file system configuration")
    @GetMapping("/configs/{configName}")
    public ResponseEntity<FileSystemConfigDto> getConfiguration(
            @Parameter(description = "Configuration name") @PathVariable String configName) {
        
        return configManager.getConfiguration(configName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new file system configuration")
    @PostMapping("/configs/{configName}")
    public ResponseEntity<FileSystemConfigDto> createConfiguration(
            @Parameter(description = "Configuration name") @PathVariable String configName,
            @Valid @RequestBody FileSystemConfigDto config) {
        
        try {
            FileSystemConfigDto created = configManager.createConfiguration(configName, config);
            logger.info("Created file system configuration: {}", configName);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create configuration {}: {}", configName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update existing file system configuration")
    @PutMapping("/configs/{configName}")
    public ResponseEntity<FileSystemConfigDto> updateConfiguration(
            @Parameter(description = "Configuration name") @PathVariable String configName,
            @Valid @RequestBody FileSystemConfigDto config) {
        
        try {
            FileSystemConfigDto updated = configManager.updateConfiguration(configName, config);
            logger.info("Updated file system configuration: {}", configName);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update configuration {}: {}", configName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete file system configuration")
    @DeleteMapping("/configs/{configName}")
    public ResponseEntity<Void> deleteConfiguration(
            @Parameter(description = "Configuration name") @PathVariable String configName) {
        
        if (configManager.deleteConfiguration(configName)) {
            logger.info("Deleted file system configuration: {}", configName);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Start file system consumer for specific configuration")
    @PostMapping("/configs/{configName}/start")
    public ResponseEntity<FileSystemStatusDto> startConsumer(
            @Parameter(description = "Configuration name") @PathVariable String configName) {
        
        try {
            FileSystemStatusDto status = configManager.startConsumer(configName);
            logger.info("Started file system consumer: {}", configName);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to start consumer {}: {}", configName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Stop file system consumer for specific configuration")
    @PostMapping("/configs/{configName}/stop")
    public ResponseEntity<FileSystemStatusDto> stopConsumer(
            @Parameter(description = "Configuration name") @PathVariable String configName) {
        
        try {
            FileSystemStatusDto status = configManager.stopConsumer(configName);
            logger.info("Stopped file system consumer: {}", configName);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to stop consumer {}: {}", configName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get status of all file system consumers")
    @GetMapping("/status")
    public ResponseEntity<Map<String, FileSystemStatusDto>> getAllStatus() {
        Map<String, FileSystemStatusDto> statuses = configManager.getAllStatus();
        return ResponseEntity.ok(statuses);
    }

    @Operation(summary = "Get status of specific file system consumer")
    @GetMapping("/status/{configName}")
    public ResponseEntity<FileSystemStatusDto> getConsumerStatus(
            @Parameter(description = "Configuration name") @PathVariable String configName) {
        
        return configManager.getConsumerStatus(configName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Restart file system consumer")
    @PostMapping("/configs/{configName}/restart")
    public ResponseEntity<FileSystemStatusDto> restartConsumer(
            @Parameter(description = "Configuration name") @PathVariable String configName) {
        
        try {
            FileSystemStatusDto status = configManager.restartConsumer(configName);
            logger.info("Restarted file system consumer: {}", configName);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to restart consumer {}: {}", configName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}