package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("knowledge_base")
    private List<String> knowledgeBase;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


