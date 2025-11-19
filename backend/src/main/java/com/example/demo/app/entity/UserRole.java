// src/main/java/com/example/demo/app/entity/UserRole.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserRole {
    private Long id;
    private Long userId;
    private Long roleId;
    private Long assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}