package com.dashboardengine.consumer.filesystem;

import com.dashboardengine.consumer.config.ApplicationProperties;
import com.dashboardengine.consumer.core.MessageProcessor;
import com.dashboardengine.consumer.core.SourceAdapter;
import com.dashboardengine.consumer.core.SourceStatus;
import com.dashboardengine.consumer.metrics.ProcessingMetrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Deprecated // Use IsolatedFileSystemConsumer via FileSystemConfigurationManager instead
public class FileSystemSourceAdapter implements SourceAdapter {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemSourceAdapter.class);
    
    private final ApplicationProperties properties;
    private final MessageProcessor<Path> messageProcessor;
    private final ProcessingMetrics metrics;
    private final Executor executor;
    private final FilePatternMatcher patternMatcher;
    
    private final Map<String, WatchService> watchServices = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private volatile LocalDateTime lastCheck = LocalDateTime.now();

    public FileSystemSourceAdapter(
            ApplicationProperties properties,
            MessageProcessor<Path> messageProcessor,
            ProcessingMetrics metrics,
            @Qualifier("processingExecutor") Executor executor,
            FilePatternMatcher patternMatcher) {
        this.properties = properties;
        this.messageProcessor = messageProcessor;
        this.metrics = metrics;
        this.executor = executor;
        this.patternMatcher = patternMatcher;
    }

    @Override
    public String getSourceType() {
        return "FILESYSTEM";
    }

    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (running.compareAndSet(false, true)) {
                    startWatching();
                    logger.info("FileSystem consumer started successfully");
                }
            } catch (Exception e) {
                logger.error("Failed to start FileSystem consumer", e);
                running.set(false);
                throw new RuntimeException("Failed to start FileSystem consumer", e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (running.compareAndSet(true, false)) {
                    stopWatching();
                    logger.info("FileSystem consumer stopped successfully");
                }
            } catch (Exception e) {
                logger.error("Error stopping FileSystem consumer", e);
                throw new RuntimeException("Failed to stop FileSystem consumer", e);
            }
        }, executor);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public SourceStatus getStatus() {
        return new SourceStatus(
            getSourceType(),
            running.get() && !watchServices.isEmpty(),
            running.get() ? "Active and monitoring directories" : "Not running",
            lastCheck,
            processedCount.get(),
            errorCount.get()
        );
    }

    private void startWatching() throws IOException {
        Map<String, ApplicationProperties.FileSystemConfig> configs = properties.getFilesystem();
        if (configs == null || configs.isEmpty()) {
            logger.warn("No filesystem configurations found");
            return;
        }

        for (Map.Entry<String, ApplicationProperties.FileSystemConfig> entry : configs.entrySet()) {
            String configName = entry.getKey();
            ApplicationProperties.FileSystemConfig config = entry.getValue();
            
            startWatchingDirectory(configName, config);
        }
    }

    private void startWatchingDirectory(String configName, ApplicationProperties.FileSystemConfig config) throws IOException {
        Path directory = Paths.get(config.getPath());
        
        if (!Files.exists(directory)) {
            logger.warn("Directory does not exist, creating: {}", directory);
            Files.createDirectories(directory);
        }

        WatchService watchService = FileSystems.getDefault().newWatchService();
        directory.register(watchService, 
            StandardWatchEventKinds.ENTRY_CREATE, 
            StandardWatchEventKinds.ENTRY_MODIFY);

        watchServices.put(configName, watchService);

        // Process existing files first
        processExistingFiles(directory, config);

        // Start watching for new files
        CompletableFuture.runAsync(() -> watchDirectory(watchService, directory, config), executor);
        
        logger.info("Started watching directory: {} for config: {}", directory, configName);
    }

    private void processExistingFiles(Path directory, ApplicationProperties.FileSystemConfig config) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile() && patternMatcher.matches(file, config.getPatterns())) {
                        CompletableFuture.runAsync(() -> processFile(file, config), executor);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("Error processing existing files in directory: {}", directory, e);
        }
    }

    private void watchDirectory(WatchService watchService, Path directory, ApplicationProperties.FileSystemConfig config) {
        while (running.get()) {
            try {
                WatchKey key = watchService.take();
                lastCheck = LocalDateTime.now();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    Path filePath = directory.resolve(filename);

                    if (Files.isRegularFile(filePath) && patternMatcher.matches(filePath, config.getPatterns())) {
                        // Add small delay to ensure file is fully written
                        CompletableFuture
                            .delayedExecutor(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(500), 
                                           java.util.concurrent.TimeUnit.NANOSECONDS, executor)
                            .execute(() -> processFile(filePath, config));
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    logger.warn("Watch key no longer valid for directory: {}", directory);
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("Directory watching interrupted for: {}", directory);
                break;
            } catch (Exception e) {
                logger.error("Error watching directory: {}", directory, e);
                errorCount.incrementAndGet();
            }
        }
    }

    private void processFile(Path filePath, ApplicationProperties.FileSystemConfig config) {
        Timer.Sample sample = metrics.startTimer(getSourceType());
        
        try {
            // Check if file is locked or still being written
            if (!isFileReady(filePath)) {
                logger.debug("File not ready for processing: {}", filePath);
                return;
            }

            logger.info("Processing file: {}", filePath);
            
            var result = messageProcessor.process(filePath, getSourceType()).join();
            
            if (result.success()) {
                processedCount.incrementAndGet();
                metrics.incrementProcessed(getSourceType());
                
                // Handle post-processing (archive or delete)
                handlePostProcessing(filePath, config);
                
                logger.info("Successfully processed file: {} in {}ms", 
                           filePath, result.processingTimeMs());
            } else {
                errorCount.incrementAndGet();
                metrics.incrementErrors(getSourceType());
                logger.error("Failed to process file: {} - {}", filePath, result.message());
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            metrics.incrementErrors(getSourceType());
            logger.error("Error processing file: {}", filePath, e);
        } finally {
            metrics.stopTimer(sample, getSourceType());
        }
    }

    private boolean isFileReady(Path filePath) {
        try {
            // Try to open file for writing to check if it's locked
            try (var channel = FileChannel.open(filePath, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                return true;
            }
        } catch (IOException e) {
            // File is likely still being written or locked
            return false;
        }
    }

    private void handlePostProcessing(Path filePath, ApplicationProperties.FileSystemConfig config) {
        try {
            if (config.isDeleteAfterProcess()) {
                Files.delete(filePath);
                logger.debug("Deleted processed file: {}", filePath);
            } else if (config.getArchiveDir() != null) {
                Path archiveDir = Paths.get(config.getArchiveDir());
                Files.createDirectories(archiveDir);
                
                Path targetPath = archiveDir.resolve(filePath.getFileName());
                Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Archived processed file: {} -> {}", filePath, targetPath);
            }
        } catch (IOException e) {
            logger.error("Error in post-processing file: {}", filePath, e);
        }
    }

    private void stopWatching() {
        for (Map.Entry<String, WatchService> entry : watchServices.entrySet()) {
            try {
                entry.getValue().close();
                logger.debug("Closed watch service for config: {}", entry.getKey());
            } catch (IOException e) {
                logger.error("Error closing watch service for config: {}", entry.getKey(), e);
            }
        }
        watchServices.clear();
    }

    @PreDestroy
    public void cleanup() {
        stop();
    }
}