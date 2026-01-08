// src/main/java/com/example/demo/controller/StatisticsController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.SystemStatsDTO;
import com.example.demo.dto.TeamStatsDTO;
import com.example.demo.dto.UserStatsDTO;
import com.example.demo.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取用户统计信息
     */
    @GetMapping("/users")
    public ApiResponse<UserStatsDTO> getUserStatistics() {
        try {
            UserStatsDTO userStats = statisticsService.getUserStatistics();
            return ApiResponse.ok(userStats);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取用户统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取团队统计信息
     */
    @GetMapping("/teams")
    public ApiResponse<TeamStatsDTO> getTeamStatistics() {
        try {
            TeamStatsDTO teamStats = statisticsService.getTeamStatistics();
            return ApiResponse.ok(teamStats);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取团队统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/system")
    public ApiResponse<SystemStatsDTO> getSystemStatistics() {
        try {
            SystemStatsDTO systemStats = statisticsService.getSystemStatistics();
            return ApiResponse.ok(systemStats);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取系统统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取综合统计信息
     */
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverviewStatistics() {
        try {
            UserStatsDTO userStats = statisticsService.getUserStatistics();
            TeamStatsDTO teamStats = statisticsService.getTeamStatistics();
            SystemStatsDTO systemStats = statisticsService.getSystemStatistics();

            Map<String, Object> overview = Map.of(
                    "users", userStats,
                    "teams", teamStats,
                    "system", systemStats,
                    "timestamp", System.currentTimeMillis()
            );

            return ApiResponse.ok(overview);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取综合统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日活跃用户数
     */
    @GetMapping("/active-users/today")
    public ApiResponse<Map<String, Long>> getTodayActiveUsers() {
        try {
            Long activeUsers = statisticsService.getTodayActiveUsers();
            Map<String, Long> result = Map.of("count", activeUsers);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取今日活跃用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取本周新用户数
     */
    @GetMapping("/new-users/week")
    public ApiResponse<Map<String, Long>> getThisWeekNewUsers() {
        try {
            Long newUsers = statisticsService.getThisWeekNewUsers();
            Map<String, Long> result = Map.of("count", newUsers);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取本周新用户失败: " + e.getMessage());
        }
    }

    /**
     * 获取团队成员统计
     */
    @GetMapping("/team-members")
    public ApiResponse<Map<String, Object>> getTeamMemberStatistics() {
        try {
            Map<String, Object> stats = statisticsService.getTeamMemberStatistics();
            return ApiResponse.ok(stats);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取团队成员统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时统计（简化版本）
     */
    @GetMapping("/realtime")
    public ApiResponse<Map<String, Object>> getRealtimeStatistics() {
        try {
            Map<String, Object> realtimeStats = Map.of(
                    "onlineUsers", statisticsService.getTodayActiveUsers(),
                    "totalRequests", 0L, // 简化实现
                    "systemLoad", "正常",
                    "databaseStatus", "正常",
                    "timestamp", System.currentTimeMillis()
            );

            return ApiResponse.ok(realtimeStats);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取实时统计失败: " + e.getMessage());
        }
    }
}
