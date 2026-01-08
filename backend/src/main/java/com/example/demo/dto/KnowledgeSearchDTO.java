// src/main/java/com/example/demo/dto/KnowledgeSearchDTO.java
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class KnowledgeSearchDTO {
    @NotBlank(message = "查询内容不能为空")
    private String query;

    @Min(value = 1, message = "返回数量必须大于0")
    private Integer topK = 5; // 默认返回5个最相关的文档块

    private Long documentId; // 可选：限制在特定文档中搜索
}

