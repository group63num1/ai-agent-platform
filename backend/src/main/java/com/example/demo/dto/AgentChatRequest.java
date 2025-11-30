package com.example.demo.dto;

import lombok.Data;

@Data
public class AgentChatRequest {
    private String message;
    private String model;
    private Integer contextRounds;
    private Integer maxTokens;
}


