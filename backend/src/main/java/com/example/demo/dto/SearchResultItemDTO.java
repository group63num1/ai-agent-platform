package com.example.demo.dto;

import lombok.Data;

@Data
public class SearchResultItemDTO {
    private String id;
    private String content;
    private String source;
    private Double score;
    private Integer chunkIndex;
}

