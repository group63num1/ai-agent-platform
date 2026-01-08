// src/main/java/com/example/demo/service/StatisticsService.java
package com.example.demo.service;

import com.example.demo.dto.SystemStatsDTO;
import com.example.demo.dto.TeamStatsDTO;
import com.example.demo.dto.UserStatsDTO;
import java.util.Map;

public interface StatisticsService {

    // 用户统计
    UserStatsDTO getUserStatistics();

    // 团队统计
    TeamStatsDTO getTeamStatistics();

    // 系统统计
    SystemStatsDTO getSystemStatistics();

    // 获取今日活跃用户
    Long getTodayActiveUsers();

    // 获取本周新注册用户
    Long getThisWeekNewUsers();

    // 获取团队成员统计
    Map<String, Object> getTeamMemberStatistics();
}