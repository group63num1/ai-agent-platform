package com.example.demo.dto;

import lombok.Data;

@Data
public class KnowledgeBaseCreateRequest {
    private String name;
    private String description;
    private String category;
}

