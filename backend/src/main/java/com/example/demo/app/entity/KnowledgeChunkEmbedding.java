// src/main/java/com/example/demo/app/entity/KnowledgeChunkEmbedding.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeChunkEmbedding {
    private Long id;
    private Long chunkId;
    private String embeddingModel;
    private String embedding; // JSON格式的向量数组
    private Integer embeddingDimension;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

