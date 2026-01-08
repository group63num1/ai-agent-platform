// src/main/java/com/example/demo/dto/SystemConfigUpdateDTO.java
package com.example.demo.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class SystemConfigUpdateDTO {
    @NotBlank(message = "配置键不能为空")
    private String configKey;

    private String configValue;
    private String description;
    private String type;
    private Integer isPublic;
}