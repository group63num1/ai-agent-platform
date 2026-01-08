package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class PluginUpdateRequest {
    private String name;
    private String intro;
    private String description;
    private String iconUrl;
    private String pluginType;
    private String createMode;
    private String version;
    private String pluginUrl;
    private String authType;
    private String specJson;
    private List<PluginHeaderDTO> headers;
    private List<PluginToolDTO> tools;
}


