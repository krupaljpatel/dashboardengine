package com.dashboardengine.consumer.filesystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilePatternMatcherTest {

    private FilePatternMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new FilePatternMatcher();
    }

    @Test
    void testGlobPatterns() {
        Path textFile = Path.of("test.txt");
        Path csvFile = Path.of("data.csv");
        Path jsonFile = Path.of("config.json");
        
        // Test wildcard patterns
        assertTrue(matcher.matches(textFile, List.of("*.txt")));
        assertTrue(matcher.matches(csvFile, List.of("*.csv")));
        assertFalse(matcher.matches(jsonFile, List.of("*.txt")));
        
        // Test multiple patterns
        assertTrue(matcher.matches(textFile, List.of("*.txt", "*.csv")));
        assertTrue(matcher.matches(csvFile, List.of("*.txt", "*.csv")));
        assertFalse(matcher.matches(jsonFile, List.of("*.txt", "*.csv")));
        
        // Test prefix patterns
        assertTrue(matcher.matches(Path.of("test_file.txt"), List.of("test*")));
        assertFalse(matcher.matches(Path.of("data_file.txt"), List.of("test*")));
    }

    @Test
    void testExactMatch() {
        Path file = Path.of("exact-file.txt");
        
        assertTrue(matcher.matches(file, List.of("exact-file.txt")));
        assertFalse(matcher.matches(file, List.of("other-file.txt")));
    }

    @Test
    void testRegexPatterns() {
        Path file1 = Path.of("file123.txt");
        Path file2 = Path.of("fileABC.txt");
        Path file3 = Path.of("document.pdf");
        
        // Test regex pattern for files with numbers
        assertTrue(matcher.matches(file1, List.of("regex:file\\d+\\.txt")));
        assertFalse(matcher.matches(file2, List.of("regex:file\\d+\\.txt")));
        assertFalse(matcher.matches(file3, List.of("regex:file\\d+\\.txt")));
    }

    @Test
    void testEmptyPatternsMatchAll() {
        Path anyFile = Path.of("any-file.ext");
        
        assertTrue(matcher.matches(anyFile, List.of()));
        assertTrue(matcher.matches(anyFile, null));
    }

    @Test
    void testQuestionMarkWildcard() {
        Path file1 = Path.of("file1.txt");
        Path file2 = Path.of("file22.txt");
        
        assertTrue(matcher.matches(file1, List.of("file?.txt")));
        assertFalse(matcher.matches(file2, List.of("file?.txt")));
    }
}