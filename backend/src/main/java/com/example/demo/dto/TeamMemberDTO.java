// src/main/java/com/example/demo/dto/TeamMemberDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamMemberDTO {
    private Long id;
    private Long teamId;
    private Long userId;
    private String role;
    private Integer status;
    private LocalDateTime joinedAt;
    private Long invitedBy;
    private String permissions;

    // 关联信息
    private UserDTO user;
    private UserDTO inviter;
}