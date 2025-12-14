package com.example.demo.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Workflow {
    private Long id;
    private Long userId;
    private String name;
    private String intro;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
