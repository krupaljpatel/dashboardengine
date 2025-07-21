package com.dashboardengine.consumer.filesystem;

import com.dashboardengine.consumer.api.dto.FileSystemConfigDto;
import com.dashboardengine.consumer.core.MessageProcessor;
import com.dashboardengine.consumer.metrics.ProcessingMetrics;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class IsolatedFileSystemConsumerFactory {

    private final MessageProcessor<Path> messageProcessor;
    private final ProcessingMetrics metrics;
    private final FilePatternMatcher patternMatcher;

    public IsolatedFileSystemConsumerFactory(MessageProcessor<Path> messageProcessor,
                                            ProcessingMetrics metrics,
                                            FilePatternMatcher patternMatcher) {
        this.messageProcessor = messageProcessor;
        this.metrics = metrics;
        this.patternMatcher = patternMatcher;
    }

    public IsolatedFileSystemConsumer createConsumer(String configName, FileSystemConfigDto config) {
        return new IsolatedFileSystemConsumer(
            configName,
            config,
            messageProcessor,
            metrics,
            patternMatcher
        );
    }
}