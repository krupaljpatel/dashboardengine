package com.dashboardengine.consumer.core;

import java.util.concurrent.CompletableFuture;

public interface MessageProcessor<T> {
    
    CompletableFuture<ProcessingResult> process(T message, String sourceType);
    
    boolean canProcess(String messageType);
}