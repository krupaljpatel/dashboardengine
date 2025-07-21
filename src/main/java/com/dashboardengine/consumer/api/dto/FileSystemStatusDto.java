package com.dashboardengine.consumer.api.dto;

import java.time.LocalDateTime;

public class FileSystemStatusDto {
    
    private String configName;
    private String path;
    private boolean running;
    private boolean healthy;
    private String status;
    private LocalDateTime lastActivity;
    private long processedFiles;
    private long errorCount;
    private long currentQueueSize;
    private int activeThreads;
    private double averageProcessingTimeMs;
    
    public FileSystemStatusDto() {}
    
    public FileSystemStatusDto(String configName, String path, boolean running, 
                              boolean healthy, String status, LocalDateTime lastActivity,
                              long processedFiles, long errorCount, long currentQueueSize,
                              int activeThreads, double averageProcessingTimeMs) {
        this.configName = configName;
        this.path = path;
        this.running = running;
        this.healthy = healthy;
        this.status = status;
        this.lastActivity = lastActivity;
        this.processedFiles = processedFiles;
        this.errorCount = errorCount;
        this.currentQueueSize = currentQueueSize;
        this.activeThreads = activeThreads;
        this.averageProcessingTimeMs = averageProcessingTimeMs;
    }

    // Getters and Setters
    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public long getProcessedFiles() {
        return processedFiles;
    }

    public void setProcessedFiles(long processedFiles) {
        this.processedFiles = processedFiles;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public long getCurrentQueueSize() {
        return currentQueueSize;
    }

    public void setCurrentQueueSize(long currentQueueSize) {
        this.currentQueueSize = currentQueueSize;
    }

    public int getActiveThreads() {
        return activeThreads;
    }

    public void setActiveThreads(int activeThreads) {
        this.activeThreads = activeThreads;
    }

    public double getAverageProcessingTimeMs() {
        return averageProcessingTimeMs;
    }

    public void setAverageProcessingTimeMs(double averageProcessingTimeMs) {
        this.averageProcessingTimeMs = averageProcessingTimeMs;
    }
}