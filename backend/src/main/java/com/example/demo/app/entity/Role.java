// src/main/java/com/example/demo/app/entity/Role.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Role {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer isSystem;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}