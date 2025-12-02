package com.example.demo.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PluginTool {
    private Long id;
    private Long pluginId;
    private String name;
    private String description;
    private String method;
    private String endpoint;
    private String serviceStatus; // online/offline
    private String testStatus; // pending/passed/failed
    private LocalDateTime lastTestAt;
    private String inputParamsJson;
    private String outputParamsJson;
    private String requestExample;
    private String responseExample;
}


