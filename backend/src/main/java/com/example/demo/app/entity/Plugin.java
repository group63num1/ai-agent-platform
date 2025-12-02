package com.example.demo.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Plugin {
    private Long id;
    private Long userId;
    private String name;
    private String intro;
    private String description;
    private String iconUrl;
    private String pluginType; // cloud/edge/mcp
    private String createMode; // existing service / coze ide etc
    private String version;
    private String status; // draft/enabled/disabled
    private String publishStatus; // unpublished/published
    private String testStatus; // pending/passed/failed
    private Boolean enabled;
    private String pluginUrl;
    private String authType;
    private String headersJson;
    private String specJson;
    private LocalDateTime lastTestAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


