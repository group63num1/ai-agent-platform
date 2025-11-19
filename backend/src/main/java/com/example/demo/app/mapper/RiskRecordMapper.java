// src/main/java/com/example/demo/app/mapper/RiskRecordMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.RiskRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RiskRecordMapper {
    int insert(RiskRecord riskRecord);
    RiskRecord selectById(Long id);
    List<RiskRecord> selectByUserId(Long userId);
    List<RiskRecord> selectBySessionId(Long sessionId);
    List<RiskRecord> selectByRiskType(String riskType);
    List<RiskRecord> selectByRiskLevel(Integer riskLevel);
    List<RiskRecord> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
    List<RiskRecord> selectHighRisk(); // 查询高风险记录
    int countByUserId(Long userId);
    int countByRiskLevel(Integer riskLevel);
    int deleteOldRecords(@Param("beforeTime") LocalDateTime beforeTime);
}

