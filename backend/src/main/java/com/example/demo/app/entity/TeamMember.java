// src/main/java/com/example/demo/app/entity/TeamMember.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamMember {
    private Long id;
    private Long teamId;
    private Long userId;
    private String role;        // owner, admin, member
    private Integer status;     // 0-已移除，1-正常
    private LocalDateTime joinedAt;
    private Long invitedBy;
    private String permissions; // JSON格式的特殊权限
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}