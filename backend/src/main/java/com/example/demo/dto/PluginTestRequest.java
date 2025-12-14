package com.example.demo.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PluginTestRequest {
    private Long pluginId;
    private Long toolId;
    private Map<String, Object> inputs;
}








