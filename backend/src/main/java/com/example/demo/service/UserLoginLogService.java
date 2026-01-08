// src/main/java/com/example/demo/service/UserLoginLogService.java
package com.example.demo.service;

import com.example.demo.app.entity.UserLoginLog;
import java.time.LocalDateTime;
import java.util.List;

public interface UserLoginLogService {

    // 记录登录成功
    void recordLoginSuccess(Long userId, String loginType, String ipAddress,
                            String userAgent, String deviceInfo, String location);

    // 记录登录失败
    void recordLoginFailure(Long userId, String loginType, String ipAddress,
                            String userAgent, String failureReason);

    // 获取用户的登录历史
    List<UserLoginLog> getLoginHistoryByUserId(Long userId);

    // 获取用户最近N条登录历史
    List<UserLoginLog> getLoginHistoryByUserId(Long userId, int limit);

    // 获取用户最近N天的登录历史
    List<UserLoginLog> getRecentLoginHistory(Long userId, int days);

    // 统计用户登录失败次数（用于登录锁定）
    int countRecentLoginFailures(Long userId, LocalDateTime sinceTime);

    // 清理过期日志
    int cleanupOldLogs(int keepDays);
}