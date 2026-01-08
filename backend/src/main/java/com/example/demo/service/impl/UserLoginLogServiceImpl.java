// src/main/java/com/example/demo/service/impl/UserLoginLogServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.app.entity.UserLoginLog;
import com.example.demo.app.mapper.UserLoginLogMapper;
import com.example.demo.service.UserLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserLoginLogServiceImpl implements UserLoginLogService {

    @Autowired
    private UserLoginLogMapper userLoginLogMapper;

    @Override
    public void recordLoginSuccess(Long userId, String loginType, String ipAddress,
                                   String userAgent, String deviceInfo, String location) {
        UserLoginLog log = new UserLoginLog();
        log.setUserId(userId);
        log.setLoginType(loginType);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setDeviceInfo(deviceInfo);
        log.setLocation(location);
        log.setStatus(1); // 成功
        log.setSessionId(generateSessionId());
        log.setCreatedAt(LocalDateTime.now());

        userLoginLogMapper.insert(log);
    }

    @Override
    public void recordLoginFailure(Long userId, String loginType, String ipAddress,
                                   String userAgent, String failureReason) {
        UserLoginLog log = new UserLoginLog();
        log.setUserId(userId);
        log.setLoginType(loginType);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setStatus(0); // 失败
        log.setFailureReason(failureReason);
        log.setCreatedAt(LocalDateTime.now());

        userLoginLogMapper.insert(log);
    }

    @Override
    public List<UserLoginLog> getLoginHistoryByUserId(Long userId) {
        return userLoginLogMapper.selectByUserId(userId);
    }

    @Override
    public List<UserLoginLog> getLoginHistoryByUserId(Long userId, int limit) {
        return userLoginLogMapper.selectByUserIdAndLimit(userId, limit);
    }

    @Override
    public List<UserLoginLog> getRecentLoginHistory(Long userId, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return userLoginLogMapper.selectByUserIdAndTime(userId, startTime);
    }

    @Override
    public int countRecentLoginFailures(Long userId, LocalDateTime sinceTime) {
        return userLoginLogMapper.countLoginAttempts(userId, sinceTime);
    }

    @Override
    public int cleanupOldLogs(int keepDays) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(keepDays);
        return userLoginLogMapper.deleteOldLogs(beforeTime);
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
