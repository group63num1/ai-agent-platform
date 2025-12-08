package com.example.demo.dto;

import lombok.Data;

@Data
public class KnowledgeBaseUpdateRequest {
    private String name;
    private String description;
    private String category;
    private Boolean enabled;
}

