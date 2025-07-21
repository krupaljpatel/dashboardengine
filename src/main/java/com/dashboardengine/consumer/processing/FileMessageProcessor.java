package com.dashboardengine.consumer.processing;

import com.dashboardengine.consumer.core.MessageProcessor;
import com.dashboardengine.consumer.core.ProcessingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@Component
public class FileMessageProcessor implements MessageProcessor<Path> {

    private static final Logger logger = LoggerFactory.getLogger(FileMessageProcessor.class);

    @Override
    public CompletableFuture<ProcessingResult> process(Path filePath, String sourceType) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                logger.debug("Processing file: {} from source: {}", filePath, sourceType);
                
                // Validate file exists and is readable
                if (!Files.exists(filePath)) {
                    return ProcessingResult.failure("File does not exist: " + filePath, 
                                                   System.currentTimeMillis() - startTime);
                }
                
                if (!Files.isReadable(filePath)) {
                    return ProcessingResult.failure("File is not readable: " + filePath, 
                                                   System.currentTimeMillis() - startTime);
                }
                
                // Get file info
                long fileSize = Files.size(filePath);
                String contentType = getContentType(filePath);
                
                logger.info("Processing file: {} (size: {} bytes, type: {})", 
                           filePath.getFileName(), fileSize, contentType);
                
                // Process based on file type
                ProcessingResult result = processFileByType(filePath, contentType, startTime);
                
                if (result.success()) {
                    logger.info("Successfully processed file: {} in {}ms", 
                               filePath.getFileName(), result.processingTimeMs());
                }
                
                return result;
                
            } catch (Exception e) {
                logger.error("Error processing file: {}", filePath, e);
                return ProcessingResult.failure("Processing error: " + e.getMessage(), 
                                               System.currentTimeMillis() - startTime);
            }
        });
    }

    @Override
    public boolean canProcess(String messageType) {
        return "FILE".equals(messageType) || "PATH".equals(messageType);
    }

    private String getContentType(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        
        if (fileName.endsWith(".txt")) return "TEXT";
        if (fileName.endsWith(".csv")) return "CSV";
        if (fileName.endsWith(".json")) return "JSON";
        if (fileName.endsWith(".xml")) return "XML";
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) return "EXCEL";
        if (fileName.endsWith(".pdf")) return "PDF";
        
        return "UNKNOWN";
    }

    private ProcessingResult processFileByType(Path filePath, String contentType, long startTime) {
        try {
            switch (contentType) {
                case "TEXT":
                    return processTextFile(filePath, startTime);
                case "CSV":
                    return processCsvFile(filePath, startTime);
                case "JSON":
                    return processJsonFile(filePath, startTime);
                case "XML":
                    return processXmlFile(filePath, startTime);
                case "EXCEL":
                    return processExcelFile(filePath, startTime);
                default:
                    return processGenericFile(filePath, startTime);
            }
        } catch (Exception e) {
            return ProcessingResult.failure("Type-specific processing error: " + e.getMessage(), 
                                           System.currentTimeMillis() - startTime);
        }
    }

    private ProcessingResult processTextFile(Path filePath, long startTime) throws Exception {
        // Simulate text file processing
        long lineCount = Files.lines(filePath).count();
        
        logger.debug("Processed text file with {} lines", lineCount);
        
        return ProcessingResult.success(
            System.currentTimeMillis() - startTime,
            "Processed " + lineCount + " lines from " + filePath.getFileName()
        );
    }

    private ProcessingResult processCsvFile(Path filePath, long startTime) throws Exception {
        // Simulate CSV processing
        long lineCount = Files.lines(filePath).count();
        long recordCount = Math.max(0, lineCount - 1); // Exclude header
        
        logger.debug("Processed CSV file with {} records", recordCount);
        
        return ProcessingResult.success(
            System.currentTimeMillis() - startTime,
            "Processed " + recordCount + " records from " + filePath.getFileName()
        );
    }

    private ProcessingResult processJsonFile(Path filePath, long startTime) throws Exception {
        // Simulate JSON processing
        String content = Files.readString(filePath);
        int objectCount = content.split("\\{").length - 1;
        
        logger.debug("Processed JSON file with {} objects", objectCount);
        
        return ProcessingResult.success(
            System.currentTimeMillis() - startTime,
            "Processed " + objectCount + " objects from " + filePath.getFileName()
        );
    }

    private ProcessingResult processXmlFile(Path filePath, long startTime) throws Exception {
        // Simulate XML processing
        String content = Files.readString(filePath);
        int elementCount = content.split("<[^/]").length - 1;
        
        logger.debug("Processed XML file with {} elements", elementCount);
        
        return ProcessingResult.success(
            System.currentTimeMillis() - startTime,
            "Processed " + elementCount + " elements from " + filePath.getFileName()
        );
    }

    private ProcessingResult processExcelFile(Path filePath, long startTime) throws Exception {
        // Simulate Excel processing (would use Apache POI in real implementation)
        long fileSize = Files.size(filePath);
        int estimatedRows = (int) (fileSize / 100); // Rough estimation
        
        logger.debug("Processed Excel file with estimated {} rows", estimatedRows);
        
        return ProcessingResult.success(
            System.currentTimeMillis() - startTime,
            "Processed ~" + estimatedRows + " rows from " + filePath.getFileName()
        );
    }

    private ProcessingResult processGenericFile(Path filePath, long startTime) throws Exception {
        // Generic file processing
        long fileSize = Files.size(filePath);
        
        logger.debug("Processed generic file of {} bytes", fileSize);
        
        return ProcessingResult.success(
            System.currentTimeMillis() - startTime,
            "Processed " + fileSize + " bytes from " + filePath.getFileName()
        );
    }
}