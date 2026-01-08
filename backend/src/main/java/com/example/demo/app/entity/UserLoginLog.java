// src/main/java/com/example/demo/app/entity/UserLoginLog.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserLoginLog {
    private Long id;
    private Long userId;
    private String loginType;    // email, username, phone, sms
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private String location;
    private Integer status;      // 0-失败，1-成功
    private String failureReason;
    private String sessionId;
    private LocalDateTime createdAt;
}