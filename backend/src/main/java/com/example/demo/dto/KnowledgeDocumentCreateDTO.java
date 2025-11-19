// src/main/java/com/example/demo/dto/KnowledgeDocumentCreateDTO.java
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeDocumentCreateDTO {
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 255, message = "文档标题长度不能超过255个字符")
    private String title;

    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath;

    @NotBlank(message = "文档内容不能为空")
    private String content;
}

