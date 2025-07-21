package com.dashboardengine.consumer.processing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileMessageProcessorTest {

    @TempDir
    Path tempDir;

    private FileMessageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new FileMessageProcessor();
    }

    @Test
    void testCanProcess() {
        assertTrue(processor.canProcess("FILE"));
        assertTrue(processor.canProcess("PATH"));
        assertFalse(processor.canProcess("MESSAGE"));
    }

    @Test
    void testProcessTextFile() throws Exception {
        Path textFile = tempDir.resolve("test.txt");
        Files.write(textFile, "Line 1\nLine 2\nLine 3".getBytes());

        var result = processor.process(textFile, "FILESYSTEM").join();

        assertTrue(result.success());
        assertTrue(result.message().contains("3 lines"));
        assertTrue(result.processingTimeMs() >= 0);
    }

    @Test
    void testProcessCsvFile() throws Exception {
        Path csvFile = tempDir.resolve("data.csv");
        Files.write(csvFile, "Name,Age\nJohn,25\nJane,30".getBytes());

        var result = processor.process(csvFile, "FILESYSTEM").join();

        assertTrue(result.success());
        assertTrue(result.message().contains("2 records")); // Excludes header
        assertTrue(result.processingTimeMs() >= 0);
    }

    @Test
    void testProcessJsonFile() throws Exception {
        Path jsonFile = tempDir.resolve("config.json");
        Files.write(jsonFile, "{\"key1\":\"value1\"},{\"key2\":\"value2\"}".getBytes());

        var result = processor.process(jsonFile, "FILESYSTEM").join();

        assertTrue(result.success());
        assertTrue(result.message().contains("objects"));
        assertTrue(result.processingTimeMs() >= 0);
    }

    @Test
    void testProcessNonExistentFile() {
        Path nonExistent = tempDir.resolve("does-not-exist.txt");

        var result = processor.process(nonExistent, "FILESYSTEM").join();

        assertFalse(result.success());
        assertTrue(result.message().contains("does not exist"));
    }

    @Test
    void testProcessGenericFile() throws Exception {
        Path genericFile = tempDir.resolve("unknown.ext");
        Files.write(genericFile, "Some binary content".getBytes());

        var result = processor.process(genericFile, "FILESYSTEM").join();

        assertTrue(result.success());
        assertTrue(result.message().contains("bytes"));
        assertTrue(result.processingTimeMs() >= 0);
    }
}