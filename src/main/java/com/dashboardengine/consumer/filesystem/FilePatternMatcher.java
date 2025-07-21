package com.dashboardengine.consumer.filesystem;

import org.springframework.stereotype.Component;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class FilePatternMatcher {

    public boolean matches(Path filePath, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return true; // No patterns means match all files
        }

        String fileName = filePath.getFileName().toString();
        
        for (String pattern : patterns) {
            if (matchesPattern(fileName, pattern)) {
                return true;
            }
        }
        
        return false;
    }

    private boolean matchesPattern(String fileName, String pattern) {
        // Handle glob patterns (e.g., *.txt, test*.csv)
        if (pattern.contains("*") || pattern.contains("?")) {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            return matcher.matches(Path.of(fileName));
        }
        
        // Handle regex patterns (patterns starting with regex:)
        if (pattern.startsWith("regex:")) {
            String regexPattern = pattern.substring(6); // Remove "regex:" prefix
            Pattern compiledPattern = Pattern.compile(regexPattern);
            return compiledPattern.matcher(fileName).matches();
        }
        
        // Exact match
        return fileName.equals(pattern);
    }
}