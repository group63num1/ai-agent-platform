package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class AgentListResponse {
    private List<AgentDTO> items;
    private long total;
    private int page;
    private int pageSize;
}


