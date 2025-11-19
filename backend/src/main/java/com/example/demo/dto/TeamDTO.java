// src/main/java/com/example/demo/dto/TeamDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String avatarUrl;
    private String visibility;
    private String joinPolicy;
    private Integer maxMembers;
    private Long ownerId;
    private Integer status;
    private String settings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联信息
    private UserDTO owner;
    private Integer memberCount;
    private String currentUserRole;
}