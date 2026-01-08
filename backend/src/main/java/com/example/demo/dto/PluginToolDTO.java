package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PluginToolDTO {
    private Long id;
    private Long pluginId;
    private String name;
    private String description;
    private String method;
    private String endpoint;
    private String serviceStatus;
    private String testStatus;
    private LocalDateTime lastTestAt;
    private List<PluginParamDTO> inputParams;
    private List<PluginParamDTO> outputParams;
    private String requestExample;
    private String responseExample;
}








