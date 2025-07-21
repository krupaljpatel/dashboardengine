package com.dashboardengine.consumer.core;

import java.util.concurrent.CompletableFuture;

public interface SourceAdapter {
    
    String getSourceType();
    
    CompletableFuture<Void> start();
    
    CompletableFuture<Void> stop();
    
    boolean isRunning();
    
    SourceStatus getStatus();
}