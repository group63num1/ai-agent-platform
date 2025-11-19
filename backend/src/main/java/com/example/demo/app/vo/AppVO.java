package com.example.demo.app.vo;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppVO {
    private Long id;
    private String name;
    private String description;
    private String fullDescription;
    private String avatar;
    private String category;
    private BigDecimal price;
    private Double rating;
    private Integer downloads;
    private Integer reviews;
    private String author;
    private LocalDate publishedAt;
    private List<String> tags;
    private List<String> features;
    private List<String> scenarios;
}
