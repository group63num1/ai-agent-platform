// src/main/java/com/example/demo/service/impl/VerificationCodeServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.app.entity.UserVerificationCode;
import com.example.demo.app.mapper.UserVerificationCodeMapper;
import com.example.demo.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// 以下导入在开发模式下未使用，但在生产环境需要启用真实邮件发送时会用到
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private UserVerificationCodeMapper verificationCodeMapper;

    // 开发模式下未使用，生产环境启用真实邮件发送时需要取消注释
    // @Autowired
    // private JavaMailSender mailSender;

    @Value("${spring.mail.username:1749683268@qq.com}")
    private String fromEmail;

    @Value("${verification.code.length:6}")
    private int codeLength;

    @Value("${verification.code.expire.minutes:5}")
    private int expireMinutes;

    @Value("${verification.rate.limit.minutes:1}")
    private int rateLimitMinutes;

    private static final String EMAIL_REGISTER = "email_register";
    private static final String EMAIL_RESET = "email_reset";
    private static final String SMS_LOGIN = "sms_login";
    private static final String SMS_REGISTER = "sms_register";

    private final Random random = new Random();

    @Override
    public String generateVerificationCode(String target, String type, Long userId) {
        // 检查发送频率
        if (isRateLimited(target, type)) {
            throw new RuntimeException("验证码发送过于频繁，请稍后再试");
        }

        // 生成随机验证码
        String code = generateRandomCode();

        // 创建验证码记录
        UserVerificationCode verificationCode = new UserVerificationCode();
        verificationCode.setUserId(userId);
        verificationCode.setType(type);
        verificationCode.setTarget(target);
        verificationCode.setCode(code);
        verificationCode.setUsed(0);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(expireMinutes));

        verificationCodeMapper.insert(verificationCode);
        return code;
    }

    @Override
    public boolean verifyCode(String target, String type, String code) {
        UserVerificationCode validCode = verificationCodeMapper.selectValidCode(target, type, code);
        if (validCode == null) {
            return false;
        }

        // 标记为已使用
        verificationCodeMapper.markAsUsed(validCode.getId());
        return true;
    }

    @Override
    public boolean sendEmailCode(String email, String type, Long userId) {
        try {
            String code = generateVerificationCode(email, type, userId);

            // 根据类型设置不同的邮件内容
            String subject = "";
            String content = "";
            switch (type) {
                case EMAIL_REGISTER:
                    subject = "用户注册验证码";
                    content = buildRegisterEmailContent(code);
                    break;
                case EMAIL_RESET:
                    subject = "密码重置验证码";
                    content = buildResetEmailContent(code);
                    break;
                default:
                    subject = "验证码";
                    content = "您的验证码是: " + code + "，有效期为" + expireMinutes + "分钟";
            }

            // ⚠️ 开发模式：模拟邮件发送（实际项目中需要配置真实的邮件服务）
            // 在开发环境中，验证码会打印到控制台，方便测试
            System.out.println("========================================");
            System.out.println("【开发模式】邮件验证码（仅用于测试）");
            System.out.println("收件人: " + email);
            System.out.println("主题: " + subject);
            System.out.println("验证码: " + code);
            System.out.println("邮件内容:");
            System.out.println(content);
            System.out.println("有效期: " + expireMinutes + " 分钟");
            System.out.println("========================================");
            System.out.println("⚠️ 注意：这是开发模式，邮件并未真正发送！");
            System.out.println("⚠️ 实际生产环境需要配置真实的邮件服务（SMTP服务器等）");
            System.out.println("========================================");

            // 实际生产环境可以取消注释以下代码，使用真实的邮件发送
            /*
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            */

            return true;
        } catch (Exception e) {
            String errorMsg = "发送邮件验证码失败: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean sendSmsCode(String phone, String type, Long userId) {
        try {
            String code = generateVerificationCode(phone, type, userId);

            // 根据类型设置不同的短信内容
            String message = "";
            switch (type) {
                case SMS_REGISTER:
                    message = "欢迎注册用户管理系统！您的注册验证码是: " + code + 
                             "，有效期为 " + expireMinutes + " 分钟。";
                    break;
                case SMS_LOGIN:
                    message = "您的登录验证码是: " + code + 
                             "，有效期为 " + expireMinutes + " 分钟。";
                    break;
                default:
                    message = "您的验证码是: " + code + 
                             "，有效期为 " + expireMinutes + " 分钟。";
            }
            
            // ⚠️ 开发模式：模拟短信发送（实际项目中需要集成短信服务商）
            // 在开发环境中，验证码会打印到控制台，方便测试
            System.out.println("========================================");
            System.out.println("【开发模式】短信验证码（仅用于测试）");
            System.out.println("手机号: " + phone);
            System.out.println("验证码: " + code);
            System.out.println("短信内容: " + message);
            System.out.println("有效期: " + expireMinutes + " 分钟");
            System.out.println("========================================");
            System.out.println("⚠️ 注意：这是开发模式，短信并未真正发送！");
            System.out.println("⚠️ 实际生产环境需要集成短信服务商（如阿里云、腾讯云等）");
            System.out.println("========================================");

            // 这里可以集成阿里云、腾讯云等短信服务
            // 示例：
            // AliyunSmsService smsService = new AliyunSmsService();
            // smsService.send(phone, code, templateId);

            return true;
        } catch (Exception e) {
            String errorMsg = "发送短信验证码失败: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new RuntimeException("短信发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int cleanupExpiredCodes() {
        return verificationCodeMapper.deleteExpiredCodes(LocalDateTime.now());
    }

    @Override
    public boolean isRateLimited(String target, String type) {
        LocalDateTime sinceTime = LocalDateTime.now().minusMinutes(rateLimitMinutes);
        int recentAttempts = verificationCodeMapper.countRecentAttempts(target, type, sinceTime);
        return recentAttempts >= 3; // 1分钟内最多3次
    }

    /**
     * 生成随机验证码
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 构建注册邮件内容
     */
    private String buildRegisterEmailContent(String code) {
        return "欢迎注册用户管理系统！\n\n" +
                "您的注册验证码是: " + code + "\n\n" +
                "验证码有效期为 " + expireMinutes + " 分钟。\n" +
                "如果不是您本人操作，请忽略此邮件。\n\n" +
                "DevOps用户管理系统团队";
    }

    /**
     * 构建重置密码邮件内容
     */
    private String buildResetEmailContent(String code) {
        return "您正在重置密码！\n\n" +
                "您的密码重置验证码是: " + code + "\n\n" +
                "验证码有效期为 " + expireMinutes + " 分钟。\n" +
                "如果不是您本人操作，请立即修改密码。\n\n" +
                "DevOps用户管理系统团队";
    }
}