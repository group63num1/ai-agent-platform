// src/main/java/com/example/demo/app/entity/SystemConfig.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private String type;        // string, number, boolean, json
    private Integer isPublic;   // 0-私有，1-公开
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}