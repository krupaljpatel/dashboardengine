package com.dashboardengine.consumer.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ProcessingMetrics {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> processedCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> errorCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> processingTimers = new ConcurrentHashMap<>();

    public ProcessingMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementProcessed(String sourceType) {
        processedCounters.computeIfAbsent(sourceType, 
            type -> Counter.builder("consumer.messages.processed")
                .tag("source_type", type)
                .register(meterRegistry)
        ).increment();
    }

    public void incrementErrors(String sourceType) {
        errorCounters.computeIfAbsent(sourceType,
            type -> Counter.builder("consumer.messages.errors")
                .tag("source_type", type)
                .register(meterRegistry)
        ).increment();
    }

    public Timer.Sample startTimer(String sourceType) {
        Timer timer = processingTimers.computeIfAbsent(sourceType,
            type -> Timer.builder("consumer.processing.duration")
                .tag("source_type", type)
                .register(meterRegistry)
        );
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String sourceType) {
        Timer timer = processingTimers.get(sourceType);
        if (timer != null) {
            sample.stop(timer);
        }
    }
}