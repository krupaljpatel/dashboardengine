package com.dashboardengine.consumer.filesystem;

import com.dashboardengine.consumer.api.dto.FileSystemConfigDto;
import com.dashboardengine.consumer.core.MessageProcessor;
import com.dashboardengine.consumer.core.SourceStatus;
import com.dashboardengine.consumer.metrics.ProcessingMetrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class IsolatedFileSystemConsumer {

    private static final Logger logger = LoggerFactory.getLogger(IsolatedFileSystemConsumer.class);
    
    private final String configName;
    private final FileSystemConfigDto config;
    private final MessageProcessor<Path> messageProcessor;
    private final ProcessingMetrics metrics;
    private final FilePatternMatcher patternMatcher;
    
    // Isolated resources for this consumer
    private final ThreadPoolExecutor processingExecutor;
    private final ScheduledExecutorService watchExecutor;
    private final Semaphore concurrencyLimiter;
    private final BlockingQueue<Path> processingQueue;
    
    private WatchService watchService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private volatile LocalDateTime lastActivity = LocalDateTime.now();
    
    private Future<?> watchTask;
    private Future<?> processingTask;

    public IsolatedFileSystemConsumer(String configName, FileSystemConfigDto config,
                                     MessageProcessor<Path> messageProcessor,
                                     ProcessingMetrics metrics,
                                     FilePatternMatcher patternMatcher) {
        this.configName = configName;
        this.config = config;
        this.messageProcessor = messageProcessor;
        this.metrics = metrics;
        this.patternMatcher = patternMatcher;
        
        // Create isolated thread pool for this consumer
        this.processingExecutor = new ThreadPoolExecutor(
            2, // core threads
            config.getMaxConcurrentFiles(),
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> new Thread(r, "FileSystem-" + configName + "-" + Thread.currentThread().getId()),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        this.watchExecutor = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, "FileWatch-" + configName)
        );
        
        this.concurrencyLimiter = new Semaphore(config.getMaxConcurrentFiles(), true);
        this.processingQueue = new LinkedBlockingQueue<>();
    }

    public void start() throws IOException {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting isolated filesystem consumer: {} for path: {}", configName, config.getPath());
            
            Path directory = Paths.get(config.getPath());
            Files.createDirectories(directory);
            
            this.watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService, 
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);
            
            // Process existing files
            processExistingFiles(directory);
            
            // Start directory watching
            this.watchTask = watchExecutor.submit(this::watchDirectory);
            
            // Start file processing
            this.processingTask = processingExecutor.submit(this::processFiles);
            
            logger.info("Started isolated filesystem consumer: {}", configName);
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping isolated filesystem consumer: {}", configName);
            
            // Cancel tasks
            if (watchTask != null) {
                watchTask.cancel(true);
            }
            if (processingTask != null) {
                processingTask.cancel(true);
            }
            
            // Close watch service
            if (watchService != null) {
                try {
                    watchService.close();
                } catch (IOException e) {
                    logger.error("Error closing watch service for: {}", configName, e);
                }
            }
            
            // Shutdown executors
            shutdownExecutor(processingExecutor, "processing");
            shutdownExecutor(watchExecutor, "watch");
            
            logger.info("Stopped isolated filesystem consumer: {}", configName);
        }
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Forcing shutdown of {} executor for: {}", name, configName);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public SourceStatus getStatus() {
        return new SourceStatus(
            "FILESYSTEM-" + configName,
            running.get() && processingExecutor.getActiveCount() >= 0,
            running.get() ? "Active and monitoring: " + config.getPath() : "Stopped",
            lastActivity,
            processedCount.get(),
            errorCount.get()
        );
    }

    public long getCurrentQueueSize() {
        return processingQueue.size();
    }

    public int getActiveThreadCount() {
        return processingExecutor.getActiveCount();
    }

    public double getAverageProcessingTime() {
        long count = processedCount.get();
        return count > 0 ? (double) totalProcessingTime.get() / count : 0.0;
    }

    private void processExistingFiles(Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile() && 
                        patternMatcher.matches(file, config.getPatterns()) &&
                        attrs.size() <= config.getMaxFileSizeBytes()) {
                        processingQueue.offer(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("Error processing existing files in: {} for config: {}", directory, configName, e);
        }
    }

    private void watchDirectory() {
        Path directory = Paths.get(config.getPath());
        
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                WatchKey key = watchService.poll(config.getPollIntervalMs(), TimeUnit.MILLISECONDS);
                if (key == null) {
                    continue;
                }
                
                lastActivity = LocalDateTime.now();
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    Path filePath = directory.resolve(filename);
                    
                    if (Files.isRegularFile(filePath) && 
                        patternMatcher.matches(filePath, config.getPatterns())) {
                        
                        // Check file size limit
                        try {
                            if (Files.size(filePath) <= config.getMaxFileSizeBytes()) {
                                processingQueue.offer(filePath);
                            } else {
                                logger.warn("File too large, skipping: {} ({}MB > {}MB)", 
                                           filePath, Files.size(filePath) / 1024 / 1024,
                                           config.getMaxFileSizeBytes() / 1024 / 1024);
                            }
                        } catch (IOException e) {
                            logger.error("Error checking file size: {}", filePath, e);
                        }
                    }
                }
                
                boolean valid = key.reset();
                if (!valid) {
                    logger.warn("Watch key no longer valid for: {} config: {}", directory, configName);
                    break;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error watching directory: {} for config: {}", directory, configName, e);
            }
        }
    }

    private void processFiles() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Path filePath = processingQueue.poll(1, TimeUnit.SECONDS);
                if (filePath != null) {
                    // Acquire semaphore to limit concurrency
                    concurrencyLimiter.acquire();
                    
                    // Submit to processing executor
                    processingExecutor.submit(() -> {
                        try {
                            processFile(filePath);
                        } finally {
                            concurrencyLimiter.release();
                        }
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processFile(Path filePath) {
        Timer.Sample sample = metrics.startTimer("FILESYSTEM-" + configName);
        long startTime = System.currentTimeMillis();
        
        try {
            // Check if file is ready (not locked)
            if (!isFileReady(filePath)) {
                logger.debug("File not ready, requeueing: {} for config: {}", filePath, configName);
                // Wait a bit and requeue
                Thread.sleep(500);
                processingQueue.offer(filePath);
                return;
            }
            
            logger.info("Processing file: {} for config: {}", filePath, configName);
            
            var result = messageProcessor.process(filePath, "FILESYSTEM-" + configName).join();
            long processingTime = System.currentTimeMillis() - startTime;
            
            if (result.success()) {
                processedCount.incrementAndGet();
                totalProcessingTime.addAndGet(processingTime);
                metrics.incrementProcessed("FILESYSTEM-" + configName);
                
                handlePostProcessing(filePath);
                
                logger.info("Successfully processed file: {} in {}ms for config: {}", 
                           filePath, processingTime, configName);
            } else {
                errorCount.incrementAndGet();
                metrics.incrementErrors("FILESYSTEM-" + configName);
                logger.error("Failed to process file: {} for config: {} - {}", 
                            filePath, configName, result.message());
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            metrics.incrementErrors("FILESYSTEM-" + configName);
            logger.error("Error processing file: {} for config: {}", filePath, configName, e);
        } finally {
            metrics.stopTimer(sample, "FILESYSTEM-" + configName);
        }
    }

    private boolean isFileReady(Path filePath) {
        try {
            try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void handlePostProcessing(Path filePath) {
        try {
            if (config.isDeleteAfterProcess()) {
                Files.delete(filePath);
                logger.debug("Deleted processed file: {} for config: {}", filePath, configName);
            } else if (config.getArchiveDir() != null) {
                Path archiveDir = Paths.get(config.getArchiveDir());
                Files.createDirectories(archiveDir);
                
                Path targetPath = archiveDir.resolve(filePath.getFileName());
                Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Archived processed file: {} -> {} for config: {}", 
                            filePath, targetPath, configName);
            }
        } catch (IOException e) {
            logger.error("Error in post-processing file: {} for config: {}", filePath, configName, e);
        }
    }
}