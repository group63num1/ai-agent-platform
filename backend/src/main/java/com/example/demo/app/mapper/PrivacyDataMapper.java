// src/main/java/com/example/demo/app/mapper/PrivacyDataMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.PrivacyData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PrivacyDataMapper {
    int insert(PrivacyData privacyData);
    PrivacyData selectById(Long id);
    List<PrivacyData> selectByUserId(Long userId);
    List<PrivacyData> selectBySessionId(Long sessionId);
    List<PrivacyData> selectByDataType(String dataType);
    List<PrivacyData> selectByDataHash(String dataHash);
    List<PrivacyData> selectExpired(); // 查询已过期的数据
    int update(PrivacyData privacyData);
    int softDelete(Long id);
    int deleteExpired(); // 删除已过期的数据
    int countByUserId(Long userId);
    int countByDataType(String dataType);
}

