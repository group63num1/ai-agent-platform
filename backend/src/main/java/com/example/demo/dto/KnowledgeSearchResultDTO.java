// src/main/java/com/example/demo/dto/KnowledgeSearchResultDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class KnowledgeSearchResultDTO {
    private List<KnowledgeChunkDTO> chunks; // 检索到的文档块
    private Integer totalCount; // 总数量
    private String query; // 查询内容
}

