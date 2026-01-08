// src/main/java/com/example/demo/app/entity/KnowledgeChunk.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeChunk {
    private Long id;
    private Long documentId;
    private Integer chunkIndex;
    private String content;
    private String contentHash;
    private Integer tokenCount;
    private Integer charCount;
    private String metadata; // JSON格式
    private LocalDateTime createdAt;
}

