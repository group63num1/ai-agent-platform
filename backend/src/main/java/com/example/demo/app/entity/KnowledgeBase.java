package com.example.demo.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeBase {
    private String kbId;
    private Long userId;
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

