package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KbDocumentDTO {
    private Long id;
    private String kbId;
    private String name;
    private Long size;
    private Integer chunkCount;
    private String status;
    private String splitMethod;
    private Integer chunkSize;
    private LocalDateTime uploadedAt;
}

