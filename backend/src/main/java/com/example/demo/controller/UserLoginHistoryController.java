// src/main/java/com/example/demo/controller/UserLoginHistoryController.java
package com.example.demo.controller;

import com.example.demo.app.entity.UserLoginLog;
import com.example.demo.common.ApiResponse;
import com.example.demo.service.UserLoginLogService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profile")
public class UserLoginHistoryController {

    @Autowired
    private UserLoginLogService userLoginLogService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (token != null) {
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取当前用户的登录历史（最近50条）
     */
    @GetMapping("/login-history")
    public ApiResponse<List<UserLoginLog>> getLoginHistory(HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            List<UserLoginLog> loginHistory = userLoginLogService.getLoginHistoryByUserId(currentUserId, 50);
            return ApiResponse.ok(loginHistory);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取登录历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近N天的登录历史
     */
    @GetMapping("/recent-logins")
    public ApiResponse<List<UserLoginLog>> getRecentLoginHistory(HttpServletRequest request,
                                                                 @RequestParam(defaultValue = "7") int days) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            if (days < 1 || days > 365) {
                return ApiResponse.fail(400, "天数范围必须在1-365之间");
            }

            List<UserLoginLog> loginHistory = userLoginLogService.getRecentLoginHistory(currentUserId, days);
            return ApiResponse.ok(loginHistory);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取最近登录历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取登录统计信息
     */
    @GetMapping("/login-stats")
    public ApiResponse<?> getLoginStatistics(HttpServletRequest request,
                                             @RequestParam(defaultValue = "30") int days) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            List<UserLoginLog> recentLogs = userLoginLogService.getRecentLoginHistory(currentUserId, days);

            long totalLogins = recentLogs.size();
            long successLogins = recentLogs.stream().filter(log -> log.getStatus() == 1).count();
            long failedLogins = totalLogins - successLogins;

            // 获取最近登录的设备信息
            String lastDevice = recentLogs.stream()
                    .filter(log -> log.getStatus() == 1)
                    .findFirst()
                    .map(UserLoginLog::getDeviceInfo)
                    .orElse("未知设备");

            // 获取最近登录的IP地址
            String lastIp = recentLogs.stream()
                    .filter(log -> log.getStatus() == 1)
                    .findFirst()
                    .map(UserLoginLog::getIpAddress)
                    .orElse("未知IP");

            var stats = new java.util.HashMap<String, Object>();
            stats.put("totalLogins", totalLogins);
            stats.put("successLogins", successLogins);
            stats.put("failedLogins", failedLogins);
            stats.put("successRate", totalLogins > 0 ? (double) successLogins / totalLogins : 0);
            stats.put("lastDevice", lastDevice);
            stats.put("lastIp", lastIp);
            stats.put("periodDays", days);

            return ApiResponse.ok(stats);
        } catch (Exception e) {
            return ApiResponse.fail(400, "获取登录统计失败: " + e.getMessage());
        }
    }
}