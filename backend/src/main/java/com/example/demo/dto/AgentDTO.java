package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentDTO {
    private String id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


