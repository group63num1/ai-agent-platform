package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class AgentUpdateRequest {
    private String name;
    private String description;
    private String model;
    private String prompt;
    private String profileMd;
    private Integer contextRounds;
    private Integer maxTokens;
    private List<String> plugins;
    private String status;
    private String sessionId;
}


