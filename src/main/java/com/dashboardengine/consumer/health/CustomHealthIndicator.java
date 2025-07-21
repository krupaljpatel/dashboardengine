package com.dashboardengine.consumer.health;

import com.dashboardengine.consumer.core.SourceAdapter;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final List<SourceAdapter> sourceAdapters;

    public CustomHealthIndicator(List<SourceAdapter> sourceAdapters) {
        this.sourceAdapters = sourceAdapters;
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        for (SourceAdapter adapter : sourceAdapters) {
            var status = adapter.getStatus();
            builder.withDetail(status.sourceType(), status);
            
            if (!status.healthy()) {
                builder.down();
            }
        }
        
        return builder.build();
    }
}