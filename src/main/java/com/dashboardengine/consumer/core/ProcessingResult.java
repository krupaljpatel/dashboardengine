package com.dashboardengine.consumer.core;

import java.time.LocalDateTime;

public record ProcessingResult(
    boolean success,
    String message,
    LocalDateTime processedAt,
    long processingTimeMs,
    String outputLocation
) {
    
    public static ProcessingResult success(long processingTimeMs, String outputLocation) {
        return new ProcessingResult(true, "Processing completed successfully", 
                                    LocalDateTime.now(), processingTimeMs, outputLocation);
    }
    
    public static ProcessingResult failure(String errorMessage, long processingTimeMs) {
        return new ProcessingResult(false, errorMessage, 
                                    LocalDateTime.now(), processingTimeMs, null);
    }
}