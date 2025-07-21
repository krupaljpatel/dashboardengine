package com.dashboardengine.consumer.api;

import com.dashboardengine.consumer.api.dto.FileSystemConfigDto;
import com.dashboardengine.consumer.api.dto.FileSystemStatusDto;
import com.dashboardengine.consumer.filesystem.FileSystemConfigurationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileSystemConfigController.class)
class FileSystemConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FileSystemConfigurationManager configManager;

    @Test
    void testGetAllConfigurations() throws Exception {
        FileSystemConfigDto config = new FileSystemConfigDto(
            "/test/path", List.of("*.txt"), 5000, null, false, true, 10, 100*1024*1024
        );
        
        when(configManager.getAllConfigurations()).thenReturn(Map.of("test", config));

        mockMvc.perform(get("/api/v1/filesystem/configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.test.path").value("/test/path"))
                .andExpect(jsonPath("$.test.patterns[0]").value("*.txt"));
    }

    @Test
    void testGetConfiguration() throws Exception {
        FileSystemConfigDto config = new FileSystemConfigDto(
            "/test/path", List.of("*.txt"), 5000, null, false, true, 10, 100*1024*1024
        );
        
        when(configManager.getConfiguration("test")).thenReturn(Optional.of(config));

        mockMvc.perform(get("/api/v1/filesystem/configs/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("/test/path"));
    }

    @Test
    void testGetConfigurationNotFound() throws Exception {
        when(configManager.getConfiguration("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/filesystem/configs/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateConfiguration() throws Exception {
        FileSystemConfigDto config = new FileSystemConfigDto(
            "/test/path", List.of("*.txt"), 5000, null, false, true, 10, 100*1024*1024
        );
        
        when(configManager.createConfiguration(eq("test"), any(FileSystemConfigDto.class)))
                .thenReturn(config);

        mockMvc.perform(post("/api/v1/filesystem/configs/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").value("/test/path"));
    }

    @Test
    void testCreateConfigurationValidationError() throws Exception {
        FileSystemConfigDto invalidConfig = new FileSystemConfigDto(
            "", List.of("*.txt"), -1, null, false, true, 10, 100*1024*1024
        );

        mockMvc.perform(post("/api/v1/filesystem/configs/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidConfig)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testStartConsumer() throws Exception {
        FileSystemStatusDto status = new FileSystemStatusDto(
            "test", "/test/path", true, true, "Running", 
            LocalDateTime.now(), 0, 0, 0, 1, 0.0
        );
        
        when(configManager.startConsumer("test")).thenReturn(status);

        mockMvc.perform(post("/api/v1/filesystem/configs/test/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(true))
                .andExpect(jsonPath("$.configName").value("test"));
    }

    @Test
    void testGetAllStatus() throws Exception {
        FileSystemStatusDto status = new FileSystemStatusDto(
            "test", "/test/path", true, true, "Running",
            LocalDateTime.now(), 5, 0, 0, 1, 100.0
        );
        
        when(configManager.getAllStatus()).thenReturn(Map.of("test", status));

        mockMvc.perform(get("/api/v1/filesystem/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.test.running").value(true))
                .andExpect(jsonPath("$.test.processedFiles").value(5));
    }
}