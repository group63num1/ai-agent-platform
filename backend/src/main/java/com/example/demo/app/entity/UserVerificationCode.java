// src/main/java/com/example/demo/app/entity/UserVerificationCode.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVerificationCode {
    private Long id;
    private Long userId;
    private String type;        // email_register, email_reset, sms_register, sms_reset, sms_login
    private String target;      // 邮箱或手机号
    private String code;
    private Integer used;       // 0-未使用，1-已使用
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}