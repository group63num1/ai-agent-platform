package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class KnowledgeBaseUpdateRequest {
    private String name;
    private String description;
    private String category;
    private Boolean enabled;
    private List<String> deleteFiles;
}

