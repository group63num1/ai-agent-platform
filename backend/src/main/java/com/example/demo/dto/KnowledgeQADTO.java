// src/main/java/com/example/demo/dto/KnowledgeQADTO.java
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class KnowledgeQADTO {
    @NotBlank(message = "问题不能为空")
    private String question;

    @Min(value = 1, message = "检索数量必须大于0")
    private Integer topK = 5; // 默认检索5个最相关的文档块

    private Long documentId; // 可选：限制在特定文档中检索
}

