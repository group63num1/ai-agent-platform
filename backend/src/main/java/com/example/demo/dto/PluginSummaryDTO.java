package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PluginSummaryDTO {
    private Long id;
    private String name;
    private String description;
    private String version;
    private String status;
    private String testStatus;
    private String publishStatus;
    private Boolean enabled;
    private String iconUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


