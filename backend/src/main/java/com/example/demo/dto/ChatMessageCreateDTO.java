// src/main/java/com/example/demo/dto/ChatMessageCreateDTO.java
package com.example.demo.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ChatMessageCreateDTO {
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    // role 默认为 "user"，由后端控制
}

