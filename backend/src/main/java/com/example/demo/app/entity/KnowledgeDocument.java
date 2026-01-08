// src/main/java/com/example/demo/app/entity/KnowledgeDocument.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeDocument {
    private Long id;
    private Long userId;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private String content;
    private String contentHash;
    private Integer status; // 0-处理中，1-已完成，2-失败
    private Integer chunkCount;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted; // 0-未删除，1-已删除
}

