package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PluginDTO {
    private Long id;
    private String name;
    private String intro;
    private String description;
    private String iconUrl;
    private String pluginType;
    private String createMode;
    private String version;
    private String status;
    private String publishStatus;
    private String testStatus;
    private Boolean enabled;
    private String pluginUrl;
    private String authType;
    private String specJson;
    private List<PluginHeaderDTO> headers;
    private LocalDateTime lastTestAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PluginToolDTO> tools;
}


