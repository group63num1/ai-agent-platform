package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeBaseSearchResponse {
    private List<SearchResultItemDTO> items;
    private Integer total;
    private String query;
    private Integer topK;
    private Double similarityThreshold;
}

