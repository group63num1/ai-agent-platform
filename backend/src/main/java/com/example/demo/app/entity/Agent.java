package com.example.demo.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Agent {
    private String id;
    private String name;
    private String description;
    private String model;
    private String prompt;
    private String profileMd;
    private Integer contextRounds;
    private Integer maxTokens;
    private String plugins; // JSON数组字符串
    private String status;
    private String sessionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


