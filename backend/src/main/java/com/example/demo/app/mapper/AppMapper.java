package com.example.demo.app.mapper;

import com.example.demo.app.entity.AppEntity;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AppMapper {
    // 查询所有应用，按 ID 升序排列
    List<AppEntity> findAllOrderById();

    // 根据 ID 查询单个应用
    AppEntity findById(Long id);

    // 根据分类查询应用
    List<AppEntity> findByCategory(String category);

    // 新增：按评分排序
    List<AppEntity> findAllOrderByRatingAsc();
    List<AppEntity> findAllOrderByRatingDesc();

    // 新增：按下载量排序
    List<AppEntity> findAllOrderByDownloadsAsc();
    List<AppEntity> findAllOrderByDownloadsDesc();

    // 新增：按分类和评分排序
    List<AppEntity> findByCategoryOrderByRatingAsc(String category);
    List<AppEntity> findByCategoryOrderByRatingDesc(String category);

    // 新增：按分类和下载量排序
    List<AppEntity> findByCategoryOrderByDownloadsAsc(String category);
    List<AppEntity> findByCategoryOrderByDownloadsDesc(String category);
}