// src/main/java/com/example/demo/dto/PermissionDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PermissionDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String module;
    private String resource;
    private String action;
    private Integer isSystem;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}