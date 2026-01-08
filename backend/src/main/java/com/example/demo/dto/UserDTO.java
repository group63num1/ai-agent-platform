// src/main/java/com/example/demo/dto/UserDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private Integer status;
    private Integer emailVerified;
    private Integer phoneVerified;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private List<RoleDTO> roles;
}
