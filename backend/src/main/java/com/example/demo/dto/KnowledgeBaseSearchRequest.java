package com.example.demo.dto;

import lombok.Data;

@Data
public class KnowledgeBaseSearchRequest {
    private String query;
    private Integer topK;
    private Double similarityThreshold;
}

