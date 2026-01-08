// src/main/java/com/example/demo/app/entity/TeamInvitation.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamInvitation {
    private Long id;
    private Long teamId;
    private Long inviterId;
    private String inviteeEmail;
    private Long inviteeUserId;
    private String invitationCode;
    private String role;        // member, admin
    private String message;
    private String status;      // pending, accepted, rejected, expired
    private LocalDateTime expiresAt;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}