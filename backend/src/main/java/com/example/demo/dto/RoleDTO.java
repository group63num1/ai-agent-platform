// src/main/java/com/example/demo/dto/RoleDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
// 暂时注释掉，等 PermissionDTO 创建后再取消注释
import java.util.List;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer isSystem;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private List<PermissionDTO> permissions;
}