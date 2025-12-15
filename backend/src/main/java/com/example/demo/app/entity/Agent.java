package com.example.demo.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Agent {
    private String id;
    private Long userId;
    private String name;
    private String description;
    private String model;
    private String prompt;
    private String profileMd;
    private Integer contextRounds;
    private Integer maxTokens;
    private String plugins; // JSON array string
    private String knowledgeBase; // JSON array string
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


