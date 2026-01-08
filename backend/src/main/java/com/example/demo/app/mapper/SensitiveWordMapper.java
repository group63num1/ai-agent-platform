// src/main/java/com/example/demo/app/mapper/SensitiveWordMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SensitiveWordMapper {
    int insert(SensitiveWord sensitiveWord);
    SensitiveWord selectById(Long id);
    List<SensitiveWord> selectAll();
    List<SensitiveWord> selectByCategory(String category);
    List<SensitiveWord> selectByLevel(Integer level);
    List<SensitiveWord> selectByStatus(Integer status);
    List<SensitiveWord> selectActive(); // 查询启用的敏感词
    int update(SensitiveWord sensitiveWord);
    int delete(Long id);
    int countByWord(String word);
}

