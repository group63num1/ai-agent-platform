// src/main/java/com/example/demo/dto/JwtResponseDTO.java
package com.example.demo.dto;

import lombok.Data;

@Data
public class JwtResponseDTO {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;

    public JwtResponseDTO(String token, Long userId, String username) {
        this.token = token;
        this.userId = userId;
        this.username = username;
    }
}