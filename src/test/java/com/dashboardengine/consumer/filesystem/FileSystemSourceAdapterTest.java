package com.dashboardengine.consumer.filesystem;

import com.dashboardengine.consumer.config.ApplicationProperties;
import com.dashboardengine.consumer.core.MessageProcessor;
import com.dashboardengine.consumer.core.ProcessingResult;
import com.dashboardengine.consumer.metrics.ProcessingMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class FileSystemSourceAdapterTest {

    @TempDir
    Path tempDir;

    @Mock
    private MessageProcessor<Path> messageProcessor;

    private ApplicationProperties properties;
    private ProcessingMetrics metrics;
    private FilePatternMatcher patternMatcher;
    private FileSystemSourceAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        properties = new ApplicationProperties();
        metrics = new ProcessingMetrics(new SimpleMeterRegistry());
        patternMatcher = new FilePatternMatcher();
        
        adapter = new FileSystemSourceAdapter(
            properties, 
            messageProcessor, 
            metrics, 
            Executors.newFixedThreadPool(2),
            patternMatcher
        );

        // Mock successful processing
        when(messageProcessor.process(any(Path.class), eq("FILESYSTEM")))
            .thenReturn(CompletableFuture.completedFuture(
                ProcessingResult.success(10, "Test processing")));
    }

    @Test
    void testSourceType() {
        assertEquals("FILESYSTEM", adapter.getSourceType());
    }

    @Test
    void testInitialStatus() {
        var status = adapter.getStatus();
        assertEquals("FILESYSTEM", status.sourceType());
        assertFalse(status.healthy());
        assertEquals(0, status.processedCount());
        assertEquals(0, status.errorCount());
    }

    @Test
    void testStartStopAdapter() {
        // Setup configuration
        ApplicationProperties.FileSystemConfig config = new ApplicationProperties.FileSystemConfig();
        config.setPath(tempDir.toString());
        config.setPatterns(List.of("*.txt"));
        
        properties.setFilesystem(Map.of("test", config));

        // Test start
        assertFalse(adapter.isRunning());
        adapter.start().join();
        assertTrue(adapter.isRunning());

        // Test stop
        adapter.stop().join();
        assertFalse(adapter.isRunning());
    }

    @Test
    void testFileProcessing() throws IOException, InterruptedException {
        // Setup configuration
        ApplicationProperties.FileSystemConfig config = new ApplicationProperties.FileSystemConfig();
        config.setPath(tempDir.toString());
        config.setPatterns(List.of("*.txt"));
        config.setDeleteAfterProcess(false);
        
        properties.setFilesystem(Map.of("test", config));

        // Start adapter
        adapter.start().join();
        
        // Create test file
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "Test content".getBytes());
        
        // Give time for processing
        Thread.sleep(1000);
        
        // Verify status shows processed file
        var status = adapter.getStatus();
        assertTrue(status.healthy());
        assertTrue(status.processedCount() > 0);
        
        adapter.stop().join();
    }
}