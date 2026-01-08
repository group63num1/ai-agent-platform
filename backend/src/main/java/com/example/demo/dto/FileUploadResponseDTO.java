// src/main/java/com/example/demo/dto/FileUploadResponseDTO.java
package com.example.demo.dto;

import lombok.Data;

@Data
public class FileUploadResponseDTO {
    private String url;
    private String filename;
    private Long size;
    private String message;

    public FileUploadResponseDTO(String url, String filename, Long size) {
        this.url = url;
        this.filename = filename;
        this.size = size;
        this.message = "上传成功";
    }
}