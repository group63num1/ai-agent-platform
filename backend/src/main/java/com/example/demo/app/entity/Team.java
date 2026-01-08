// src/main/java/com/example/demo/app/entity/Team.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Team {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String avatarUrl;
    private String visibility;  // public, private
    private String joinPolicy;  // open, approval, invite
    private Integer maxMembers;
    private Long ownerId;
    private Integer status;     // 0-禁用，1-正常，2-已解散
    private String settings;    // JSON格式的团队设置
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
