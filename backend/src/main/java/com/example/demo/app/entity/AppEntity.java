package com.example.demo.app.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppEntity {
    private Long id;
    private String name;
    private String description;
    private String fullDescription;  // 对应数据库字段 full_description
    private String avatar;
    private String category;
    private BigDecimal price;
    private Double rating;
    private Integer downloads;
    private Integer reviews;
    private String author;
    private LocalDate publishedAt;   // 对应数据库字段 published_at
}