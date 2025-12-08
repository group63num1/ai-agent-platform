package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeBaseDTO {
    private String kbId;
    private String name;
    private String description;
    private String category;
    private Integer documentCount;
    private Integer chunkCount;
    private Long totalSize;
    private String milvusCollection;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

