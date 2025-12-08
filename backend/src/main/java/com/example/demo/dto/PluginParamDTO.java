package com.example.demo.dto;

import lombok.Data;

@Data
public class PluginParamDTO {
    private String name;
    private String description;
    private String type;
    private String location; // query, body, header etc
    private Boolean required;
    private String example;
}





