// src/main/java/com/example/demo/dto/KnowledgeChunkDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeChunkDTO {
    private Long id;
    private Long documentId;
    private Integer chunkIndex;
    private String content;
    private Integer tokenCount;
    private Integer charCount;
    private String metadata;
    private LocalDateTime createdAt;
}

