// src/main/java/com/example/demo/service/VerificationCodeService.java
package com.example.demo.service;

public interface VerificationCodeService {

    // 生成验证码
    String generateVerificationCode(String target, String type, Long userId);

    // 验证验证码
    boolean verifyCode(String target, String type, String code);

    // 发送邮箱验证码
    boolean sendEmailCode(String email, String type, Long userId);

    // 发送短信验证码
    boolean sendSmsCode(String phone, String type, Long userId);

    // 清理过期验证码
    int cleanupExpiredCodes();

    // 检查发送频率限制
    boolean isRateLimited(String target, String type);
}