// src/main/java/com/example/demo/dto/SystemConfigDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemConfigDTO {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private String type;
    private Integer isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
