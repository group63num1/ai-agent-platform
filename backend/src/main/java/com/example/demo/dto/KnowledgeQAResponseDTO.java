// src/main/java/com/example/demo/dto/KnowledgeQAResponseDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class KnowledgeQAResponseDTO {
    private String question; // 用户问题
    private String answer; // AI回答
    private List<KnowledgeChunkDTO> sources; // 参考的文档块（来源）
    private Integer sourceCount; // 来源数量
}

