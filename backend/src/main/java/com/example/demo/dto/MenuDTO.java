// src/main/java/com/example/demo/dto/MenuDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class MenuDTO {
    private String title;
    private String path;
    private String icon;
    private List<MenuDTO> children;
    private String permission; // 权限代码，用于控制菜单显示
    private Integer sortOrder; // 排序顺序
}

