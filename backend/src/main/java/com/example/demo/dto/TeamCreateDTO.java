// src/main/java/com/example/demo/dto/TeamCreateDTO.java
package com.example.demo.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class TeamCreateDTO {
    @NotBlank(message = "团队名称不能为空")
    @Size(min = 2, max = 100, message = "团队名称长度必须在2-100个字符之间")
    private String name;

    private String description;
    private String visibility = "private";
    private String joinPolicy = "approval";
    private Integer maxMembers;
}