package com.dashboardengine.consumer.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class FileSystemConfigDto {
    
    @NotBlank(message = "Path is required")
    private String path;
    
    private List<String> patterns;
    
    @Positive(message = "Poll interval must be positive")
    private int pollIntervalMs = 5000;
    
    private String archiveDir;
    
    private boolean deleteAfterProcess = false;
    
    private boolean enabled = true;
    
    private int maxConcurrentFiles = 10;
    
    private long maxFileSizeBytes = 100 * 1024 * 1024; // 100MB default
    
    public FileSystemConfigDto() {}
    
    public FileSystemConfigDto(String path, List<String> patterns, int pollIntervalMs, 
                              String archiveDir, boolean deleteAfterProcess, boolean enabled,
                              int maxConcurrentFiles, long maxFileSizeBytes) {
        this.path = path;
        this.patterns = patterns;
        this.pollIntervalMs = pollIntervalMs;
        this.archiveDir = archiveDir;
        this.deleteAfterProcess = deleteAfterProcess;
        this.enabled = enabled;
        this.maxConcurrentFiles = maxConcurrentFiles;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    // Getters and Setters
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    public int getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(int pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public String getArchiveDir() {
        return archiveDir;
    }

    public void setArchiveDir(String archiveDir) {
        this.archiveDir = archiveDir;
    }

    public boolean isDeleteAfterProcess() {
        return deleteAfterProcess;
    }

    public void setDeleteAfterProcess(boolean deleteAfterProcess) {
        this.deleteAfterProcess = deleteAfterProcess;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxConcurrentFiles() {
        return maxConcurrentFiles;
    }

    public void setMaxConcurrentFiles(int maxConcurrentFiles) {
        this.maxConcurrentFiles = maxConcurrentFiles;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }
}