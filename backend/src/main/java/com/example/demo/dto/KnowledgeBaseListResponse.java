package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeBaseListResponse {
    private List<KnowledgeBaseDTO> items;
    private Integer total;
    private Integer page;
    private Integer pageSize;
}

