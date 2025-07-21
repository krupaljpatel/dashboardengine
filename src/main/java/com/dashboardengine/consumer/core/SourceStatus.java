package com.dashboardengine.consumer.core;

import java.time.LocalDateTime;

public record SourceStatus(
    String sourceType,
    boolean healthy,
    String message,
    LocalDateTime lastCheck,
    long processedCount,
    long errorCount
) {}