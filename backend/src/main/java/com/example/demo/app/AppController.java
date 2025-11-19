// src/main/java/com/example/demo/app/AppController.java
// src/main/java/com.example.demo.app/AppController.java
package com.example.demo.app;

import com.example.demo.common.ApiResponse;
import com.example.demo.app.vo.AppVO;
import com.example.demo.app.entity.AppEntity;
import com.example.demo.app.mapper.AppMapper;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/apps")
public class AppController {
    private final AppMapper appMapper;

    public AppController(AppMapper appMapper) {
        this.appMapper = appMapper;
    }

    @GetMapping
    public ApiResponse<List<AppVO>> list(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {

        List<AppEntity> entities;

        // 根据排序参数调用不同的查询方法
        if ("rating".equals(sortBy)) {
            entities = "asc".equalsIgnoreCase(sortOrder)
                    ? appMapper.findAllOrderByRatingAsc()
                    : appMapper.findAllOrderByRatingDesc();
        } else if ("downloads".equals(sortBy)) {
            entities = "asc".equalsIgnoreCase(sortOrder)
                    ? appMapper.findAllOrderByDownloadsAsc()
                    : appMapper.findAllOrderByDownloadsDesc();
        } else {
            // 默认按ID排序
            entities = appMapper.findAllOrderById();
        }

        // 转换为 VO 对象
        List<AppVO> out = new ArrayList<>();
        for (AppEntity e : entities) {
            AppVO vo = AppVO.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .fullDescription(e.getFullDescription())
                    .avatar(e.getAvatar())
                    .category(e.getCategory())
                    .price(e.getPrice())
                    .rating(e.getRating())
                    .downloads(e.getDownloads())
                    .reviews(e.getReviews())
                    .author(e.getAuthor())
                    .publishedAt(e.getPublishedAt())
                    .build();
            out.add(vo);
        }
        return ApiResponse.ok(out);
    }

    // 根据分类查询应用（也支持排序）
    @GetMapping("/category/{category}")
    public ApiResponse<List<AppVO>> listByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {

        List<AppEntity> entities;

        if ("rating".equals(sortBy)) {
            entities = "asc".equalsIgnoreCase(sortOrder)
                    ? appMapper.findByCategoryOrderByRatingAsc(category)
                    : appMapper.findByCategoryOrderByRatingDesc(category);
        } else if ("downloads".equals(sortBy)) {
            entities = "asc".equalsIgnoreCase(sortOrder)
                    ? appMapper.findByCategoryOrderByDownloadsAsc(category)
                    : appMapper.findByCategoryOrderByDownloadsDesc(category);
        } else {
            entities = appMapper.findByCategory(category);
        }

        List<AppVO> out = new ArrayList<>();
        for (AppEntity e : entities) {
            AppVO vo = AppVO.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .fullDescription(e.getFullDescription())
                    .avatar(e.getAvatar())
                    .category(e.getCategory())
                    .price(e.getPrice())
                    .rating(e.getRating())
                    .downloads(e.getDownloads())
                    .reviews(e.getReviews())
                    .author(e.getAuthor())
                    .publishedAt(e.getPublishedAt())
                    .build();
            out.add(vo);
        }
        return ApiResponse.ok(out);
    }
}


