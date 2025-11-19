// src/main/java/com/example/demo/dto/KnowledgeDocumentDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeDocumentDTO {
    private Long id;
    private Long userId;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private String content;
    private Integer status; // 0-处理中，1-已完成，2-失败
    private Integer chunkCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

